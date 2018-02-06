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
package org.eclipse.che.plugin.debugger.ide.debug.dialogs.changevalue;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.impl.SimpleValueImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariableImpl;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.debug.dialogs.DebuggerDialogFactory;
import org.eclipse.che.plugin.debugger.ide.debug.dialogs.common.TextAreaDialogView;
import org.eclipse.che.plugin.debugger.ide.debug.panel.variables.VariablesPanelPresenter;

/**
 * Presenter for changing variables value.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ChangeValuePresenter implements TextAreaDialogView.ActionDelegate {
  private final DebuggerManager debuggerManager;
  private final TextAreaDialogView view;
  private final VariablesPanelPresenter variablesPanelPresenter;
  private final DebuggerLocalizationConstant constant;
  private Variable selectedVariable;

  @Inject
  public ChangeValuePresenter(
      DebuggerDialogFactory dialogFactory,
      DebuggerLocalizationConstant constant,
      DebuggerManager debuggerManager,
      VariablesPanelPresenter variablesPanelPresenter) {
    this.view =
        dialogFactory.createTextAreaDialogView(
            constant.changeValueViewTitle(),
            constant.changeValueViewChangeButtonTitle(),
            constant.changeValueViewCancelButtonTitle());
    this.debuggerManager = debuggerManager;
    this.variablesPanelPresenter = variablesPanelPresenter;
    this.view.setDelegate(this);
    this.constant = constant;
  }

  public void showDialog() {
    this.selectedVariable = variablesPanelPresenter.getSelectedVariable();
    view.setValueTitle(constant.changeValueViewExpressionFieldTitle(selectedVariable.getName()));
    view.setValue(selectedVariable.getValue().getString());
    view.focusInValueField();
    view.selectAllText();
    view.setEnableChangeButton(false);
    view.showDialog();
  }

  @Override
  public void onCancelClicked() {
    view.close();
  }

  @Override
  public void onAgreeClicked() {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (selectedVariable != null && debugger != null && debugger.isSuspended()) {
      Variable newVariable =
          new VariableImpl(
              selectedVariable.getType(),
              selectedVariable.getName(),
              new SimpleValueImpl(view.getValue()),
              selectedVariable.isPrimitive(),
              selectedVariable.getVariablePath());

      final long threadId = debugger.getDebugContextThreadId();
      final int frameIndex = debugger.getDebugContextFrameIndex();
      debugger.setValue(newVariable, threadId, frameIndex);
    }

    view.close();
  }

  @Override
  public void onValueChanged() {
    final String value = view.getValue();
    boolean isExpressionFieldNotEmpty = !value.trim().isEmpty();
    view.setEnableChangeButton(isExpressionFieldNotEmpty);
  }
}
