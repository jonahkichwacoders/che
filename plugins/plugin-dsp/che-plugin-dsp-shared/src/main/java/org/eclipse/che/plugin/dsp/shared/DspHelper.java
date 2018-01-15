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
package org.eclipse.che.plugin.dsp.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DspHelper {
  private DspHelper() {}

  /**
   * Get the arguments from the connection map
   *
   * @param map connection parameters map
   * @return list of arguments
   */
  public static List<String> getDspArgs(Map<String, String> map) {
    String nArgsStr = map.get(DspConstants.ATTR_DSP_N_ARGS);
    int nArgs;
    try {
      nArgs = Integer.parseInt(nArgsStr);
    } catch (NumberFormatException e) {
      nArgs = 0;
    }
    List<String> args = new ArrayList<>(nArgs);
    for (int i = 0; i < nArgs; i++) {
      String arg = map.get(DspConstants.ATTR_DSP_ARG_PREFIX + Integer.toString(i));
      if (arg != null) {
        args.add(arg);
      }
    }
    return args;
  }

  /**
   * Set the arguments in the connection map
   *
   * @param map connection parameters map
   * @param args list of arguments
   */
  public static void setDspArgs(Map<String, String> map, List<String> args) {
    map.put(DspConstants.ATTR_DSP_N_ARGS, Integer.toString(args.size()));
    for (int i = 0; i < args.size(); i++) {
      map.put(DspConstants.ATTR_DSP_ARG_PREFIX + Integer.toString(i), args.get(i));
    }
  }
}
