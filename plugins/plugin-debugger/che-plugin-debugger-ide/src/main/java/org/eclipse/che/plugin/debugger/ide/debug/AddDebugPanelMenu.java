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
package org.eclipse.che.plugin.debugger.ide.debug;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Collection;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionGroup;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.Separator;
import org.eclipse.che.ide.api.debug.DebugPartPresenter;
import org.eclipse.che.ide.api.debug.DebugPartPresenterManager;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.menu.ContextMenu;
import org.eclipse.che.plugin.debugger.ide.DebuggerExtension;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;

/** Menu for adding new debug panel. */
public class AddDebugPanelMenu extends ContextMenu {

  private final DebuggerLocalizationConstant debugLocalizationConstant;
  private final DebuggerResources debuggerResources;
  private final EventBus eventBus;
  private final DebugPartPresenterManager debugPartPresenterManager;

  @Inject
  public AddDebugPanelMenu(
      ActionManager actionManager,
      KeyBindingAgent keyBindingAgent,
      Provider<PerspectiveManager> managerProvider,
      DebuggerLocalizationConstant debugLocalizationConstant,
      DebuggerResources debuggerResources,
      DebugPartPresenterManager debugPartPresenterManager,
      EventBus eventBus) {
    super(actionManager, keyBindingAgent, managerProvider);

    this.debugLocalizationConstant = debugLocalizationConstant;
    this.debuggerResources = debuggerResources;
    this.debugPartPresenterManager = debugPartPresenterManager;
    this.eventBus = eventBus;
  }

  /** {@inheritDoc} */
  @Override
  protected String getGroupMenu() {
    return DebuggerExtension.ADD_DEBUG_VIEW_CONTEXT_MENU;
  }

  @Override
  protected ActionGroup updateActions() {
    final DefaultActionGroup actionGroup = new DefaultActionGroup(actionManager);

    Separator separ = new Separator(debugLocalizationConstant.addDebugPanelMenuHeader());
    actionGroup.add(separ);

    Collection<DebugPartPresenter> debugPartPresenters =
        debugPartPresenterManager.getDebugPartPresenters();
    for (DebugPartPresenter debugPartPresenter : debugPartPresenters) {
      OpenDebugViewAction openDebugViewAction = new OpenDebugViewAction(debugPartPresenter);
      actionGroup.add(openDebugViewAction);
    }

    return actionGroup;
  }

  /** Action to display machine output. */
  public class OpenDebugViewAction extends BaseAction {
    private DebugPartPresenter debugPartPresenter;

    public OpenDebugViewAction(DebugPartPresenter debugPartPresenter) {
      super(
          debugPartPresenter.getTitle(),
          debugPartPresenter.getTitleToolTip(),
          debugPartPresenter.getTitleImage());
      this.debugPartPresenter = debugPartPresenter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      eventBus.fireEvent(new OpenDebugViewEvent(debugPartPresenter));
    }
  }
}
