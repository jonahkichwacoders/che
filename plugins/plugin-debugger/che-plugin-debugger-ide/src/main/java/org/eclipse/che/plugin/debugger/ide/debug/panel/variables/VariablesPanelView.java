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
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.WatchExpression;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

public interface VariablesPanelView extends View<VariablesPanelView.ActionDelegate> {

  interface ActionDelegate extends BaseActionDelegate {
    /**
     * Performs any actions appropriate in response to the user having pressed the expand button in
     * variables tree.
     */
    void onExpandVariable(Variable variable);

    /** Is invoked when a add watch expression button clicked */
    void onAddExpressionBtnClicked(WatchExpression expression);

    /** Is invoked when remove watch expression button clicked. */
    void onRemoveExpressionBtnClicked(WatchExpression expression);

    /** Is invoked when edit watch expression button clicked. */
    void onEditExpressionBtnClicked(WatchExpression expression);
  }

  /** Remove all variables */
  void removeAllVariables();

  /**
   * Set variables.
   *
   * @param variables available variables
   */
  void setVariables(@NotNull List<? extends Variable> variables);

  /** Updates variable in the list */
  void updateVariable(Variable variable);

  /**
   * Expand variable in the debugger tree.
   *
   * @param variable to expand
   */
  void expandVariable(Variable variable);

  /**
   * Returns selected variable on the debugger panel or null if none selected variable.
   *
   * @return selected variable or null otherwise.
   */
  Variable getSelectedVariable();

  /**
   * Returns selected expression on the debugger panel or null if none selected expression.
   *
   * @return selected expression or null otherwise.
   */
  WatchExpression getSelectedExpression();

  /**
   * Add new watch expression.
   *
   * @param expression to add
   */
  void addExpression(WatchExpression expression);

  /**
   * Update new expression.
   *
   * @param expression to update
   */
  void updateExpression(WatchExpression expression);

  /**
   * Remove expression.
   *
   * @param expression to remove
   */
  void removeExpression(WatchExpression expression);

  /** Returns debugger watch toolbar panel widget. */
  AcceptsOneWidget getDebuggerWatchToolbarPanel();
}
