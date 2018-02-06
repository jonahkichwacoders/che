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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.debug.DebugPartPresenter;
import org.eclipse.che.ide.api.debug.DebugPartPresenterManager;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.ui.SplitterFancyUtil;
import org.eclipse.che.ide.ui.multisplitpanel.SubPanel;
import org.eclipse.che.ide.ui.multisplitpanel.SubPanelFactory;
import org.eclipse.che.ide.ui.multisplitpanel.WidgetToShow;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * The class business logic which allow us to change visual representation of debugger panel.
 *
 * @author Andrey Plotnikov
 * @author Dmitry Shnurenko
 * @author Oleksandr Andriienko
 */
@Singleton
public class DebuggerViewImpl extends BaseView<DebuggerView.ActionDelegate>
    implements DebuggerView,
        SubPanel.FocusListener,
        SubPanel.DoubleClickListener,
        SubPanel.AddTabButtonClickListener,
        RequiresResize {

  interface DebuggerViewImplUiBinder extends UiBinder<Widget, DebuggerViewImpl> {}

  @UiField Label vmName;
  @UiField SimplePanel toolbarPanel;

  @UiField(provided = true)
  DebuggerLocalizationConstant locale;

  @UiField(provided = true)
  Resources coreRes;

  @UiField(provided = true)
  SplitLayoutPanel splitPanel;

  private Map<WidgetToShow, SubPanel> widget2Panels;

  private SubPanel focusedSubPanel;
  private Focusable lastFosuced;
  private SubPanel masterSubPanel;
  private DebugPartPresenterManager debugPartPresenterManager;

  @Inject
  protected DebuggerViewImpl(
      DebuggerResources resources,
      DebuggerLocalizationConstant locale,
      Resources coreRes,
      DebuggerViewImplUiBinder uiBinder,
      SplitterFancyUtil splitterFancyUtil,
      SubPanelFactory subPanelFactory,
      DebugPartPresenterManager debugPartPresenterManager) {
    super();

    this.locale = locale;
    this.coreRes = coreRes;
    this.debugPartPresenterManager = debugPartPresenterManager;

    widget2Panels = new HashMap<>();
    splitPanel = new SplitLayoutPanel(1);
    setContentWidget(uiBinder.createAndBindUi(this));

    masterSubPanel = subPanelFactory.newPanel();
    masterSubPanel.setFocusListener(this);
    masterSubPanel.setDoubleClickListener(this);
    masterSubPanel.setAddTabButtonClickListener(this);
    splitPanel.add(masterSubPanel.getView());
    focusedSubPanel = masterSubPanel;
    splitterFancyUtil.tuneSplitter(splitPanel);
  }

  @Override
  public void setVMName(@Nullable String name) {
    vmName.setText(name == null ? "" : name);
  }

  @Override
  public AcceptsOneWidget getDebuggerToolbarPanel() {
    return toolbarPanel;
  }

  @Override
  public void addWidget(
      final String title, final SVGResource icon, final IsWidget widget, final boolean removable) {
    SubPanel subPanel = focusedSubPanel;
    addWidget(subPanel, title, icon, widget, removable);
  }

  private void addWidget(
      SubPanel subPanel,
      final String title,
      final SVGResource icon,
      final IsWidget widget,
      final boolean removable) {
    Optional<WidgetToShow> existingWidgetInSamePanel =
        subPanel
            .getAllWidgets()
            .stream()
            .filter(widgetToShow -> widgetToShow.getWidget().equals(widget))
            .findFirst();

    if (existingWidgetInSamePanel.isPresent()) {
      subPanel.activateWidget(existingWidgetInSamePanel.get());
      return;
    }

    Optional<Entry<WidgetToShow, SubPanel>> existingWidgetInAnyPanel =
        widget2Panels
            .entrySet()
            .stream()
            .filter(e -> e.getKey().getWidget().equals(widget))
            .findFirst();
    existingWidgetInAnyPanel.ifPresent(
        (Entry<WidgetToShow, SubPanel> e) -> {
          SubPanel panel = e.getValue();
          WidgetToShow widgetToShow = e.getKey();
          panel.removeWidget(widgetToShow);
          widget2Panels.remove(widgetToShow);
        });

    final WidgetToShow widgetToShow =
        new WidgetToShow() {
          @Override
          public IsWidget getWidget() {
            return widget;
          }

          @Override
          public String getTitle() {
            return title;
          }

          @Override
          public SVGResource getIcon() {
            return icon;
          }
        };

    widget2Panels.put(widgetToShow, subPanel);

    subPanel.addWidget(
        widgetToShow,
        removable,
        new SubPanel.WidgetRemovingListener() {
          @Override
          public void onWidgetRemoving(SubPanel.RemoveCallback removeCallback) {
            removeCallback.remove();
          }
        });
    subPanel.activateWidget(widgetToShow);
  }

  @Override
  public void focusGained(SubPanel subPanel, IsWidget widget) {
    focusedSubPanel = subPanel;

    if (lastFosuced != null && !lastFosuced.equals(widget)) {
      lastFosuced.setFocus(false);
    }

    if (widget instanceof Focusable) {
      ((Focusable) widget).setFocus(true);

      lastFosuced = (Focusable) widget;
    }
  }

  @Override
  public void onDoubleClicked(final SubPanel panel, final IsWidget widget) {
    delegate.onToggleMaximizeDebugPanel();
  }

  @Override
  public void onResize() {
    for (WidgetToShow widgetToShow : widget2Panels.keySet()) {
      final IsWidget widget = widgetToShow.getWidget();
      if (widget instanceof RequiresResize) {
        ((RequiresResize) widget).onResize();
      }
    }

    for (SubPanel panel : widget2Panels.values()) {
      if (panel.getView() instanceof RequiresResize) {
        ((RequiresResize) panel.getView()).onResize();
      }
    }
  }

  @Override
  public void onAddTabButtonClicked(int mouseX, int mouseY) {
    delegate.onAddTabButtonClicked(mouseX, mouseY);
  }

  @Override
  public void initialState() {
    Collection<DebugPartPresenter> parts =
        debugPartPresenterManager.getInitialDebugPartPresenters();
    int numberOfPanels = parts.size();
    Iterator<DebugPartPresenter> iterator = parts.iterator();
    if (iterator.hasNext()) {
      loadPart(iterator, masterSubPanel, numberOfPanels);
    }
  }

  private void loadPart(
      Iterator<DebugPartPresenter> iterator, SubPanel subPanel, int numberOfPanels) {
    Scheduler scheduler = Scheduler.get();
    scheduler.scheduleDeferred(
        () -> {
          DebugPartPresenter part = iterator.next();
          AcceptsOneWidget container =
              new AcceptsOneWidget() {

                @Override
                public void setWidget(IsWidget w) {
                  addWidget(subPanel, part.getTitle(), part.getTitleImage(), w, true);
                }
              };
          part.go(container);
          if (iterator.hasNext()) {
            double width = (numberOfPanels - 1.0) / numberOfPanels;
            loadPart(iterator, subPanel.splitVertically(width), numberOfPanels - 1);
          }
        });
  }
}
