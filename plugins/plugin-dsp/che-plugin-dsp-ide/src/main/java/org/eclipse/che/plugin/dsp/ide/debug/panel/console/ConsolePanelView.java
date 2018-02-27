/*
 * Copyright (c) 2017 Kichwa Coders Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Kichwa Coders Ltd. - initial API and implementation
 */
package org.eclipse.che.plugin.dsp.ide.debug.panel.console;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public interface ConsolePanelView extends View<ConsolePanelView.ActionDelegate> {

  interface ActionDelegate extends BaseActionDelegate {
    /** Perform the command the user has typed */
    void onExecuteCommand(String command);
  }

  AcceptsOneWidget getOutputConsoleContainer();
}
