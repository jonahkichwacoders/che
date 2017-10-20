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

import static org.junit.Assert.assertEquals;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.plugin.dsp.ide.DspDebugger;
import org.eclipse.che.plugin.dsp.ide.DspResources;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@RunWith(GwtMockitoTestRunner.class)
public class DspConfigurationTypeTest {

  @Mock private DspResources resources;
  @Mock private DspConfigurationPagePresenter dspConfigurationPagePresenter;
  @Mock private IconRegistry iconRegistry;

  @InjectMocks private DspConfigurationType dspConfigurationType;

  @Test
  public void testGetId() throws Exception {
    final String id = dspConfigurationType.getId();

    Assert.assertEquals(DspDebugger.ID, id);
  }

  @Test
  public void testGetDisplayName() throws Exception {
    final String displayName = dspConfigurationType.getDisplayName();

    assertEquals(DspDebugger.DISPLAY_NAME, displayName);
  }

  @Test
  public void testGetConfigurationPage() throws Exception {
    final DebugConfigurationPage<? extends DebugConfiguration> page =
        dspConfigurationType.getConfigurationPage();

    assertEquals(dspConfigurationPagePresenter, page);
  }
}
