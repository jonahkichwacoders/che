/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.debugger.ide.debug.panel.context;

import static java.util.Collections.emptyList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.che.api.debug.shared.dto.ThreadStateDto;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.ThreadState;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.ide.api.debug.DebugPartPresenter;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.debug.DebuggerManagerObserver;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerLocationHandler;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerLocationHandlerManager;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerPresenter;

@Singleton
public class ContextPanelPresenter extends BasePresenter
    implements ContextPanelView.ActionDelegate,
        DebuggerManagerObserver,
        WorkspaceStoppedEvent.Handler,
        DebugPartPresenter {
  private final DebuggerManager debuggerManager;
  private final DebuggerLocationHandlerManager resourceHandlerManager;
  private final ContextPanelView view;

  private Map<Long, ? extends ThreadState> threadDump;
  private Location executionPoint;
  private DebuggerLocalizationConstant locale;

  @Inject
  public ContextPanelPresenter(
      final ContextPanelView view,
      final EventBus eventBus,
      final DebuggerManager debuggerManager,
      final DebuggerLocalizationConstant locale,
      final DebuggerLocationHandlerManager resourceHandlerManager) {
    this.view = view;
    this.debuggerManager = debuggerManager;
    this.locale = locale;
    this.resourceHandlerManager = resourceHandlerManager;

    this.view.setDelegate(this);

    eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);
    this.debuggerManager.addObserver(this);

    this.threadDump = new HashMap<>();
    clearView();
  }

  @Override
  public String getTitle() {
    return locale.debuggerFramesTitle();
  }

  @Override
  public IsWidget getView() {
    return view;
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
  }

  @Override
  public String getTitleToolTip() {
    return locale.debuggerFramesTitle();
  }

  private void clearView() {
    threadDump = new HashMap<>();
    executionPoint = null;
    view.setExecutionPoint(null);
    view.setThreadDump(emptyList(), -1);
    view.setFrames(emptyList());
    view.setThreadNotSuspendPlaceHolderVisible(false);
  }

  @Override
  public void onSelectedThread(long threadId) {
    refreshView(threadId);
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null) {
      debugger.setDebugContext(threadId, 0);
    }
  }

  private void open(Location location) {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null) {
      DebuggerLocationHandler handler = resourceHandlerManager.getOrDefault(location);

      handler.open(
          location,
          new AsyncCallback<VirtualFile>() {
            @Override
            public void onFailure(Throwable caught) {}

            @Override
            public void onSuccess(VirtualFile result) {}
          });
    }
  }

  @Override
  public void onSelectedFrame(int frameIndex) {
    long threadId = view.getSelectedThreadId();

    ThreadState threadState = threadDump.get(threadId);
    if (threadState != null) {
      List<? extends StackFrameDump> frames = threadState.getFrames();
      if (frames.size() > frameIndex) {
        open(frames.get(frameIndex).getLocation());
      }
    }
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null) {
      debugger.setDebugContext(threadId, frameIndex);
    }
  }

  protected void refreshView() {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null && debugger.isSuspended()) {
      debugger
          .getThreadDump()
          .then(
              threadDump -> {
                ContextPanelPresenter.this.threadDump =
                    threadDump.stream().collect(Collectors.toMap(ThreadStateDto::getId, ts -> ts));

                if (executionPoint != null) {
                  view.setThreadDump(threadDump, executionPoint.getThreadId());
                  refreshView(executionPoint.getThreadId());
                }
              })
          .catchError(
              error -> {
                Log.error(DebuggerPresenter.class, error.getCause());
              });
    }
  }

  protected void refreshView(long threadId) {
    ThreadState threadState = threadDump.get(threadId);
    if (threadState == null) {
      view.setFrames(Collections.emptyList());
      view.setThreadNotSuspendPlaceHolderVisible(false);
      return;
    }

    view.setThreadNotSuspendPlaceHolderVisible(!threadState.isSuspended());

    if (threadState.isSuspended()) {
      List<? extends StackFrameDump> frames = threadState.getFrames();
      view.setFrames(frames);
    } else {
      view.setFrames(Collections.emptyList());
    }
  }

  @Override
  public void onDebuggerAttached(final DebuggerDescriptor debuggerDescriptor) {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null) {
      debugger.setDebugContext(-1, -1);
    }
  }

  @Override
  public void onDebuggerDisconnected() {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null) {
      debugger.setDebugContext(-1, -1);
    }
    clearView();
  }

  @Override
  public void onPreStepInto() {
    clearView();
  }

  @Override
  public void onPreStepOut() {
    clearView();
  }

  @Override
  public void onPreStepOver() {
    clearView();
  }

  @Override
  public void onPreResume() {
    clearView();
  }

  @Override
  public void onBreakpointStopped(String filePath, Location location) {
    executionPoint = location;
    view.setExecutionPoint(executionPoint);
    refreshView();
  }

  @Override
  public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
    clearView();
  }

  @Override
  public void onBreakpointAdded(Breakpoint breakpoint) {}

  @Override
  public void onBreakpointActivated(String filePath, int lineNumber) {}

  @Override
  public void onBreakpointDeleted(Breakpoint breakpoint) {}

  @Override
  public void onAllBreakpointsDeleted() {}

  @Override
  public void onValueChanged(Variable variable, long threadId, int frameIndex) {}

  @Override
  public void onActiveDebuggerChanged(Debugger activeDebugger) {}

  @Override
  public void onDebugContextChanged() {
    // TODO update view if something other than this presenter changed the context to begin with.
  }
}
