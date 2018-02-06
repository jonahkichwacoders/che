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

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Provides methods which allow change view representation of debugger panel. Also the interface
 * contains inner action delegate interface which provides methods which allows react on user's
 * actions.
 *
 * @author Andrey Plotnikov
 * @author Dmitry Shnurenko
 * @author Oleksandr Andriienko
 */
public interface DebuggerView extends View<DebuggerView.ActionDelegate> {

  /** Needs for delegate some function into Debugger view. */
  interface ActionDelegate extends BaseActionDelegate {
    void onAddTabButtonClicked(int mouseX, int mouseY);

    void onToggleMaximizeDebugPanel();
  }

  /**
   * Sets java virtual machine name and version.
   *
   * @param name virtual machine name
   */
  void setVMName(@Nullable String name);

  /**
   * Sets title.
   *
   * @param title title of view
   */
  void setTitle(@NotNull String title);

  /** Returns debugger toolbar panel widget. */
  AcceptsOneWidget getDebuggerToolbarPanel();

  void addWidget(String title, SVGResource icon, IsWidget widget, boolean removable);

  void initialState();
}
