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

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Map;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.debug.DebuggerServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.json.JsonHelper;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.debug.AbstractDebugger;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerLocationHandlerManager;
import org.eclipse.che.plugin.dsp.shared.DspConstants;

public class DspDebugger extends AbstractDebugger {

  public static final String ID = "dsp";
  public static final String DISPLAY_NAME = "Debug Server Protocol";

  @Inject
  public DspDebugger(
      DebuggerServiceClient service,
      RequestTransmitter transmitter,
      RequestHandlerConfigurator configurator,
      DtoFactory dtoFactory,
      LocalStorageProvider localStorageProvider,
      EventBus eventBus,
      NotificationManager notificationManager,
      AppContext appContext,
      DebuggerManager debuggerManager,
      BreakpointManager breakpointManager,
      DebuggerLocalizationConstant constant,
      RequestHandlerManager requestHandlerManager,
      DebuggerLocationHandlerManager debuggerLocationHandlerManager,
      PromiseProvider promiseProvider) {
    super(
        service,
        transmitter,
        configurator,
        dtoFactory,
        localStorageProvider,
        eventBus,
        debuggerManager,
        notificationManager,
        appContext,
        breakpointManager,
        constant,
        requestHandlerManager,
        debuggerLocationHandlerManager,
        promiseProvider,
        ID);
  }

  @Override
  protected DebuggerDescriptor toDescriptor(Map<String, String> connectionProperties) {
    String json = connectionProperties.get(DspConstants.ATTR_DSP_PARAM);
    Map<String, String> params = JsonHelper.toMap(json);
    String userFriendlyishName =
        params.getOrDefault(
            "program",
            params.getOrDefault("target", params.getOrDefault("name", "Debug Server Protocol")));
    return new DebuggerDescriptor("DSP", userFriendlyishName);
  }

  @Override
  public boolean supportsConsole() {
    return true;
  }
}
