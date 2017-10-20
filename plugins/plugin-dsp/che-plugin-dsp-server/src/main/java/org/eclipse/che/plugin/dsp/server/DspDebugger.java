/*
 * Copyright (c) 2017 Kichwa Coders Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Kichwa Coders Ltd. - initial API and implementation
 */
package org.eclipse.che.plugin.dsp.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.DebuggerInfo;
import org.eclipse.che.api.debug.shared.model.Field;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.Method;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.ThreadState;
import org.eclipse.che.api.debug.shared.model.ThreadStatus;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.action.ResumeAction;
import org.eclipse.che.api.debug.shared.model.action.StartAction;
import org.eclipse.che.api.debug.shared.model.action.StepIntoAction;
import org.eclipse.che.api.debug.shared.model.action.StepOutAction;
import org.eclipse.che.api.debug.shared.model.action.StepOverAction;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.MethodImpl;
import org.eclipse.che.api.debug.shared.model.impl.SimpleValueImpl;
import org.eclipse.che.api.debug.shared.model.impl.StackFrameDumpImpl;
import org.eclipse.che.api.debug.shared.model.impl.ThreadStateImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariableImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.DisconnectEventImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.SuspendEventImpl;
import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.dsp4j.DebugProtocol;
import org.eclipse.dsp4j.DebugProtocol.Capabilities;
import org.eclipse.dsp4j.DebugProtocol.ContinueArguments;
import org.eclipse.dsp4j.DebugProtocol.DisconnectArguments;
import org.eclipse.dsp4j.DebugProtocol.InitializeRequestArguments;
import org.eclipse.dsp4j.DebugProtocol.NextArguments;
import org.eclipse.dsp4j.DebugProtocol.Scope;
import org.eclipse.dsp4j.DebugProtocol.ScopesArguments;
import org.eclipse.dsp4j.DebugProtocol.ScopesResponse;
import org.eclipse.dsp4j.DebugProtocol.SetBreakpointsArguments;
import org.eclipse.dsp4j.DebugProtocol.SetBreakpointsResponse;
import org.eclipse.dsp4j.DebugProtocol.Source;
import org.eclipse.dsp4j.DebugProtocol.SourceBreakpoint;
import org.eclipse.dsp4j.DebugProtocol.StackFrame;
import org.eclipse.dsp4j.DebugProtocol.StackTraceArguments;
import org.eclipse.dsp4j.DebugProtocol.StackTraceResponse;
import org.eclipse.dsp4j.DebugProtocol.StepInArguments;
import org.eclipse.dsp4j.DebugProtocol.StepOutArguments;
import org.eclipse.dsp4j.DebugProtocol.StoppedEvent;
import org.eclipse.dsp4j.DebugProtocol.TerminatedEvent;
import org.eclipse.dsp4j.DebugProtocol.Thread;
import org.eclipse.dsp4j.DebugProtocol.ThreadsResponse;
import org.eclipse.dsp4j.DebugProtocol.VariablesArguments;
import org.eclipse.dsp4j.DebugProtocol.VariablesResponse;
import org.eclipse.dsp4j.IDebugProtocolClient;
import org.eclipse.dsp4j.IDebugProtocolServer;
import org.eclipse.dsp4j.jsonrpc.DebugLauncher;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

public class DspDebugger implements Debugger, IDebugProtocolClient {

  private Process process;
  private Future<?> debugProtocolFuture;
  private IDebugProtocolServer debugProtocolServer;
  private String targetName;
  private DebuggerCallback debuggerCallback;
  private Map<String, Object> launchArguments;
  private ExecutorService threadPool;
  private Integer currentThreadId;
  private Capabilities capabilities;
  private List<ThreadState> threadDump;
  private int primitiveVariableId = 0;
  private Map<ImmutableVariablePath, Variable> variableMap = new HashMap<>();
  //  private List<ImmutableBreakpoint> breakpoints = new ArrayList<>();
  private Map<Source, List<SourceBreakpoint>> targetBreakpoints = new HashMap<>();

  public DspDebugger(
      DebuggerCallback debuggerCallback, Process process, Map<String, Object> launchArguments)
      throws DebuggerException {
    this.debuggerCallback = debuggerCallback;
    this.process = process;
    this.launchArguments = launchArguments;
  }

  @Override
  public void start(StartAction action) throws DebuggerException {
    threadPool = Executors.newCachedThreadPool();

    DebugLauncher<IDebugProtocolServer> debugProtocolLauncher =
        DebugLauncher.createLauncher(
            this,
            IDebugProtocolServer.class,
            process.getInputStream(),
            process.getOutputStream(),
            threadPool,
            Function.identity());

    debugProtocolFuture = debugProtocolLauncher.startListening();
    debugProtocolServer = debugProtocolLauncher.getRemoteProxy();

    capabilities =
        complete(
            debugProtocolServer.initialize(
                new InitializeRequestArguments()
                    .setClientID("che")
                    .setAdapterID(launchArguments.getOrDefault("type", "").toString())
                    .setPathFormat("path")));
    Object object = launchArguments.get("program");
    targetName = Objects.toString(object, "Debug Adapter Target");

    complete(debugProtocolServer.launch(Either.forLeft(launchArguments)));

    targetBreakpoints.clear();
    action.getBreakpoints().forEach(this::addBreakpointToMap);
    sendBreakpoints();

    complete(debugProtocolServer.configurationDone());
  }

  private void addBreakpointToMap(Breakpoint breakpoint) {
    Location location = breakpoint.getLocation();
    String name = "readme.md"; // TODO: location.getTarget();
    String path = "/projects/readme/readme.md"; // TODO: location.getResourceProjectPath();
    int lineNumber = location.getLineNumber();
    String condition = breakpoint.getCondition();

    Source source = new Source().setName(name).setPath(path);

    List<SourceBreakpoint> sourceBreakpoints =
        targetBreakpoints.computeIfAbsent(source, s -> new ArrayList<>());
    sourceBreakpoints.add(new SourceBreakpoint().setLine(lineNumber).setCondition(condition));
  }

  private void deleteBreakpointFromMap(Location location) {
    String name = "readme.md"; // TODO: location.getTarget();
    String path = "/projects/readme/readme.md"; // TODO: location.getResourceProjectPath();
    int lineNumber = location.getLineNumber();
    for (Entry<Source, List<SourceBreakpoint>> entry : targetBreakpoints.entrySet()) {
      Source source = entry.getKey();
      if (Objects.equals(name, source.name) && Objects.equals(path, source.path)) {
        List<SourceBreakpoint> bps = entry.getValue();
        for (Iterator<SourceBreakpoint> iterator = bps.iterator(); iterator.hasNext(); ) {
          SourceBreakpoint sourceBreakpoint = (SourceBreakpoint) iterator.next();
          if (Objects.equals(lineNumber, sourceBreakpoint.line)) {
            iterator.remove();
          }
        }
      }
    }
  }

  private void deleteAllBreakpointsFromMap() {
    for (Entry<Source, List<SourceBreakpoint>> entry : targetBreakpoints.entrySet()) {
      entry.getValue().clear();
    }
  }

  private void sendBreakpoints() throws DebuggerException {
    for (Iterator<Entry<Source, List<SourceBreakpoint>>> iterator =
            targetBreakpoints.entrySet().iterator();
        iterator.hasNext();
        ) {
      Entry<Source, List<SourceBreakpoint>> entry = iterator.next();

      Source source = entry.getKey();
      List<SourceBreakpoint> bps = entry.getValue();
      Integer[] lines = bps.stream().map(sb -> sb.line).toArray(Integer[]::new);
      SourceBreakpoint[] sourceBps = bps.toArray(new SourceBreakpoint[bps.size()]);

      CompletableFuture<SetBreakpointsResponse.Body> future =
          debugProtocolServer.setBreakpoints(
              new SetBreakpointsArguments()
                  .setSource(source)
                  .setLines(lines)
                  .setBreakpoints(sourceBps)
                  .setSourceModified(false));
      // XXX: we should really do something with the response, but Che does not support, AFAICT,
      // updating where breakpoints are actually installed, etc.
      complete(future);

      // Once we told adapter there are no breakpoints for a source file, we can stop tracking that file
      if (bps.isEmpty()) {
        iterator.remove();
      }
    }
  }

  /**
   * Gets the response from the debug command
   *
   * @param future
   * @throws DebuggerException
   */
  private static <T> T complete(CompletableFuture<T> future) throws DebuggerException {
    try {
      T t = future.get();
      return t;
    } catch (InterruptedException | ExecutionException e) {
      throw new DebuggerException(e.getMessage(), e);
    }
  }

  private Integer getCurrentThreadId() {
    return currentThreadId;
  }

  private Location getCurrentLocation() {
    Thread thread = getCurrentThread();
    StackFrame frame = getTopStackFrame(thread);

    return getLocation(frame);
  }

  private Location getLocation(StackFrame frame) {
    String path = "readme.md"; // TODO: frame.source.path;
    Integer line = frame.line;
    Method method = new MethodImpl(frame.name, Collections.emptyList());
    // Method method = new MethodImpl(frame.name, Collections.emptyList());

    return new LocationImpl(path, line, false, 0, path, method, 0);
    // return new LocationImpl(path, line, path);
  }

  private Location getLocation(long threadId, int frameIndex) throws DebuggerException {
    List<ThreadState> threadDump = getThreadDump();
    for (ThreadState threadState : threadDump) {
      if (threadState.getId() == threadId) {
        List<? extends StackFrameDump> frames = threadState.getFrames();
        if (frames.size() > frameIndex) {
          return frames.get(frameIndex).getLocation();
        }
        throw new DebuggerException("Invalid frame index");
      }
    }
    throw new DebuggerException("Invalid thread id");
  }

  private StackFrame getTopStackFrame(Thread thread) {
    StackFrame[] stackFrames = getStackFrames(thread);
    StackFrame frame = stackFrames[0];
    return frame;
  }

  private StackFrame[] getStackFrames(Thread thread) {
    CompletableFuture<StackTraceResponse.Body> future =
        debugProtocolServer.stackTrace(
            new StackTraceArguments().setThreadId(thread.id).setStartFrame(0).setLevels(10));
    StackTraceResponse.Body framesBody;
    try {
      framesBody = future.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Can't get frames", e);
    }
    if (framesBody.stackFrames == null || framesBody.stackFrames.length == 0) {
      throw new RuntimeException("frames empty");
    }
    StackFrame[] stackFrames = framesBody.stackFrames;
    return stackFrames;
  }

  private Thread getCurrentThread() {
    Thread[] threads = getThreads();
    Integer currentThreadId = getCurrentThreadId();
    int resolvedId;
    if (currentThreadId == null) {
      resolvedId = 0;
    } else {
      resolvedId = currentThreadId;
      if (resolvedId < 0 || resolvedId >= threads.length) {
        resolvedId = 0;
      }
    }
    return threads[resolvedId];
  }

  private Thread[] getThreads() {
    CompletableFuture<ThreadsResponse.Body> threadsFuture = debugProtocolServer.threads();
    ThreadsResponse.Body threadsBody;
    try {
      threadsBody = threadsFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Can't get threads", e);
    }
    if (threadsBody.threads == null || threadsBody.threads.length == 0) {
      throw new RuntimeException("threads empty");
    }
    return threadsBody.threads;
  }

  private synchronized void clearThreadsDump() {
    threadDump = null;
  }

  /**
   * Check and update cache of threads and frames if needed.
   *
   * @return
   */
  private synchronized List<ThreadState> getThreadsDump() {
    if (threadDump == null) {
      threadDump = new ArrayList<>();
      Thread[] threads = getThreads();
      for (Thread thread : threads) {
        long id = thread.id == null ? 0 : thread.id - 1;
        String name = thread.name;
        String groupName = null;
        ThreadStatus status = ThreadStatus.UNKNOWN; /* TODO */
        boolean isSuspended = true; /* TODO */
        List<StackFrameDump> frames = new ArrayList<>();
        StackFrame[] stackFrames = getStackFrames(thread);
        for (StackFrame stackFrame : stackFrames) {
          List<Field> fields = Collections.emptyList();
          List<Variable> variables = Collections.emptyList();
          StackFrameDump stackFrameDump =
              new StackFrameDumpImpl(fields, variables, getLocation(stackFrame));
          frames.add(stackFrameDump);
        }
        ThreadState threadState =
            new ThreadStateImpl(id, name, groupName, status, isSuspended, frames);
        threadDump.add(threadState);
      }
    }
    return threadDump;
  }

  @Override
  public DebuggerInfo getInfo() throws DebuggerException {
    return new DebuggerInfo() {

      @Override
      public String getVersion() {
        // TODO Auto-generated method stub
        return "1.0.0";
      }

      @Override
      public int getPort() {
        // TODO Auto-generated method stub
        return 0;
      }

      @Override
      public int getPid() {
        // TODO Auto-generated method stub
        return 0;
      }

      @Override
      public String getName() {
        return targetName;
      }

      @Override
      public String getHost() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String getFile() {
        // TODO Auto-generated method stub
        return null;
      }
    };
  }

  @Override
  public void disconnect() throws DebuggerException {
    complete(debugProtocolServer.disconnect(new DisconnectArguments().setTerminateDebuggee(true)));
  }

  @Override
  public void terminated(TerminatedEvent.Body body) {
    threadPool.submit(() -> debuggerCallback.onEvent(new DisconnectEventImpl()));
  }

  @Override
  public void addBreakpoint(Breakpoint breakpoint) throws DebuggerException {
    addBreakpointToMap(breakpoint);
    sendBreakpoints();
  }

  @Override
  public void deleteBreakpoint(Location location) throws DebuggerException {
    if (location == null) {
      return;
    }
    deleteBreakpointFromMap(location);
    sendBreakpoints();
  }

  @Override
  public void deleteAllBreakpoints() throws DebuggerException {
    deleteAllBreakpointsFromMap();
    sendBreakpoints();
  }

  @Override
  public List<Breakpoint> getAllBreakpoints() throws DebuggerException {
    return new ArrayList<>();
  }

  @Override
  public SimpleValue getValue(VariablePath variablePath) throws DebuggerException {
    if (variablePath == null
        || variablePath.getPath() == null
        || variablePath.getPath().isEmpty()) {
      throw new DebuggerException("Malformed request, variablePath missing");
    }
    ImmutableVariablePath existingVariablePath = new ImmutableVariablePath(variablePath);
    Variable existingVariable = variableMap.get(existingVariablePath);
    if (existingVariable == null) {
      throw new DebuggerException(
          "Malformed request, unknown variablePath:" + variablePath.getPath());
    }

    if (!existingVariable.isPrimitive() && existingVariable.getValue().getVariables().isEmpty()) {
      List<String> path = variablePath.getPath();
      String string = path.get(path.size() - 1);

      int variablesReference;
      try {
        variablesReference = Integer.parseInt(string);
      } catch (NumberFormatException e) {
        throw new DebuggerException("Malformed request, variablePath invalid: " + path, e);
      }
      VariablesResponse.Body variablesResponse =
          complete(
              debugProtocolServer.variables(
                  new VariablesArguments().setVariablesReference(variablesReference)));

      List<Variable> variables = new ArrayList<>();
      for (DebugProtocol.Variable variable : variablesResponse.variables) {
        List<String> list = new ArrayList<>(path);
        boolean primitive;
        if (variable.variablesReference == 0) {
          primitive = true;
          primitiveVariableId++;
          list.add("primitive" + Integer.toString(primitiveVariableId));
        } else {
          primitive = false;
          list.add(Integer.toString(variable.variablesReference));
        }
        ImmutableVariablePath childVariablePath = new ImmutableVariablePath(list);
        VariableImpl variableImpl =
            new VariableImpl(
                variable.type,
                variable.name,
                new SimpleValueImpl(variable.value),
                primitive,
                childVariablePath);
        variables.add(variableImpl);
        variableMap.put(childVariablePath, variableImpl);
      }
      existingVariable =
          new VariableImpl(
              existingVariable.getType(),
              existingVariable.getName(),
              new SimpleValueImpl(variables, existingVariable.getValue().getString()),
              existingVariable.isPrimitive(),
              existingVariablePath);
      variableMap.put(existingVariablePath, existingVariable);
    }
    return existingVariable.getValue();
  }

  @Override
  public SimpleValue getValue(VariablePath variablePath, long threadId, int frameIndex)
      throws DebuggerException {
    // The variable paths embed the thread id and frame index by use of
    // variable references. Therefore don't need threadId/frameIndex
    return getValue(variablePath);
  }

  @Override
  public void setValue(Variable variable) throws DebuggerException {
    // TODO Auto-generated method stub

  }

  @Override
  public void setValue(Variable variable, long threadId, int frameIndex) throws DebuggerException {
    // TODO Auto-generated method stub
    Debugger.super.setValue(variable, threadId, frameIndex);
  }

  @Override
  public String evaluate(String expression) throws DebuggerException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String evaluate(String expression, long threadId, int frameIndex)
      throws DebuggerException {
    // TODO Auto-generated method stub
    return Debugger.super.evaluate(expression, threadId, frameIndex);
  }

  @Override
  public void stopped(StoppedEvent.Body body) {
    currentThreadId = body.threadId;
    clearThreadsDump();
    variableMap.clear();
    threadPool.submit(() -> debuggerCallback.onEvent(new SuspendEventImpl(getCurrentLocation())));
  }

  @Override
  public void stepOver(StepOverAction action) throws DebuggerException {
    complete(debugProtocolServer.next(new NextArguments().setThreadId(getCurrentThreadId())));
  }

  @Override
  public void stepInto(StepIntoAction action) throws DebuggerException {
    complete(debugProtocolServer.stepIn(new StepInArguments().setThreadId(getCurrentThreadId())));
  }

  @Override
  public void stepOut(StepOutAction action) throws DebuggerException {
    complete(debugProtocolServer.stepOut(new StepOutArguments().setThreadId(getCurrentThreadId())));
  }

  @Override
  public void resume(ResumeAction action) throws DebuggerException {
    complete(
        debugProtocolServer.continue_(new ContinueArguments().setThreadId(getCurrentThreadId())));
  }

  @Override
  public StackFrameDump dumpStackFrame() throws DebuggerException {
    return getStackFrameDump(getCurrentThreadId(), 0);
  }

  @Override
  public StackFrameDump getStackFrameDump(long threadId, int frameIndex) throws DebuggerException {
    ScopesResponse.Body scopesResponse =
        complete(debugProtocolServer.scopes(new ScopesArguments().setFrameId(frameIndex)));
    List<Variable> variables = new ArrayList<>();
    for (Scope scope : scopesResponse.scopes) {
      ImmutableVariablePath variablePath =
          new ImmutableVariablePath(Integer.toString(scope.variablesReference));
      VariableImpl variableImpl =
          new VariableImpl(
              scope.name, scope.name, new SimpleValueImpl(scope.name), false, variablePath);
      variables.add(variableImpl);
      variableMap.put(variablePath, variableImpl);
    }

    return new StackFrameDumpImpl(
        Collections.emptyList(), variables, getLocation(threadId, frameIndex));
  }

  @Override
  public List<ThreadState> getThreadDump() throws DebuggerException {
    return getThreadsDump();
  }
}
