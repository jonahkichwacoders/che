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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.plugin.dsp.shared.DspConstants;

public class DspConfigurationPageViewImpl implements DspConfigurationPageView {

  interface DspConfigurationPageViewImplUiBinder
      extends UiBinder<FlowPanel, DspConfigurationPageViewImpl> {}

  private static final DspConfigurationPageViewImplUiBinder UI_BINDER =
      GWT.create(DspConfigurationPageViewImplUiBinder.class);

  private final FlowPanel rootElement;

  @UiField Label commandLabel;
  @UiField TextBox command;
  @UiField Label argumentsLabel;
  // TODO: argument should be storing a list of arguments to the debug server, for now the UI has
  // only been implemented for a single one.
  @UiField TextBox arguments;
  @UiField TextArea parameters;
  @UiField Label serverHostLabel;
  @UiField TextBox serverHost;
  @UiField Label serverPortLabel;
  @UiField TextBox serverPort;
  @UiField RadioButton launch;
  @UiField RadioButton attach;

  private ActionDelegate delegate;

  public DspConfigurationPageViewImpl() {
    rootElement = UI_BINDER.createAndBindUi(this);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public Widget asWidget() {
    return rootElement;
  }

  @Override
  public void setMode(String mode) {
    if (DspConstants.DSP_MODE_CONNECT.equals(mode)) {
      launch.setValue(false);
      attach.setValue(true);
    } else {
      // default to launch mode
      launch.setValue(true);
      attach.setValue(false);
    }
    updateVisibilities();
  }

  @Override
  public void setCommand(String command) {
    this.command.setValue(command);
  }

  @Override
  public void setArguments(List<String> arguments) {
    if (arguments.size() > 1) {
      this.arguments.setValue("TODO handle multiple arguments: " + arguments.toString());
    } else if (arguments.size() == 1) {
      this.arguments.setValue(arguments.get(0));
    } else {
      this.arguments.setValue("");
    }
  }

  @Override
  public void setParameters(String parameters) {
    this.parameters.setValue(parameters);
  }

  @Override
  public void setServerHost(String host) {
    this.serverHost.setValue(host);
  }

  @Override
  public void setServerPort(String port) {
    this.serverPort.setValue(port);
  }

  @Override
  public String getMode() {
    return attach.getValue() ? DspConstants.DSP_MODE_CONNECT : DspConstants.DSP_MODE_LAUNCH;
  }

  @Override
  public String getCommand() {
    return command.getValue();
  }

  @Override
  public List<String> getArguments() {
    String value = this.arguments.getValue();
    if (value.length() > 0) {
      return Arrays.asList(value);
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public String getParameters() {
    return this.parameters.getValue();
  }

  @Override
  public String getServerHost() {
    return this.serverPort.getValue();
  }

  @Override
  public String getServerPort() {
    return this.serverPort.getValue();
  }

  @UiHandler({"command"})
  void onCommandKeyUp(KeyUpEvent event) {
    delegate.onCommandChanged();
  }

  @UiHandler({"arguments"})
  void onArgumentKeyUp(KeyUpEvent event) {
    delegate.onArgumentsChanged();
  }

  @UiHandler({"parameters"})
  void onParametersKeyUp(KeyUpEvent event) {
    delegate.onParametersChanged();
  }

  @UiHandler({"serverHost"})
  void onServerHostKeyUp(KeyUpEvent event) {
    delegate.onServerHostChanged();
  }

  @UiHandler({"serverPort"})
  void onServerPortKeyUp(KeyUpEvent event) {
    delegate.onServerPortChanged();
  }

  @UiHandler({"launch", "attach"})
  void onModeChange(ValueChangeEvent<Boolean> event) {
    updateVisibilities();
    delegate.onModeChanged();
  }

  private void updateVisibilities() {
    boolean launchMode = launch.getValue();
    commandLabel.setVisible(launchMode);
    command.setVisible(launchMode);
    argumentsLabel.setVisible(launchMode);
    arguments.setVisible(launchMode);
    boolean attachMode = attach.getValue();
    serverHostLabel.setVisible(attachMode);
    serverHost.setVisible(attachMode);
    serverPortLabel.setVisible(attachMode);
    serverPort.setVisible(attachMode);
  }
}
