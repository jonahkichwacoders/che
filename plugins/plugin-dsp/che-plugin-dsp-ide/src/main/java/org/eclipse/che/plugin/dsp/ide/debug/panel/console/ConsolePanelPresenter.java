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
package org.eclipse.che.plugin.dsp.ide.debug.panel.console;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.ide.api.debug.DebugPartPresenter;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.debug.DebuggerManagerObserver;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerLocationHandlerManager;
import org.eclipse.che.plugin.dsp.ide.DspLocalizationConstant;

@Singleton
public class ConsolePanelPresenter extends BasePresenter
    implements ConsolePanelView.ActionDelegate, DebuggerManagerObserver, DebugPartPresenter {
  private static final String TITLE = "Console";
  private final DebuggerManager debuggerManager;
  private final DspLocalizationConstant constant;
  private final ConsolePanelView view;
  private StringBuilder outputText = new StringBuilder();

  @Inject
  public ConsolePanelPresenter(
      final ConsolePanelView view,
      final DspLocalizationConstant constant,
      final DebuggerManager debuggerManager,
      final DebuggerLocationHandlerManager resourceHandlerManager) {
    this.view = view;
    this.constant = constant;
    this.debuggerManager = debuggerManager;

    this.view.setDelegate(this);

    this.debuggerManager.addObserver(this);
  }

  @Override
  public String getTitle() {
    return TITLE;
  }

  @Override
  public String getTitleToolTip() {
    return TITLE;
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
  public void onExecuteCommand(String command) {
    Debugger activeDebugger = this.debuggerManager.getActiveDebugger();
    if (activeDebugger != null && activeDebugger.supportsConsole()) {
      if (!command.trim().isEmpty()) {
        activeDebugger.executeCommand(command);
      }
    }

  }

  @Override
  public void onConsole(String text) {
    outputText.append(text);
    view.setOutputText(outputText.toString());
  }

  @Override
  public void onActiveDebuggerChanged(Debugger activeDebugger) {
    outputText = new StringBuilder();
    if (activeDebugger == null) {
      view.setOutputText("No active debug session.");
    } else if (activeDebugger.supportsConsole()) {
      view.setOutputText("Debug session started. Output will appear here.");
    } else {
      view.setOutputText(
          "Debug session started. Current debugger does not support the console view.");
    }
  }

  @Override
  public void onDebuggerAttached(DebuggerDescriptor debuggerDescriptor) {}

  @Override
  public void onDebuggerDisconnected() {}

  @Override
  public void onBreakpointAdded(Breakpoint breakpoint) {}

  @Override
  public void onBreakpointActivated(String filePath, int lineNumber) {}

  @Override
  public void onBreakpointDeleted(Breakpoint breakpoint) {}

  @Override
  public void onAllBreakpointsDeleted() {}

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
  public void onDebugContextChanged() {}
}
