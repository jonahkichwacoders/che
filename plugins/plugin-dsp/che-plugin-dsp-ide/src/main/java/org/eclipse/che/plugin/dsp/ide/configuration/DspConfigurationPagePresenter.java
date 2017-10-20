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

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;

/** Presentation for editing the parameters to launch the debug adapter. */
@Singleton
public class DspConfigurationPagePresenter
    implements DspConfigurationPageView.ActionDelegate, DebugConfigurationPage<DebugConfiguration> {

  public static final String COMMAND_PROPERTY = "COMMAND";
  public static final String ARGUMENT_PROPERTY = "ARGUMENT";
  public static final String PARAMETERS_PROPERTY = "PARAMETERS";
  private static final String DEFAULT_COMMAND = "/scratch/node/node-v6.11.0-linux-x64/bin/node";
  private static final String DEFAULT_ARGUMENT =
      "/home/jonah/.vscode/extensions/andreweinand.mock-debug-0.20.0/out/mockDebug.js";
  private static final String DEFAULT_PARAMETERS =
      "{\r\n"
          + "            \"type\": \"mock\",\r\n"
          + "            \"request\": \"launch\",\r\n"
          + "            \"name\": \"Mock Debug\",\r\n"
          + "            \"program\": \"/scratch/debug/examples/mockdebug/readme.md\",\r\n"
          + "            \"stopOnEntry\": true,\r\n"
          + "            \"trace\": true\r\n"
          + "}";

  private final DspConfigurationPageView view;

  private DebugConfiguration editedConfiguration;
  private Map<String, String> originalConnectionProperties;
  private DirtyStateListener listener;

  @Inject
  public DspConfigurationPagePresenter(DspConfigurationPageView view) {
    this.view = view;

    view.setDelegate(this);
  }

  @Override
  public void resetFrom(DebugConfiguration configuration) {
    editedConfiguration = configuration;
    Map<String, String> properties = configuration.getConnectionProperties();
    properties.putIfAbsent(COMMAND_PROPERTY, DEFAULT_COMMAND);
    properties.putIfAbsent(ARGUMENT_PROPERTY, DEFAULT_ARGUMENT);
    properties.putIfAbsent(PARAMETERS_PROPERTY, DEFAULT_PARAMETERS);

    originalConnectionProperties = new HashMap<>(properties);
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);

    view.setCommand(editedConfiguration.getConnectionProperties().get(COMMAND_PROPERTY));
    view.setArgument(editedConfiguration.getConnectionProperties().get(ARGUMENT_PROPERTY));
    view.setParameters(editedConfiguration.getConnectionProperties().get(PARAMETERS_PROPERTY));
  }

  @Override
  public boolean isDirty() {
    return !editedConfiguration.getConnectionProperties().equals(originalConnectionProperties);
  }

  @Override
  public void setDirtyStateListener(DirtyStateListener listener) {
    this.listener = listener;
  }

  @Override
  public void onCommandChanged() {
    editedConfiguration.getConnectionProperties().put(COMMAND_PROPERTY, view.getCommand());
    listener.onDirtyStateChanged();
  }

  @Override
  public void onArgumentChanged() {
    editedConfiguration.getConnectionProperties().put(ARGUMENT_PROPERTY, view.getArgument());
    listener.onDirtyStateChanged();
  }

  @Override
  public void onParametersChanged() {
    editedConfiguration.getConnectionProperties().put(PARAMETERS_PROPERTY, view.getParameters());
    listener.onDirtyStateChanged();
  }
}
