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

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import elemental.dom.Element;
import elemental.html.TableElement;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.debug.BreakpointResources;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.eclipse.che.ide.util.dom.Elements;

public class BreakpointsPanelViewImpl extends BaseView<BreakpointsPanelView.ActionDelegate>
    implements BreakpointsPanelView {
  interface BreakpointsPanelViewImplUiBinder extends UiBinder<Widget, BreakpointsPanelViewImpl> {}

  @UiField(provided = true)
  Resources coreRes;

  @UiField ScrollPanel breakpointsPanel;
  private final SimpleList<ActiveBreakpointWrapper> breakpoints;
  private final BreakpointResources breakpointResources;

  @Inject
  public BreakpointsPanelViewImpl(
      BreakpointsPanelViewImplUiBinder uiBinder,
      Resources coreRes,
      BreakpointResources breakpointResources) {
    this.coreRes = coreRes;
    this.breakpointResources = breakpointResources;
    setContentWidget(uiBinder.createAndBindUi(this));

    this.breakpoints = createBreakpointList();
    this.breakpointsPanel.add(breakpoints);
  }

  private SimpleList<ActiveBreakpointWrapper> createBreakpointList() {
    TableElement breakPointsElement = Elements.createTableElement();
    breakPointsElement.setAttribute("style", "width: 100%");

    SimpleList.ListEventDelegate<ActiveBreakpointWrapper> breakpointListEventDelegate =
        new SimpleList.ListEventDelegate<ActiveBreakpointWrapper>() {
          public void onListItemClicked(Element itemElement, ActiveBreakpointWrapper itemData) {
            breakpoints.getSelectionModel().setSelectedItem(itemData);
          }

          @Override
          public void onListItemContextMenu(
              int clientX, int clientY, ActiveBreakpointWrapper itemData) {
            delegate.onBreakpointContextMenu(clientX, clientY, itemData.getBreakpoint());
          }

          @Override
          public void onListItemDoubleClicked(
              Element listItemBase, ActiveBreakpointWrapper itemData) {
            delegate.onBreakpointDoubleClick(itemData.getBreakpoint());
          }
        };

    return SimpleList.create(
        (SimpleList.View) breakPointsElement,
        coreRes.defaultSimpleListCss(),
        new BreakpointItemRender(breakpointResources),
        breakpointListEventDelegate);
  }

  @Override
  public void setBreakpoints(@NotNull List<ActiveBreakpointWrapper> breakpoints) {
    this.breakpoints.render(breakpoints);
  }
}
