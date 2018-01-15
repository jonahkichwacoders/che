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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.BreakpointConfiguration;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointConfigurationImpl;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.BreakpointActivatedEventImpl;
import org.eclipse.che.api.debugger.server.Debugger.DebuggerCallback;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.lsp4j.debug.BreakpointEventArguments;
import org.eclipse.lsp4j.debug.BreakpointEventArgumentsReason;
import org.eclipse.lsp4j.debug.Capabilities;
import org.eclipse.lsp4j.debug.SetBreakpointsArguments;
import org.eclipse.lsp4j.debug.SetBreakpointsResponse;
import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.SourceBreakpoint;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DspBreakpointManager {
  public static final Logger LOG = LoggerFactory.getLogger(DspBreakpointManager.class);
  @Inject private DspLocationHelper locationHelper;

  private static class TargetBreakpointInfo {
    private Source source;
    private SourceBreakpoint sourceBreakpoint;
    private org.eclipse.lsp4j.debug.Breakpoint breakpoint;

    public TargetBreakpointInfo(Source source, SourceBreakpoint sourceBreakpoint) {
      this.source = source;
      this.sourceBreakpoint = sourceBreakpoint;
    }
  }

  private Map<Source, List<TargetBreakpointInfo>> targetBreakpoints = new HashMap<>();
  private Map<Long, TargetBreakpointInfo> mapIdToInfo = new HashMap<>();
  private IDebugProtocolServer debugProtocolServer;
  private Capabilities capabilities;
  private DebuggerCallback debugCallback;

  /**
   * Initialize the manager and send all platform breakpoints to the debug adapter.
   *
   * @return the completeable future to signify when the breakpoints are all sent.
   * @throws DebuggerException
   */
  public CompletableFuture<Void> initialize(
      IDebugProtocolServer debugProtocolServer,
      DebuggerCallback debugCallback,
      Capabilities capabilities,
      List<? extends Breakpoint> list) {
    this.debugProtocolServer = debugProtocolServer;
    this.debugCallback = debugCallback;
    this.capabilities = capabilities;
    list.forEach(this::addBreakpointToMap);
    return this.sendBreakpoints();
  }

  /** Called when the debug manager is no longer needed/debug session is shut down. */
  public void shutdown() {}

  private CompletableFuture<Void> sendBreakpoints() {
    List<CompletableFuture<Void>> all = new ArrayList<>();
    // iterate safely in a copy of the keys because sendBreakpoints(Source) modifies the map
    ArrayList<Source> sources = new ArrayList<>(targetBreakpoints.keySet());
    for (Source source : sources) {
      CompletableFuture<Void> future = sendBreakpoints(source);
      all.add(future);
    }
    return CompletableFuture.allOf(all.toArray(new CompletableFuture[all.size()]));
  }

  /**
   * Send the breakpoints for the given source, removing the source from the map if there are no
   * breakpoints left to track for that source.
   *
   * @param source
   * @return
   */
  private CompletableFuture<Void> sendBreakpoints(Source source) {
    List<TargetBreakpointInfo> bps =
        targetBreakpoints.getOrDefault(source, Collections.emptyList());
    Long[] lines = bps.stream().map(tbi -> tbi.sourceBreakpoint.getLine()).toArray(Long[]::new);
    SourceBreakpoint[] sourceBps =
        bps.stream().map(tbi -> tbi.sourceBreakpoint).toArray(SourceBreakpoint[]::new);

    SetBreakpointsArguments arguments = new SetBreakpointsArguments();
    arguments.setSource(source);
    arguments.setLines(lines);
    arguments.setBreakpoints(sourceBps);
    arguments.setSourceModified(false);
    return debugProtocolServer
        .setBreakpoints(arguments)
        .thenAccept(
            (SetBreakpointsResponse bpResponse) -> {
              if (bps.isEmpty()) {
                // We can stop tracking this source now
                targetBreakpoints.remove(source);
              }
              org.eclipse.lsp4j.debug.Breakpoint[] installedBreakpoints =
                  bpResponse.getBreakpoints();
              if (installedBreakpoints.length != bps.size()) {
                LOG.error(
                    "SetBreakpointsResponse size did not match SetBreakpointsArguments size: {} {}",
                    bpResponse,
                    arguments);
              }
              for (int i = 0; i < Math.min(bps.size(), installedBreakpoints.length); i++) {
                TargetBreakpointInfo targetBreakpointInfo = bps.get(i);
                targetBreakpointInfo.breakpoint = installedBreakpoints[i];
                Long id = installedBreakpoints[i].getId();
                if (id != null) {
                  mapIdToInfo.put(id, targetBreakpointInfo);
                }
                if (installedBreakpoints[i].getVerified()) {
                  toPlatformBreakpoint(targetBreakpointInfo)
                      .ifPresent(bp -> debugCallback.onEvent(new BreakpointActivatedEventImpl(bp)));
                }
              }
            })
        .exceptionally(
            t -> {
              t.toString();
              return null;
            });
  }

  public Source addBreakpointToMap(Breakpoint breakpoint) {
    Location location = breakpoint.getLocation();
    Source source = locationHelper.toSource(location);
    List<TargetBreakpointInfo> targetBreakpointInfos =
        targetBreakpoints.computeIfAbsent(source, (k) -> new ArrayList<>());
    SourceBreakpoint sourceBreakpoint = new SourceBreakpoint();
    sourceBreakpoint.setLine((long) location.getLineNumber());
    BreakpointConfiguration breakpointConfiguration = breakpoint.getBreakpointConfiguration();
    if (breakpointConfiguration.isConditionEnabled()) {
      if (capabilities.getSupportsConditionalBreakpoints()) {
        sourceBreakpoint.setCondition(breakpointConfiguration.getCondition());
      } else {
        // TODO how to report to user in UI that !capabilities.getSupportsConditionalBreakpoints()?
        LOG.warn("Debug adapter does not support conditional breakpoints");
      }
    }
    targetBreakpointInfos.add(new TargetBreakpointInfo(source, sourceBreakpoint));
    return source;
  }

  private Source removeBreakpointFromMap(Location location) {
    Source source = locationHelper.toSource(location);
    List<TargetBreakpointInfo> targetBreakpointInfos = targetBreakpoints.get(source);
    for (Iterator<TargetBreakpointInfo> iterator = targetBreakpointInfos.iterator();
        iterator.hasNext(); ) {
      TargetBreakpointInfo targetBreakpointInfo = iterator.next();
      SourceBreakpoint sourceBreakpoint = targetBreakpointInfo.sourceBreakpoint;
      if (Objects.equals((long) location.getLineNumber(), sourceBreakpoint.getLine())) {
        iterator.remove();
        if (targetBreakpointInfo.breakpoint != null) {
          Long id = targetBreakpointInfo.breakpoint.getId();
          if (id != null) {
            mapIdToInfo.remove(id, targetBreakpointInfo);
          }
        }
      }
    }
    return source;
  }

  public void addBreakpoint(Breakpoint breakpoint) throws DebuggerException {
    Source source = addBreakpointToMap(breakpoint);
    sendBreakpoints(source);
  }

  public void deleteBreakpoint(Location location) throws DebuggerException {
    Source source = removeBreakpointFromMap(location);
    sendBreakpoints(source);
  }

  public void deleteAllBreakpoints() throws DebuggerException {
    Set<Entry<Source, List<TargetBreakpointInfo>>> entrySet = targetBreakpoints.entrySet();
    for (Entry<Source, List<TargetBreakpointInfo>> entry : entrySet) {
      entry.getValue().clear();
    }
    sendBreakpoints();
  }

  public List<Breakpoint> getAllBreakpoints() throws DebuggerException {
    List<Breakpoint> breakpoints = new ArrayList<>();
    for (List<TargetBreakpointInfo> targetBreakpointInfos : targetBreakpoints.values()) {
      for (TargetBreakpointInfo targetBreakpointInfo : targetBreakpointInfos) {
        toPlatformBreakpoint(targetBreakpointInfo).ifPresent(breakpoints::add);
      }
    }
    return breakpoints;
  }

  private Optional<Breakpoint> toPlatformBreakpoint(TargetBreakpointInfo targetBreakpointInfo) {
    return locationHelper
        .toLocation(
            targetBreakpointInfo.source, targetBreakpointInfo.sourceBreakpoint.getLine().intValue())
        .map(
            location -> {
              String condition = targetBreakpointInfo.sourceBreakpoint.getCondition();
              BreakpointConfiguration breakpointConfiguration;
              if (condition == null) {
                breakpointConfiguration = new BreakpointConfigurationImpl();
              } else {
                breakpointConfiguration = new BreakpointConfigurationImpl(condition);
              }
              return new BreakpointImpl(location, true, breakpointConfiguration);
            });
  }

  public void breakpoint(BreakpointEventArguments args) {
    switch (args.getReason()) {
      case BreakpointEventArgumentsReason.CHANGED:
        org.eclipse.lsp4j.debug.Breakpoint breakpoint = args.getBreakpoint();
        Long id = breakpoint.getId();
        if (id != null) {
          TargetBreakpointInfo targetBreakpointInfo = mapIdToInfo.get(id);
          if (targetBreakpointInfo != null) {
            if (!targetBreakpointInfo.breakpoint.getVerified() && breakpoint.getVerified()) {
              toPlatformBreakpoint(targetBreakpointInfo)
                  .ifPresent(bp -> debugCallback.onEvent(new BreakpointActivatedEventImpl(bp)));
            }
            // update with the new info
            targetBreakpointInfo.breakpoint = breakpoint;
          }
        }
        break;
      case BreakpointEventArgumentsReason.NEW:
        // No way to indicate to UI that backend created breakpoint
      case BreakpointEventArgumentsReason.REMOVED:
        // No way to indicate to UI that backend removed breakpoint
      default:
        LOG.error("Unsupported breakpoint event from debug server: {}", args);
    }
  }
}
