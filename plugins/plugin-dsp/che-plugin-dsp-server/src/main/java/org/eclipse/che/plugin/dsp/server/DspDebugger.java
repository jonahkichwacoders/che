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

import com.google.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.SuspendPolicy;
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
import org.eclipse.che.api.debug.shared.model.impl.SimpleValueImpl;
import org.eclipse.che.api.debug.shared.model.impl.StackFrameDumpImpl;
import org.eclipse.che.api.debug.shared.model.impl.ThreadStateImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariableImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.DisconnectEventImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.SuspendEventImpl;
import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.Debugger.DebuggerCallback;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.lsp4j.debug.BreakpointEventArguments;
import org.eclipse.lsp4j.debug.Capabilities;
import org.eclipse.lsp4j.debug.ConfigurationDoneArguments;
import org.eclipse.lsp4j.debug.ContinueArguments;
import org.eclipse.lsp4j.debug.ContinuedEventArguments;
import org.eclipse.lsp4j.debug.DisconnectArguments;
import org.eclipse.lsp4j.debug.ExitedEventArguments;
import org.eclipse.lsp4j.debug.InitializeRequestArguments;
import org.eclipse.lsp4j.debug.LoadedSourceEventArguments;
import org.eclipse.lsp4j.debug.ModuleEventArguments;
import org.eclipse.lsp4j.debug.NextArguments;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.ProcessEventArguments;
import org.eclipse.lsp4j.debug.Scope;
import org.eclipse.lsp4j.debug.ScopesArguments;
import org.eclipse.lsp4j.debug.ScopesResponse;
import org.eclipse.lsp4j.debug.StackFrame;
import org.eclipse.lsp4j.debug.StackTraceArguments;
import org.eclipse.lsp4j.debug.StackTraceResponse;
import org.eclipse.lsp4j.debug.StepInArguments;
import org.eclipse.lsp4j.debug.StepOutArguments;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.TerminatedEventArguments;
import org.eclipse.lsp4j.debug.Thread;
import org.eclipse.lsp4j.debug.ThreadEventArguments;
import org.eclipse.lsp4j.debug.ThreadsResponse;
import org.eclipse.lsp4j.debug.VariablesArguments;
import org.eclipse.lsp4j.debug.VariablesResponse;
import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.validation.ReflectiveMessageValidator;

public class DspDebugger implements Debugger, IDebugProtocolClient {
  private static boolean TRACE_IO = true;
  private static boolean TRACE_MESSAGES = false;

  private Process process;
  private Future<?> debugProtocolFuture;
  private IDebugProtocolServer debugProtocolServer;
  private String targetName;
  private DebuggerCallback debuggerCallback;
  private Map<String, Object> launchArguments;
  private ExecutorService threadPool;
  private Long currentThreadId;
  private Capabilities capabilities;
  /** Once we have received initialized event, this member will be "done" as a flag */
  private CompletableFuture<Void> initialized = new CompletableFuture<Void>();

  private List<ThreadState> threadDump;
  private long primitiveVariableId = 0;
  private Map<ImmutableVariablePath, Variable> variableMap = new HashMap<>();
  @Inject private DspBreakpointManager breakpointManager;
  @Inject private DspLocationHelper locationHelper;

  public void init(
      DebuggerCallback debuggerCallback, Process process, Map<String, Object> launchArguments)
      throws DebuggerException {
    this.debuggerCallback = debuggerCallback;
    this.process = process;
    this.launchArguments = launchArguments;
  }

  @Override
  public void start(StartAction action) throws DebuggerException {
    threadPool = Executors.newCachedThreadPool();

    InputStream in = process.getInputStream();
    OutputStream out = process.getOutputStream();

    File file = new File("/tmp/log");
    FileOutputStream stream = null;
    try {
      stream = new FileOutputStream(file, true);
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    PrintWriter traceMessages;
    if (TRACE_MESSAGES) {
      traceMessages = new PrintWriter(stream);
    } else {
      traceMessages = null;
    }
    if (TRACE_IO) {
      in = new TraceInputStream(in, stream);
      out = new TraceOutputStream(out, stream);
    }
    // TODO this Function copied from createClientLauncher so that I can replace
    // threadpool, so make this wrapper more accessible
    Function<MessageConsumer, MessageConsumer> wrapper =
        consumer -> {
          MessageConsumer result = consumer;
          if (traceMessages != null) {
            result =
                message -> {
                  traceMessages.println(message);
                  traceMessages.flush();
                  consumer.consume(message);
                };
          }
          if (true) {
            result = new ReflectiveMessageValidator(result);
          }
          return result;
        };

    Launcher<IDebugProtocolServer> debugProtocolLauncher =
        DSPLauncher.createClientLauncher(this, in, out, threadPool, wrapper);

    debugProtocolFuture = debugProtocolLauncher.startListening();
    debugProtocolServer = debugProtocolLauncher.getRemoteProxy();

    complete(initialize(action, launchArguments));
    launchArguments.toString();

    //    InitializeRequestArguments initializeRequestArguments = new InitializeRequestArguments();
    //    initializeRequestArguments.setClientID("che");
    //    initializeRequestArguments.setAdapterID(launchArguments.getOrDefault("type",
    // "").toString());
    //    initializeRequestArguments.setPathFormat("path");
    //    capabilities = complete(debugProtocolServer.initialize(initializeRequestArguments));
    //    Object object = launchArguments.get("program");
    //    targetName = Objects.toString(object, "Debug Adapter Target");
    //
    //    complete(debugProtocolServer.launch(launchArguments));
    //
    //    targetBreakpoints.clear();
    //    action.getBreakpoints().forEach(this::addBreakpointToMap);
    //    sendBreakpoints();
    //
    //    complete(debugProtocolServer.configurationDone(new ConfigurationDoneArguments()));
  }

  private CompletableFuture<Void> initialize(
      StartAction action, Map<String, Object> dspParameters) {
    InitializeRequestArguments arguments = new InitializeRequestArguments();
    arguments.setClientID("che");
    arguments.setAdapterID((String) dspParameters.get("type"));
    arguments.setPathFormat("path");
    arguments.setSupportsVariableType(true);
    arguments.setSupportsVariablePaging(true);
    arguments.setLinesStartAt1(true);
    arguments.setColumnsStartAt1(true);
    arguments.setSupportsRunInTerminalRequest(true);
    targetName = Objects.toString(dspParameters.get("program"), "Debug Adapter Target");

    CompletableFuture<Void> launchFuture =
        debugProtocolServer
            .initialize(arguments)
            .thenAccept(
                (Capabilities capabilities) -> {
                  this.capabilities = capabilities;
                })
            .thenCompose(
                (v) -> {
                  if ("launch".equals(dspParameters.getOrDefault("request", "launch"))) {
                    return debugProtocolServer.launch(dspParameters);
                  } else {
                    return debugProtocolServer.attach(dspParameters);
                  }
                });
    CompletableFuture<Void> configurationDoneFuture =
        initialized
            .thenCompose(
                (v) -> {
                  return breakpointManager.initialize(
                      debugProtocolServer, debuggerCallback, capabilities, action.getBreakpoints());
                })
            .thenCompose(
                (v) -> {
                  if (Boolean.TRUE.equals(capabilities.getSupportsConfigurationDoneRequest())) {
                    return debugProtocolServer.configurationDone(new ConfigurationDoneArguments());
                  }
                  return CompletableFuture.completedFuture(null);
                });
    return CompletableFuture.allOf(launchFuture, configurationDoneFuture);
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

  private Long getCurrentThreadId() {
    return currentThreadId;
  }

  private Location getCurrentLocation() {
    Thread thread = getCurrentThread();
    StackFrame frame = getTopStackFrame(thread);

    return getLocation(frame);
  }

  private Location getLocation(StackFrame frame) {
    Optional<Location> location =
        locationHelper.toLocation(frame.getSource(), frame.getLine().intValue(), frame.getName());

    return location.orElseGet(
        () -> new LocationImpl(frame.getSource().getName(), frame.getLine().intValue()));
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
    StackTraceArguments stackTraceArguments = new StackTraceArguments();
    stackTraceArguments.setThreadId(thread.getId());
    stackTraceArguments.setStartFrame(0L);
    stackTraceArguments.setLevels(10L);
    CompletableFuture<StackTraceResponse> future =
        debugProtocolServer.stackTrace(stackTraceArguments);
    StackTraceResponse framesBody;
    try {
      framesBody = future.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Can't get frames", e);
    }
    if (framesBody.getStackFrames() == null || framesBody.getStackFrames().length == 0) {
      throw new RuntimeException("frames empty");
    }
    StackFrame[] stackFrames = framesBody.getStackFrames();
    return stackFrames;
  }

  private Thread getCurrentThread() {
    Thread[] threads = getThreads();
    Long currentThreadId = getCurrentThreadId();
    long resolvedId;
    if (currentThreadId == null) {
      resolvedId = 0;
    } else {
      resolvedId = currentThreadId;
      if (resolvedId < 0 || resolvedId >= threads.length) {
        resolvedId = 0;
      }
    }
    return threads[0 /* TODO: Bad assumption that thread ids are sequential!! */];
  }

  private Thread[] getThreads() {
    CompletableFuture<ThreadsResponse> threadsFuture = debugProtocolServer.threads();
    ThreadsResponse threadsBody;
    try {
      threadsBody = threadsFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Can't get threads", e);
    }
    if (threadsBody.getThreads() == null || threadsBody.getThreads().length == 0) {
      throw new RuntimeException("threads empty");
    }
    return threadsBody.getThreads();
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
        long id = thread.getId() == null ? 0 : thread.getId() - 1;
        String name = thread.getName();
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
    DisconnectArguments disconnectArguments = new DisconnectArguments();
    disconnectArguments.setTerminateDebuggee(true);
    complete(debugProtocolServer.disconnect(disconnectArguments));
  }

  @Override
  public void terminated(TerminatedEventArguments body) {
    threadPool.submit(() -> debuggerCallback.onEvent(new DisconnectEventImpl()));
  }

  @Override
  public void addBreakpoint(Breakpoint breakpoint) throws DebuggerException {
    breakpointManager.addBreakpoint(breakpoint);
  }

  @Override
  public void deleteBreakpoint(Location location) throws DebuggerException {
    breakpointManager.deleteBreakpoint(location);
  }

  @Override
  public void deleteAllBreakpoints() throws DebuggerException {
    breakpointManager.deleteAllBreakpoints();
  }

  @Override
  public List<Breakpoint> getAllBreakpoints() throws DebuggerException {
    return breakpointManager.getAllBreakpoints();
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

      long variablesReference;
      try {
        variablesReference = Long.parseLong(string);
      } catch (NumberFormatException e) {
        throw new DebuggerException("Malformed request, variablePath invalid: " + path, e);
      }
      VariablesArguments variablesArguments = new VariablesArguments();
      variablesArguments.setVariablesReference(variablesReference);
      VariablesResponse variablesResponse =
          complete(debugProtocolServer.variables(variablesArguments));

      List<Variable> variables = new ArrayList<>();
      for (org.eclipse.lsp4j.debug.Variable variable : variablesResponse.getVariables()) {
        List<String> list = new ArrayList<>(path);
        boolean primitive;
        if (variable.getVariablesReference() == 0L) {
          primitive = true;
          primitiveVariableId++;
          list.add("primitive" + Long.toString(primitiveVariableId));
        } else {
          primitive = false;
          list.add(Long.toString(variable.getVariablesReference()));
        }
        ImmutableVariablePath childVariablePath = new ImmutableVariablePath(list);
        VariableImpl variableImpl =
            new VariableImpl(
                variable.getType(),
                variable.getName(),
                new SimpleValueImpl(variable.getValue()),
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
  public void stepOver(StepOverAction action) throws DebuggerException {
    NextArguments nextArguments = new NextArguments();
    nextArguments.setThreadId(getCurrentThreadId());
    complete(debugProtocolServer.next(nextArguments));
  }

  @Override
  public void stepInto(StepIntoAction action) throws DebuggerException {
    StepInArguments stepInArguments = new StepInArguments();
    stepInArguments.setThreadId(getCurrentThreadId());
    complete(debugProtocolServer.stepIn(stepInArguments));
  }

  @Override
  public void stepOut(StepOutAction action) throws DebuggerException {
    StepOutArguments stepOutArguments = new StepOutArguments();
    stepOutArguments.setThreadId(getCurrentThreadId());
    complete(debugProtocolServer.stepOut(stepOutArguments));
  }

  @Override
  public void resume(ResumeAction action) throws DebuggerException {
    ContinueArguments continueArguments = new ContinueArguments();
    continueArguments.setThreadId(getCurrentThreadId());
    complete(debugProtocolServer.continue_(continueArguments));
  }

  @Override
  public StackFrameDump dumpStackFrame() throws DebuggerException {
    return getStackFrameDump(getCurrentThreadId(), 0);
  }

  @Override
  public StackFrameDump getStackFrameDump(long threadId, int frameIndex) throws DebuggerException {
    ScopesArguments scopesArguments = new ScopesArguments();
    scopesArguments.setFrameId((long) frameIndex);
    ScopesResponse scopesResponse = complete(debugProtocolServer.scopes(scopesArguments));
    List<Variable> variables = new ArrayList<>();
    for (Scope scope : scopesResponse.getScopes()) {
      ImmutableVariablePath variablePath =
          new ImmutableVariablePath(Long.toString(scope.getVariablesReference()));
      VariableImpl variableImpl =
          new VariableImpl(
              scope.getName(),
              scope.getName(),
              new SimpleValueImpl(scope.getName()),
              false,
              variablePath);
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

  @Override
  public void initialized() {
    initialized.complete(null);
  }

  @Override
  public void stopped(StoppedEventArguments body) {
    currentThreadId = body.getThreadId();
    clearThreadsDump();
    variableMap.clear();
    threadPool.submit(
        () ->
            debuggerCallback.onEvent(
                new SuspendEventImpl(getCurrentLocation(), SuspendPolicy.ALL)));
  }

  @Override
  public void continued(ContinuedEventArguments args) {
    // TODO Auto-generated method stub

  }

  @Override
  public void exited(ExitedEventArguments args) {
    // TODO Auto-generated method stub

  }

  @Override
  public void thread(ThreadEventArguments args) {
    // TODO Auto-generated method stub

  }

  @Override
  public void output(OutputEventArguments args) {
    // TODO Auto-generated method stub

  }

  @Override
  public void breakpoint(BreakpointEventArguments args) {
    breakpointManager.breakpoint(args);
  }

  @Override
  public void module(ModuleEventArguments args) {
    // TODO Auto-generated method stub

  }

  @Override
  public void loadedSource(LoadedSourceEventArguments args) {
    // TODO Auto-generated method stub

  }

  @Override
  public void process(ProcessEventArguments args) {
    // TODO Auto-generated method stub

  }
}
