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
package org.eclipse.che.plugin.dsp.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.Debugger.DebuggerCallback;
import org.eclipse.che.api.debugger.server.DebuggerFactory;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.plugin.dsp.shared.DspConstants;
import org.eclipse.che.plugin.dsp.shared.DspHelper;

public class DspDebuggerFactory implements DebuggerFactory {
  @Inject private Provider<DspDebugger> dspDebuggerProvider;

  public static final String TYPE = "dsp";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public Debugger create(Map<String, String> properties, DebuggerCallback debuggerCallback)
      throws DebuggerException {

    // TODO implement connect
    boolean launchNotConnect =
        !DspConstants.DSP_MODE_CONNECT.equals(properties.get(DspConstants.ATTR_DSP_MODE));

    String command = properties.get(DspConstants.ATTR_DSP_CMD);
    List<String> arguments = DspHelper.getDspArgs(properties);
    String jsonParams = properties.get(DspConstants.ATTR_DSP_PARAM);
    Gson gson = new Gson();
    Type type = new TypeToken<Map<String, Object>>() {}.getType();
    Map<String, Object> launchArguments = gson.fromJson(jsonParams, type);

    try {
      List<String> commandLine = new ArrayList<>();
      if (command == null) {
        throw new DebuggerException("Debug command unspecified.");
      }
      commandLine.add(command);
      commandLine.addAll(arguments);
      ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
      Process process = processBuilder.start();
      DspDebugger dspDebugger = dspDebuggerProvider.get();
      dspDebugger.init(debuggerCallback, process, launchArguments);
      return dspDebugger;
    } catch (IOException e1) {
      throw new DebuggerException("Failed to launch debug process", e1);
    }
  }
}
