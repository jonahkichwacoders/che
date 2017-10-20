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
import org.eclipse.che.api.debug.shared.model.Location;

public class ImmutableBreakpoint implements Breakpoint {

  private ImmutableLocation location;
  private boolean enabled;
  private String condition;

  public ImmutableBreakpoint(Breakpoint breakpoint) {
    Location bpLocation = breakpoint.getLocation();
    if (bpLocation == null) {
      location = null;
    } else {
      location = new ImmutableLocation(bpLocation);
    }
    enabled = breakpoint.isEnabled();
    condition = breakpoint.getCondition();
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
  public String getCondition() {
    return condition;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((condition == null) ? 0 : condition.hashCode());
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
    if (condition == null) {
      if (other.condition != null) return false;
    } else if (!condition.equals(other.condition)) return false;
    if (enabled != other.enabled) return false;
    if (location == null) {
      if (other.location != null) return false;
    } else if (!location.equals(other.location)) return false;
    return true;
  }
}
