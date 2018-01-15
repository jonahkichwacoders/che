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

public interface DspLocalizationConstant extends com.google.gwt.i18n.client.Messages {

  @Key("view.dspConfigurationPage.modeLaunchLabel")
  String dspConfigurationPageModeLaunchLabel();

  @Key("view.dspConfigurationPage.modeAttachLabel")
  String dspConfigurationPageModeAttachLabel();

  @Key("view.dspConfigurationPage.commandLabel")
  String dspConfigurationPageViewCommandLabel();

  @Key("view.dspConfigurationPage.argumentsLabel")
  String dspConfigurationPageViewArgumentsLabel();

  @Key("view.dspConfigurationPage.parametersLabel")
  String dspConfigurationPageViewParametersLabel();

  @Key("view.dspConfigurationPage.serverHostLabel")
  String dspConfigurationPageViewServerHostLabel();

  @Key("view.dspConfigurationPage.serverPortLabel")
  String dspConfigurationPageViewServerPortLabel();
}
