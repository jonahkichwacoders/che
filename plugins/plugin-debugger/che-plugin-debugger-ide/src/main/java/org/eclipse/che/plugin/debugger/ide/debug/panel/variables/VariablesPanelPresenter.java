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
package org.eclipse.che.plugin.debugger.ide.debug.panel.variables;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.MutableVariable;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.WatchExpression;
import org.eclipse.che.api.debug.shared.model.impl.MutableVariableImpl;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.debug.DebugPartPresenter;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.debug.DebuggerManagerObserver;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerLocationHandlerManager;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerPresenter;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerWatchToolBar;

@Singleton
public class VariablesPanelPresenter extends BasePresenter
    implements VariablesPanelView.ActionDelegate, DebuggerManagerObserver, DebugPartPresenter {
  private static final String TITLE = "Variables";
  private final DebuggerManager debuggerManager;
  private final DebuggerLocationHandlerManager resourceHandlerManager;
  private final DebuggerLocalizationConstant constant;
  private final VariablesPanelView view;
  private final ToolbarPresenter watchToolbar;
  private List<Variable> variables;
  private List<WatchExpression> watchExpressions;

  @Inject
  public VariablesPanelPresenter(
      final VariablesPanelView view,
      final DebuggerLocalizationConstant constant,
      final @DebuggerWatchToolBar ToolbarPresenter watchToolbar,
      final DebuggerManager debuggerManager,
      final DebuggerLocationHandlerManager resourceHandlerManager) {
    this.view = view;
    this.constant = constant;
    this.watchToolbar = watchToolbar;
    this.debuggerManager = debuggerManager;
    this.resourceHandlerManager = resourceHandlerManager;

    this.view.setDelegate(this);

    this.debuggerManager.addObserver(this);
    this.watchExpressions = new ArrayList<>();
  }

  @Override
  public String getTitle() {
    return TITLE;
  }

  @Override
  public IsWidget getView() {
    return view;
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
    watchToolbar.go(view.getDebuggerWatchToolbarPanel());
  }

  @Override
  public String getTitleToolTip() {
    return TITLE;
  }

  private boolean isSameSelection(long threadId, int frameIndex) {
    Debugger debugger = debuggerManager.getActiveDebugger();

    if (debugger == null || !debugger.isSuspended()) {
      return false;
    }
    if (debugger.getDebugContextThreadId() != threadId) {
      return false;
    }
    if (debugger.getDebugContextFrameIndex() != frameIndex) {
      return false;
    }
    return true;
  }

  @Override
  public void onExpandVariable(Variable variable) {
    Debugger debugger = debuggerManager.getActiveDebugger();

    if (debugger != null && debugger.isSuspended()) {
      long threadId = debugger.getDebugContextThreadId();
      int frameIndex = debugger.getDebugContextFrameIndex();

      debugger
          .getValue(variable, threadId, frameIndex)
          .then(
              value -> {
                if (isSameSelection(threadId, frameIndex)) {
                  MutableVariable updatedVariable =
                      new MutableVariableImpl(
                          variable.getType(),
                          variable.getName(),
                          value,
                          variable.getVariablePath(),
                          variable.isPrimitive());

                  view.expandVariable(updatedVariable);
                }
              })
          .catchError(
              error -> {
                Log.error(DebuggerPresenter.class, error.getCause());
              });
    }
  }

  @Override
  public void onValueChanged(Variable variable, long threadId, int frameIndex) {
    if (isSameSelection(threadId, frameIndex)) {
      Debugger debugger = debuggerManager.getActiveDebugger();
      if (debugger != null && debugger.isSuspended()) {
        Promise<? extends SimpleValue> promise = debugger.getValue(variable, threadId, frameIndex);
        promise
            .then(
                value -> {
                  MutableVariable updatedVariable =
                      variable instanceof MutableVariable
                          ? ((MutableVariable) variable)
                          : new MutableVariableImpl(variable);
                  updatedVariable.setValue(value);
                  view.updateVariable(updatedVariable);
                })
            .catchError(
                error -> {
                  Log.error(DebuggerPresenter.class, error.getCause());
                });
      }
    }
  }

  @Override
  public void onAddExpressionBtnClicked(WatchExpression expression) {
    watchExpressions.add(expression);
    view.addExpression(expression);

    evaluateWatchExpression(expression);
  }

  @Override
  public void onRemoveExpressionBtnClicked(WatchExpression expression) {
    watchExpressions.remove(expression);
    view.removeExpression(expression);
  }

  @Override
  public void onEditExpressionBtnClicked(WatchExpression expression) {
    expression.setResult("");
    view.updateExpression(expression);

    evaluateWatchExpression(expression);
  }

  protected void refreshVariables() {
    view.removeAllVariables();

    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null && debugger.isSuspended()) {
      long threadId = debugger.getDebugContextThreadId();
      int frameIndex = debugger.getDebugContextFrameIndex();
      Promise<? extends StackFrameDump> promise = debugger.getStackFrameDump(threadId, frameIndex);
      promise
          .then(
              stackFrameDump -> {
                if (isSameSelection(threadId, frameIndex)
                    || debugger.getDebugContextThreadId() == -1) {
                  variables = new LinkedList<>();
                  variables.addAll(stackFrameDump.getFields());
                  variables.addAll(stackFrameDump.getVariables());
                  view.setVariables(variables);
                }
              })
          .catchError(
              error -> {
                Log.error(DebuggerPresenter.class, error.getCause());
              });
    }
  }

  private void refreshView() {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null && debugger.isSuspended()) {
      refreshVariables();
      refreshWatchExpressions();
    } else {
      view.removeAllVariables();
      invalidateExpressions(constant.debuggerThreadNotSuspend());
    }
  }

  private void invalidateExpressions(String result) {
    for (WatchExpression expression : watchExpressions) {
      expression.setResult(result);
      view.updateExpression(expression);
    }
  }

  @Override
  public void onDebugContextChanged() {
    refreshVariables();
    refreshWatchExpressions();
  }

  private void refreshWatchExpressions() {
    for (WatchExpression expression : watchExpressions) {
      expression.setResult("");
      view.updateExpression(expression);
    }

    for (WatchExpression expression : watchExpressions) {
      evaluateWatchExpression(expression);
    }
  }

  private void evaluateWatchExpression(WatchExpression expression) {
    Debugger activeDebugger = debuggerManager.getActiveDebugger();
    if (activeDebugger != null && activeDebugger.isSuspended()) {
      long threadId = activeDebugger.getDebugContextThreadId();
      int frameIndex = activeDebugger.getDebugContextFrameIndex();
      debuggerManager
          .getActiveDebugger()
          .evaluate(expression.getExpression(), threadId, frameIndex)
          .then(
              result -> {
                if (isSameSelection(threadId, frameIndex)) {
                  expression.setResult(result);
                  view.updateExpression(expression);
                }
              })
          .catchError(
              error -> {
                if (isSameSelection(threadId, frameIndex)) {
                  expression.setResult(error.getMessage());
                  view.updateExpression(expression);
                }
              });
    }
  }

  public WatchExpression getSelectedWatchExpression() {
    return view.getSelectedExpression();
  }

  public Variable getSelectedVariable() {
    return view.getSelectedVariable();
  }

  public void clearView() {
    variables = new ArrayList<>();
    view.removeAllVariables();
    invalidateExpressions("");
  }

  public ToolbarPresenter getWatchExpressionToolbar() {
    return watchToolbar;
  }

  @Override
  public void onDebuggerAttached(DebuggerDescriptor debuggerDescriptor) {}

  @Override
  public void onDebuggerDisconnected() {
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
    refreshView();
  }

  @Override
  public void onActiveDebuggerChanged(Debugger activeDebugger) {}
}
