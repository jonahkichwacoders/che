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
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.Debugger.DebuggerCallback;
import org.eclipse.che.api.debugger.server.DebuggerFactory;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;

public class DspDebuggerFactory implements DebuggerFactory {
  public static final String TYPE = "dsp";

  public static final String COMMAND_PROPERTY = "COMMAND";
  public static final String ARGUMENT_PROPERTY = "ARGUMENT";
  public static final String PARAMETERS_PROPERTY = "PARAMETERS";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public Debugger create(Map<String, String> properties, DebuggerCallback debuggerCallback)
      throws DebuggerException {
    String command = properties.get(COMMAND_PROPERTY);
    String argument = properties.get(ARGUMENT_PROPERTY);
    //TODO
    argument = "/home/user/andreweinand.mock-debug-0.23.0/out/mockDebug.js";
    String jsonParams = properties.get(PARAMETERS_PROPERTY);
    Gson gson = new Gson();
    Type type = new TypeToken<Map<String, Object>>() {}.getType();
    Map<String, Object> launchArguments = gson.fromJson(jsonParams, type);

    try {
      List<String> commandLine = new ArrayList<>();
      if (command == null) {
        throw new DebuggerException("Debug command unspecified.");
      }
      commandLine.add(command);
      if (argument != null && !argument.isEmpty()) {
        commandLine.add(argument);
      }
      ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
      Process process = processBuilder.start();
      return new DspDebugger(debuggerCallback, process, launchArguments);
    } catch (IOException e1) {
      throw new DebuggerException("Failed to launch debug process", e1);
    }
  }
}
