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

import static com.google.gwt.event.dom.client.KeyCodes.KEY_ENTER;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ConsolePanelViewImpl extends BaseView<ConsolePanelView.ActionDelegate>
    implements ConsolePanelView {
  interface VariablesPanelViewImplUiBinder extends UiBinder<Widget, ConsolePanelViewImpl> {}

  @UiField(provided = true)
  Resources coreRes;

  @UiField FlowPanel outputPanel;

  @UiField TextBox inputText;

  @Inject
  public ConsolePanelViewImpl(
      DebuggerResources resources, VariablesPanelViewImplUiBinder uiBinder, Resources coreRes) {
    this.coreRes = coreRes;

    setContentWidget(uiBinder.createAndBindUi(this));
  }

  @UiHandler("inputText")
  void maybeExecuteCommand(KeyUpEvent event) {
    if (event.getNativeKeyCode() == KEY_ENTER) {
      String command = inputText.getText().trim();
      if (!command.isEmpty()) {
        delegate.onExecuteCommand(command);
      }
      inputText.setText("");
    }
  }

  @Override
  public AcceptsOneWidget getOutputConsoleContainer() {
    return new AcceptsOneWidget(){

      @Override
      public void setWidget(IsWidget w) {
        outputPanel.add(w);
      }
    };
  }
}
