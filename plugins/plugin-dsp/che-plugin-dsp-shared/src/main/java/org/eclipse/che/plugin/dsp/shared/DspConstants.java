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

import java.util.List;
import java.util.Map;

public class DspConstants {
  private DspConstants() {}

  // Unique identifier for the DSP debug model
  public static final String ID_DSP_DEBUG_MODEL = "org.eclipse.che.debug.model";

  // Launch configuration attribute keys
  /** String, one of {@link #DSP_MODE_LAUNCH} or {@link #DSP_MODE_CONNECT} */
  public static final String ATTR_DSP_MODE = ID_DSP_DEBUG_MODEL + ".ATTR_DSP_MODE";

  public static final String DSP_MODE_LAUNCH = "launch server";
  public static final String DSP_MODE_CONNECT = "connect to server";
  /** String */
  public static final String ATTR_DSP_CMD = ID_DSP_DEBUG_MODEL + ".ATTR_DSP_CMD";
  /**
   * Number - count of ATTR_DSP_ARG_n.
   *
   * <p>Use {@link DspHelper#getDspArgs(Map)} and {@link DspHelper#setDspArgs(Map, List)} utilities.
   */
  public static final String ATTR_DSP_N_ARGS = ID_DSP_DEBUG_MODEL + ".ATTR_DSP_N_ARGS";
  /**
   * String for argument. Each argument from 0 to ATTR_DSP_N_ARGS - 1 should be suffixed onto this
   * key name
   *
   * <p>Use {@link DspHelper#getDspArgs(Map)} and {@link DspHelper#setDspArgs(Map, List)} utilities.
   */
  public static final String ATTR_DSP_ARG_PREFIX = ID_DSP_DEBUG_MODEL + ".ATTR_DSP_ARG_";

  /** String - should be properly formed JSON */
  public static final String ATTR_DSP_PARAM = ID_DSP_DEBUG_MODEL + ".ATTR_DSP_PARAM";
  /** String */
  public static final String ATTR_DSP_SERVER_HOST = ID_DSP_DEBUG_MODEL + ".ATTR_DSP_SERVER_HOST";
  /** Integer */
  public static final String ATTR_DSP_SERVER_PORT = ID_DSP_DEBUG_MODEL + ".ATTR_DSP_SERVER_PORT";
}
