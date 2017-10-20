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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.machine.RecipeServiceClient;
import org.eclipse.che.ide.macro.CurrentProjectPathMacro;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DspConfigurationPagePresenterTest {

  private static final String COMMAND = "/command/to/dsp";
  private static final String ARGUMENT = "argument";
  private static final String PARAMETERS_JSON = "{ \"prop\": 123 }";
  @Mock private DspConfigurationPageView pageView;
  @Mock private DebugConfiguration configuration;
  @Mock private CurrentProjectPathMacro currentProjectPathMacro;
  @Mock private AppContext appContext;
  @Mock private RecipeServiceClient recipeServiceClient;

  @InjectMocks private DspConfigurationPagePresenter pagePresenter;

  @Before
  public void setUp() {
    Map<String, String> testConnectionProperties = new HashMap<>();
    testConnectionProperties.put(DspConfigurationPagePresenter.COMMAND_PROPERTY, COMMAND);
    testConnectionProperties.put(DspConfigurationPagePresenter.ARGUMENT_PROPERTY, ARGUMENT);
    testConnectionProperties.put(
        DspConfigurationPagePresenter.PARAMETERS_PROPERTY, PARAMETERS_JSON);
    when(configuration.getConnectionProperties()).thenReturn(testConnectionProperties);
    pagePresenter.resetFrom(configuration);
  }

  @Test
  public void testResetting() throws Exception {
    verify(configuration, atLeastOnce()).getConnectionProperties();
  }

  @Test
  public void testGo() throws Exception {
    AcceptsOneWidget container = Mockito.mock(AcceptsOneWidget.class);

    pagePresenter.go(container);

    verify(container).setWidget(eq(pageView));
    verify(configuration, atLeastOnce()).getConnectionProperties();
    verify(pageView).setCommand(eq(COMMAND));
    verify(pageView).setArgument(eq(ARGUMENT));
    verify(pageView).setParameters(eq(PARAMETERS_JSON));
  }

  @Test
  public void testOnCommandChanged() throws Exception {
    String command = "/another/command/to/dsp";
    when(pageView.getCommand()).thenReturn(command);

    final DebugConfigurationPage.DirtyStateListener listener =
        mock(DebugConfigurationPage.DirtyStateListener.class);
    pagePresenter.setDirtyStateListener(listener);

    pagePresenter.onCommandChanged();

    verify(pageView).getCommand();
    Assert.assertEquals(
        command,
        configuration
            .getConnectionProperties()
            .get(DspConfigurationPagePresenter.COMMAND_PROPERTY));
    verify(listener).onDirtyStateChanged();
  }

  @Test
  public void testOnArgumentChanged() throws Exception {
    String argument = "another argument";
    when(pageView.getArgument()).thenReturn(argument);

    final DebugConfigurationPage.DirtyStateListener listener =
        mock(DebugConfigurationPage.DirtyStateListener.class);
    pagePresenter.setDirtyStateListener(listener);

    pagePresenter.onArgumentChanged();

    verify(pageView).getArgument();
    Assert.assertEquals(
        argument,
        configuration
            .getConnectionProperties()
            .get(DspConfigurationPagePresenter.ARGUMENT_PROPERTY));
    verify(listener).onDirtyStateChanged();
  }

  @Test
  public void testOnParametersChanged() throws Exception {
    String parameters = "{ \"another_prop\": 123 }";
    when(pageView.getParameters()).thenReturn(parameters);

    final DebugConfigurationPage.DirtyStateListener listener =
        mock(DebugConfigurationPage.DirtyStateListener.class);
    pagePresenter.setDirtyStateListener(listener);

    pagePresenter.onParametersChanged();

    verify(pageView).getParameters();
    Assert.assertEquals(
        parameters,
        configuration
            .getConnectionProperties()
            .get(DspConfigurationPagePresenter.PARAMETERS_PROPERTY));
    verify(listener).onDirtyStateChanged();
  }
}
