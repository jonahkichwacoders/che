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
package org.eclipse.che.plugin.debugger.ide.debug.panel.breakpoints;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.debug.BreakpointManagerObserver;
import org.eclipse.che.ide.api.debug.DebugPartPresenter;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.debug.DebuggerManagerObserver;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerLocationHandlerManager;
import org.eclipse.che.plugin.debugger.ide.debug.breakpoint.BreakpointContextMenuFactory;
import org.eclipse.che.plugin.debugger.ide.debug.panel.breakpoints.BreakpointsPanelView.ActiveBreakpointWrapper;

@Singleton
public class BreakpointsPanelPresenter extends BasePresenter
    implements BreakpointsPanelView.ActionDelegate,
        DebuggerManagerObserver,
        BreakpointManagerObserver,
        DebugPartPresenter {
  private final BreakpointManager breakpointManager;
  private final DebuggerManager debuggerManager;
  private final BreakpointContextMenuFactory breakpointContextMenuFactory;
  private final DebuggerLocationHandlerManager resourceHandlerManager;
  private final BreakpointsPanelView view;
  private DebuggerLocalizationConstant locale;

  @Inject
  public BreakpointsPanelPresenter(
      final BreakpointsPanelView view,
      final DebuggerManager debuggerManager,
      final BreakpointManager breakpointManager,
      final BreakpointContextMenuFactory breakpointContextMenuFactory,
      final DebuggerLocationHandlerManager resourceHandlerManager,
      final DebuggerLocalizationConstant locale) {
    this.view = view;
    this.debuggerManager = debuggerManager;
    this.breakpointManager = breakpointManager;
    this.breakpointContextMenuFactory = breakpointContextMenuFactory;
    this.resourceHandlerManager = resourceHandlerManager;
    this.locale = locale;

    this.view.setDelegate(this);

    this.debuggerManager.addObserver(this);
    this.breakpointManager.addObserver(this);

    refreshBreakpoints();
  }

  @Override
  public String getTitle() {
    return locale.breakpoints();
  }

  @Override
  public IsWidget getView() {
    return view;
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
    refreshBreakpoints();
  }

  @Override
  public String getTitleToolTip() {
    return locale.breakpoints();
  }

  @Override
  public void onBreakpointContextMenu(int clientX, int clientY, Breakpoint breakpoint) {
    Scheduler.get()
        .scheduleDeferred(
            () -> breakpointContextMenuFactory.newContextMenu(breakpoint).show(clientX, clientY));
  }

  @Override
  public void onBreakpointDoubleClick(Breakpoint breakpoint) {
    Location location = breakpoint.getLocation();
    resourceHandlerManager
        .getOrDefault(location)
        .open(
            location,
            new AsyncCallback<VirtualFile>() {
              @Override
              public void onFailure(Throwable caught) {}

              @Override
              public void onSuccess(VirtualFile result) {}
            });
  }

  public void refreshBreakpoints() {
    List<Breakpoint> breakpoints = new ArrayList<>(breakpointManager.getAll());
    breakpoints.sort(
        (o1, o2) -> {
          Location location1 = o1.getLocation();
          Location location2 = o2.getLocation();
          int compare = location1.getTarget().compareTo(location2.getTarget());
          return (compare == 0 ? location1.getLineNumber() - location2.getLineNumber() : compare);
        });

    view.setBreakpoints(
        breakpoints
            .stream()
            .map(
                breakpoint ->
                    new ActiveBreakpointWrapper(breakpoint, breakpointManager.isActive(breakpoint)))
            .collect(Collectors.toList()));
  }

  @Override
  public void onDebuggerAttached(DebuggerDescriptor debuggerDescriptor) {}

  @Override
  public void onDebuggerDisconnected() {
    refreshBreakpoints();
  }

  @Override
  public void onBreakpointAdded(Breakpoint breakpoint) {
    refreshBreakpoints();
  }

  @Override
  public void onBreakpointActivated(String filePath, int lineNumber) {}

  @Override
  public void onBreakpointDeleted(Breakpoint breakpoint) {
    refreshBreakpoints();
  }

  @Override
  public void onAllBreakpointsDeleted() {
    refreshBreakpoints();
  }

  @Override
  public void onBreakpointUpdated(Breakpoint breakpoint) {
    refreshBreakpoints();
  }

  @Override
  public void onPreStepInto() {}

  @Override
  public void onPreStepOut() {}

  @Override
  public void onPreStepOver() {}

  @Override
  public void onPreResume() {}

  @Override
  public void onBreakpointStopped(String filePath, Location location) {}

  @Override
  public void onValueChanged(Variable variable, long threadId, int frameIndex) {}

  @Override
  public void onActiveDebuggerChanged(Debugger activeDebugger) {}

  @Override
  public void onDebugContextChanged() {}
}
