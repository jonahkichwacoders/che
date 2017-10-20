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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class DspConfigurationPageViewImpl implements DspConfigurationPageView {

  interface DspConfigurationPageViewImplUiBinder
      extends UiBinder<FlowPanel, DspConfigurationPageViewImpl> {}

  private static final DspConfigurationPageViewImplUiBinder UI_BINDER =
      GWT.create(DspConfigurationPageViewImplUiBinder.class);

  private final FlowPanel rootElement;

  @UiField TextBox command;
  @UiField TextBox argument;
  @UiField TextArea parameters;

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
  public String getCommand() {
    return command.getValue();
  }

  @Override
  public void setCommand(String command) {
    this.command.setValue(command);
  }

  @Override
  public void setArgument(String argument) {
    this.argument.setValue(argument);
  }

  @Override
  public void setParameters(String parameters) {
    this.parameters.setValue(parameters);
  }

  @Override
  public String getArgument() {
    return this.parameters.getValue();
  }

  @Override
  public String getParameters() {
    return this.parameters.getValue();
  }

  @UiHandler({"command"})
  void onCommandKeyUp(KeyUpEvent event) {
    delegate.onCommandChanged();
  }

  @UiHandler({"argument"})
  void onArgumentKeyUp(KeyUpEvent event) {
    delegate.onArgumentChanged();
  }

  @UiHandler({"parameters"})
  void onParametersKeyUp(KeyUpEvent event) {
    delegate.onParametersChanged();
  }
}
