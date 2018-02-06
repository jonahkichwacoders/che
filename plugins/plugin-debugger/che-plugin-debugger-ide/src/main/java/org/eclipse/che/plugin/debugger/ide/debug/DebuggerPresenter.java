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

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.debug.DebugPartPresenter;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.ActivePartChangedHandler;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.debug.DebuggerManagerObserver;
import org.eclipse.che.ide.part.perspectives.project.ProjectPerspective;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
import org.eclipse.che.plugin.debugger.ide.debug.OpenDebugViewEvent.OpenDebugViewHandler;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * The presenter provides debugging applications.
 *
 * @author Vitaly Parfonov
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 * @author Dmitry Shnurenko
 * @author Anatoliy Bazko
 * @author Mykola Morhun
 * @author Oleksandr Andriienko
 */
@Singleton
public class DebuggerPresenter extends BasePresenter
    implements DebuggerView.ActionDelegate,
        DebuggerManagerObserver,
        WorkspaceStoppedEvent.Handler,
        ActivePartChangedHandler,
        OpenDebugViewHandler {
  private static final String TITLE = "Debug";

  private final DebuggerResources debuggerResources;
  private final ToolbarPresenter debuggerToolbar;
  private final NotificationManager notificationManager;
  private final DebuggerLocalizationConstant constant;
  private final DebuggerView view;
  private final DebuggerManager debuggerManager;
  private final WorkspaceAgent workspaceAgent;
  private final DebuggerLocationHandlerManager resourceHandlerManager;
  private final AddDebugPanelMenuFactory addDebugPanelMenuFactory;

  private DebuggerDescriptor debuggerDescriptor;

  private boolean firstTime = true;

  @Inject
  public DebuggerPresenter(
      final DebuggerView view,
      final DebuggerLocalizationConstant constant,
      final NotificationManager notificationManager,
      final DebuggerResources debuggerResources,
      final @DebuggerToolbar ToolbarPresenter debuggerToolbar,
      final DebuggerManager debuggerManager,
      final WorkspaceAgent workspaceAgent,
      final DebuggerLocationHandlerManager resourceHandlerManager,
      final EventBus eventBus,
      final AddDebugPanelMenuFactory addDebugPanelMenuFactory) {
    this.view = view;
    this.debuggerResources = debuggerResources;
    this.debuggerToolbar = debuggerToolbar;
    this.debuggerManager = debuggerManager;
    this.workspaceAgent = workspaceAgent;
    this.resourceHandlerManager = resourceHandlerManager;
    this.addDebugPanelMenuFactory = addDebugPanelMenuFactory;

    this.view.setDelegate(this);
    this.view.setTitle(TITLE);
    this.constant = constant;

    this.notificationManager = notificationManager;
    this.addRule(ProjectPerspective.PROJECT_PERSPECTIVE_ID);

    this.debuggerManager.addObserver(this);

    eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);
    eventBus.addHandler(OpenDebugViewEvent.TYPE, this);
    eventBus.addHandler(ActivePartChangedEvent.TYPE, this);

    clearView();
    addDebuggerPanel();
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
  public SVGResource getTitleImage() {
    return debuggerResources.debug();
  }

  @Override
  public String getTitleToolTip() {
    return TITLE;
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
    debuggerToolbar.go(view.getDebuggerToolbarPanel());
  }

  public ToolbarPresenter getDebuggerToolbar() {
    return debuggerToolbar;
  }

  @Override
  public void onDebuggerAttached(final DebuggerDescriptor debuggerDescriptor) {
    this.debuggerDescriptor = debuggerDescriptor;
    view.setVMName(debuggerDescriptor.getInfo());
    showDebuggerPanel();
  }

  @Override
  public void onDebuggerDisconnected() {
    String address = debuggerDescriptor != null ? debuggerDescriptor.getAddress() : "";
    String content = constant.debuggerDisconnectedDescription(address);

    notificationManager.notify(
        constant.debuggerDisconnectedTitle(), content, SUCCESS, NOT_EMERGE_MODE);

    clearView();
  }

  @Override
  public void onValueChanged(Variable variable, long threadId, int frameIndex) {}

  private void clearView() {
    debuggerDescriptor = null;
  }

  @Override
  public void onBreakpointStopped(String filePath, Location location) {}

  @Override
  public void onActiveDebuggerChanged(@Nullable Debugger activeDebugger) {}

  private void addDebuggerPanel() {
    if (partStack == null || !partStack.containsPart(this)) {
      workspaceAgent.openPart(this, PartStackType.INFORMATION);
    }
  }

  public void showDebuggerPanel() {
    partStack.setActivePart(this);
  }

  public void hideDebuggerPanel() {
    partStack.minimize();
  }

  public boolean isDebuggerPanelOpened() {
    return partStack.getActivePart() == this;
  }

  @Override
  public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
    clearView();
  }

  @Override
  public void onAddTabButtonClicked(int mouseX, int mouseY) {
    Scheduler.get()
        .scheduleDeferred(
            new Scheduler.ScheduledCommand() {
              @Override
              public void execute() {
                addDebugPanelMenuFactory.newDebugPanelTabMenu().show(mouseX, mouseY);
              }
            });
  }

  @Override
  public void onToggleMaximizeDebugPanel() {
    super.onToggleMaximize();
  }

  @Override
  public void onOpenDebugView(OpenDebugViewEvent event) {
    DebugPartPresenter debugPartPresenter = event.getDebugPartPresenter();
    AcceptsOneWidget container =
        new AcceptsOneWidget() {

          @Override
          public void setWidget(IsWidget w) {
            view.addWidget(
                debugPartPresenter.getTitle(), debugPartPresenter.getTitleImage(), w, true);
          }
        };
    debugPartPresenter.go(container);
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
  public void onPreStepInto() {}

  @Override
  public void onPreStepOut() {}

  @Override
  public void onPreStepOver() {}

  @Override
  public void onPreResume() {}

  public long getSelectedThreadId() {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null) {
      return debugger.getDebugContextThreadId();
    }
    return -1;
  }

  public int getSelectedFrameIndex() {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null) {
      return debugger.getDebugContextFrameIndex();
    }
    return 0;
  }

  @Override
  public void onDebugContextChanged() {}

  @Override
  public void onActivePartChanged(ActivePartChangedEvent event) {
    if (event.getActivePart() == this) {
      if (firstTime) {
        firstTime = false;
        view.initialState();
        //        Scheduler scheduler = Scheduler.get();
        //        scheduler.scheduleDeferred(
        //            () -> {
        //              view.splitVertically();
        //              scheduler.scheduleDeferred(() -> view.splitVertically());
        //            });
      }
    }
  }
}
