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
import org.eclipse.che.plugin.dsp.shared.DspConstants;
import org.eclipse.che.plugin.dsp.shared.DspHelper;

/** Presentation for editing the parameters to launch the debug adapter. */
@Singleton
public class DspConfigurationPagePresenter
    implements DspConfigurationPageView.ActionDelegate, DebugConfigurationPage<DebugConfiguration> {

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
    properties.putIfAbsent(DspConstants.ATTR_DSP_MODE, DspConstants.DSP_MODE_LAUNCH);
    properties.putIfAbsent(DspConstants.ATTR_DSP_CMD, "");
    properties.putIfAbsent(DspConstants.ATTR_DSP_N_ARGS, "0");
    properties.putIfAbsent(DspConstants.ATTR_DSP_SERVER_HOST, "127.0.0.1");
    properties.putIfAbsent(DspConstants.ATTR_DSP_SERVER_PORT, "4771");
    properties.putIfAbsent(DspConstants.ATTR_DSP_PARAM, "{\n}");

    originalConnectionProperties = new HashMap<>(properties);
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);

    Map<String, String> connectionProperties = editedConfiguration.getConnectionProperties();

    view.setMode(connectionProperties.get(DspConstants.ATTR_DSP_MODE));
    view.setCommand(connectionProperties.get(DspConstants.ATTR_DSP_CMD));
    view.setArguments(DspHelper.getDspArgs(connectionProperties));
    view.setParameters(connectionProperties.get(DspConstants.ATTR_DSP_PARAM));
    view.setServerHost(connectionProperties.get(DspConstants.ATTR_DSP_SERVER_HOST));
    view.setServerPort(connectionProperties.get(DspConstants.ATTR_DSP_SERVER_PORT));
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
  public void onModeChanged() {
    editedConfiguration.getConnectionProperties().put(DspConstants.ATTR_DSP_MODE, view.getMode());
    listener.onDirtyStateChanged();
  }

  @Override
  public void onCommandChanged() {
    editedConfiguration.getConnectionProperties().put(DspConstants.ATTR_DSP_CMD, view.getCommand());
    listener.onDirtyStateChanged();
  }

  @Override
  public void onArgumentsChanged() {
    DspHelper.setDspArgs(editedConfiguration.getConnectionProperties(), view.getArguments());
    listener.onDirtyStateChanged();
  }

  @Override
  public void onParametersChanged() {
    editedConfiguration
        .getConnectionProperties()
        .put(DspConstants.ATTR_DSP_PARAM, view.getParameters());
    listener.onDirtyStateChanged();
  }

  @Override
  public void onServerHostChanged() {
    editedConfiguration
        .getConnectionProperties()
        .put(DspConstants.ATTR_DSP_SERVER_HOST, view.getServerHost());
    listener.onDirtyStateChanged();
  }

  @Override
  public void onServerPortChanged() {
    editedConfiguration
        .getConnectionProperties()
        .put(DspConstants.ATTR_DSP_SERVER_PORT, view.getServerPort());
    listener.onDirtyStateChanged();
  }
}
