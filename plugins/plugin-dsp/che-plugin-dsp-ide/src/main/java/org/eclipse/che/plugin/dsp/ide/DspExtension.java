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
package org.eclipse.che.plugin.dsp.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.debug.DebugPartPresenterManager;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.plugin.dsp.ide.debug.panel.console.ConsolePanelPresenter;

/** Extension for debugging any target type that uses Debug Server Protocol */
@Singleton
@Extension(title = DspDebugger.DISPLAY_NAME, version = "1.0.0")
public class DspExtension {

  @Inject
  public DspExtension(
      DebuggerManager debuggerManager,
      DspDebugger dspDebugger,
      DebugPartPresenterManager debugPartPresenterManager,
      ConsolePanelPresenter consolePanelPresenter) {
    debuggerManager.registeredDebugger(DspDebugger.ID, dspDebugger);
    debugPartPresenterManager.registerDebugPartPresenter(consolePanelPresenter);
  }
}
