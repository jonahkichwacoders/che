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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.plugin.dsp.ide.DspDebugger;
import org.eclipse.che.plugin.dsp.ide.DspResources;

@Singleton
public class DspConfigurationType implements DebugConfigurationType {

  private final DspConfigurationPagePresenter page;

  @Inject
  public DspConfigurationType(
      DspConfigurationPagePresenter page, IconRegistry iconRegistry, DspResources resources) {
    this.page = page;
    iconRegistry.registerIcon(
        new Icon(
            DspDebugger.ID + ".debug.configuration.type.icon",
            resources.dspDebugConfigurationType()));
  }

  @Override
  public String getId() {
    return DspDebugger.ID;
  }

  @Override
  public String getDisplayName() {
    return DspDebugger.DISPLAY_NAME;
  }

  @Override
  public DebugConfigurationPage<? extends DebugConfiguration> getConfigurationPage() {
    return page;
  }
}
