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
package org.eclipse.che.plugin.debugger.ide.debug.panel.context;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.ThreadState;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

public interface ContextPanelView extends View<ContextPanelView.ActionDelegate> {

  interface ActionDelegate extends BaseActionDelegate {
    /** Is invoked when a new thread is selected. */
    void onSelectedThread(long threadId);

    /**
     * Is invoked when a new frame is selected.
     *
     * @param frameIndex the frame index inside a thread
     */
    void onSelectedFrame(int frameIndex);
  }

  /**
   * Sets information about the execution point.
   *
   * @param location information about the execution point
   */
  void setExecutionPoint(@NotNull Location location);

  /**
   * Sets thread dump and select the thread with {@link ThreadState#getId()} equal to {@code
   * activeThreadId}.
   */
  void setThreadDump(@NotNull List<? extends ThreadState> threadDump, long threadIdToSelect);

  /** Sets the list of frames for selected thread. */
  void setFrames(@NotNull List<? extends StackFrameDump> stackFrameDumps);

  /** Returns selected thread id {@link ThreadState#getId()} or -1 if there is no selection. */
  long getSelectedThreadId();

  /** Returns selected frame index inside thread or -1 if there is no selection. */
  int getSelectedFrameIndex();

  void setThreadNotSuspendPlaceHolderVisible(boolean visible);
}
