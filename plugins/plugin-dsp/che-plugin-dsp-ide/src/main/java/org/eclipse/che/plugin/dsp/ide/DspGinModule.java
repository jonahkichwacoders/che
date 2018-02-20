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

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.plugin.dsp.ide.configuration.DspConfigurationPageView;
import org.eclipse.che.plugin.dsp.ide.configuration.DspConfigurationPageViewImpl;
import org.eclipse.che.plugin.dsp.ide.configuration.DspConfigurationType;
import org.eclipse.che.plugin.dsp.ide.debug.panel.console.ConsolePanelView;
import org.eclipse.che.plugin.dsp.ide.debug.panel.console.ConsolePanelViewImpl;

@ExtensionGinModule
public class DspGinModule extends AbstractGinModule {
  @Override
  protected void configure() {
    GinMultibinder.newSetBinder(binder(), DebugConfigurationType.class)
        .addBinding()
        .to(DspConfigurationType.class);
    bind(DspConfigurationPageView.class).to(DspConfigurationPageViewImpl.class).in(Singleton.class);
    bind(ConsolePanelView.class).to(ConsolePanelViewImpl.class).in(Singleton.class);
  }
}
