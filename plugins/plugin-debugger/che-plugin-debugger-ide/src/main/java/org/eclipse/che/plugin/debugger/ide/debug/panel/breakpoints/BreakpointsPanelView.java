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

import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

public interface BreakpointsPanelView extends View<BreakpointsPanelView.ActionDelegate> {

  interface ActionDelegate extends BaseActionDelegate {
    /** Breakpoint context menu is invoked. */
    void onBreakpointContextMenu(int clientX, int clientY, Breakpoint breakpoint);

    void onBreakpointDoubleClick(Breakpoint breakpoint);
  }

  /**
   * Sets breakpoints.
   *
   * @param breakpoints available breakpoints
   */
  void setBreakpoints(@NotNull List<ActiveBreakpointWrapper> breakpoints);

  /*
   * Wraps breakpoint and its state.
   */
  class ActiveBreakpointWrapper {
    private Breakpoint breakpoint;
    private boolean active;

    public ActiveBreakpointWrapper(Breakpoint breakpoint, boolean active) {
      this.breakpoint = breakpoint;
      this.active = active;
    }

    public Breakpoint getBreakpoint() {
      return breakpoint;
    }

    public boolean isActive() {
      return active;
    }
  }
}
