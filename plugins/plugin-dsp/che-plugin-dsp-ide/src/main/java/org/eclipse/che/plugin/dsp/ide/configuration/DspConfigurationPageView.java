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
package org.eclipse.che.plugin.dsp.ide.configuration;

import org.eclipse.che.ide.api.mvp.View;

public interface DspConfigurationPageView extends View<DspConfigurationPageView.ActionDelegate> {

  void setCommand(String command);

  void setArgument(String argument);

  void setParameters(String parameters);

  String getCommand();

  String getArgument();

  String getParameters();

  /** Action handler for the view's controls. */
  interface ActionDelegate {

    void onCommandChanged();

    void onArgumentChanged();

    void onParametersChanged();
  }
}
