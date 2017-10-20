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

  @Key("view.dspConfigurationPage.commandLabel")
  String dspConfigurationPageViewCommandLabel();

  @Key("view.dspConfigurationPage.argumentLabel")
  String dspConfigurationPageViewArgumentLabel();

  @Key("view.dspConfigurationPage.parametersLabel")
  String dspConfigurationPageViewParametersLabel();
}
