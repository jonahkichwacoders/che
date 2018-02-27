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

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Date;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.DebugPartPresenter;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.console.CommandConsoleFactory;
import org.eclipse.che.ide.console.CompoundOutputCustomizer;
import org.eclipse.che.ide.console.DefaultOutputConsole;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.debug.DebuggerManagerObserver;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerLocationHandlerManager;
import org.eclipse.che.plugin.dsp.ide.DspLocalizationConstant;

@Singleton
public class ConsolePanelPresenter extends BasePresenter
    implements ConsolePanelView.ActionDelegate,
        OutputConsole.ActionDelegate,
        DebuggerManagerObserver,
        DebugPartPresenter {
  private static final String TITLE = "Console";
  private final DebuggerManager debuggerManager;
  private final DspLocalizationConstant constant;
  private final ConsolePanelView view;
  private StringBuilder outputText = new StringBuilder();
  private DefaultOutputConsole outputConsole;
  private boolean clearInfoMessage = false;

  @Inject
  public ConsolePanelPresenter(
      ConsolePanelView view,
      DspLocalizationConstant constant,
      DebuggerManager debuggerManager,
      DebuggerLocationHandlerManager resourceHandlerManager,
      CommandConsoleFactory consoleFactory,
      AppContext appContext,
      EditorAgent editorAgent) {
    this.view = view;
    this.constant = constant;
    this.debuggerManager = debuggerManager;
    this.outputConsole = (DefaultOutputConsole) consoleFactory.create("Output");
    this.outputConsole.setCustomizer(new CompoundOutputCustomizer(new GDBPanelOutputCustomizer(appContext, editorAgent)));
    this.outputConsole.addActionDelegate(this);

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
    AcceptsOneWidget outputConsoleContainer = view.getOutputConsoleContainer();
    outputConsole.go(outputConsoleContainer);
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
    if (clearInfoMessage) {
      outputConsole.clearOutputsButtonClicked();
      clearInfoMessage = false;
    }
    outputText.append(text);
    outputConsole.printText(text);
  }

  @Override
  public void onActiveDebuggerChanged(Debugger activeDebugger) {
    outputText = new StringBuilder();
    outputConsole.clearOutputsButtonClicked();
    clearInfoMessage = true;
    if (activeDebugger == null) {
      outputConsole.printText("No active debug session.");
    } else if (activeDebugger.supportsConsole()) {
      outputConsole.printText("Debug session started. Output will appear here.");
    } else {
      outputConsole.printText(
          "Debug session started. Current debugger does not support the console view.");
    }
  }

  @Override
  public void onConsoleOutput(OutputConsole console) {}

  @Override
  public void onDownloadOutput(OutputConsole console) {
    download("debug.log", outputText.toString());
  }

  /**
   * Invokes the browser to download a file.
   *
   * @param fileName file name
   * @param text file content
   */
  private native void download(String fileName, String text) /*-{
		var element = $doc.createElement('a');
		element.setAttribute('href', 'data:text/plain;charset=utf-8,'
				+ encodeURIComponent(text));
		element.setAttribute('download', fileName);

		element.style.display = 'none';
		$doc.body.appendChild(element);

		element.click();

		$doc.body.removeChild(element);
  }-*/;

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
