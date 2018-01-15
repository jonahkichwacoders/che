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

import java.util.List;
import org.eclipse.che.ide.api.mvp.View;

public interface DspConfigurationPageView extends View<DspConfigurationPageView.ActionDelegate> {
  void setMode(String string);

  void setCommand(String command);

  void setArguments(List<String> args);

  void setParameters(String parameters);

  void setServerHost(String host);

  void setServerPort(String port);

  String getMode();

  String getCommand();

  List<String> getArguments();

  String getParameters();

  String getServerHost();

  String getServerPort();

  /** Action handler for the view's controls. */
  interface ActionDelegate {

    void onModeChanged();

    void onCommandChanged();

    void onArgumentsChanged();

    void onParametersChanged();

    void onServerHostChanged();

    void onServerPortChanged();
  }
}
