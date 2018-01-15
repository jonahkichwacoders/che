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

import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.BreakpointConfiguration;
import org.eclipse.che.api.debug.shared.model.Location;

public class ImmutableBreakpoint implements Breakpoint {

  private ImmutableLocation location;
  private boolean enabled;
  private ImmutableBreakpointConfiguration breakpointConfiguration;

  public ImmutableBreakpoint(Breakpoint breakpoint) {
    Location bpLocation = breakpoint.getLocation();
    if (bpLocation == null) {
      location = null;
    } else {
      location = new ImmutableLocation(bpLocation);
    }
    enabled = breakpoint.isEnabled();
    BreakpointConfiguration configuration = breakpoint.getBreakpointConfiguration();
    if (configuration == null) {
      breakpointConfiguration = null;
    } else {
      breakpointConfiguration = new ImmutableBreakpointConfiguration(configuration);
    }
  }

  @Override
  public ImmutableLocation getLocation() {
    return location;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    // TODO At the time of authoring ImmutableBreakpoint, Breakpoint had an immutable interface,
    // CHE-6845 changed that
  }

  @Override
  public ImmutableBreakpointConfiguration getBreakpointConfiguration() {
    return breakpointConfiguration;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result
            + ((breakpointConfiguration == null) ? 0 : breakpointConfiguration.hashCode());
    result = prime * result + (enabled ? 1231 : 1237);
    result = prime * result + ((location == null) ? 0 : location.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ImmutableBreakpoint other = (ImmutableBreakpoint) obj;
    if (breakpointConfiguration == null) {
      if (other.breakpointConfiguration != null) return false;
    } else if (!breakpointConfiguration.equals(other.breakpointConfiguration)) return false;
    if (enabled != other.enabled) return false;
    if (location == null) {
      if (other.location != null) return false;
    } else if (!location.equals(other.location)) return false;
    return true;
  }
}
