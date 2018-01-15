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

import org.eclipse.che.api.debug.shared.model.BreakpointConfiguration;
import org.eclipse.che.api.debug.shared.model.SuspendPolicy;

public class ImmutableBreakpointConfiguration implements BreakpointConfiguration {

  private boolean conditionEnabled;
  private String condition;
  private boolean hitCountEnabled;
  private int hitCount;
  private SuspendPolicy suspendPolicy;

  public ImmutableBreakpointConfiguration(BreakpointConfiguration configuration) {
    conditionEnabled = configuration.isConditionEnabled();
    condition = configuration.getCondition();
    hitCountEnabled = configuration.isHitCountEnabled();
    hitCount = configuration.getHitCount();
    suspendPolicy = configuration.getSuspendPolicy();
  }

  @Override
  public boolean isConditionEnabled() {
    return conditionEnabled;
  }

  @Override
  public String getCondition() {
    return condition;
  }

  @Override
  public boolean isHitCountEnabled() {
    return hitCountEnabled;
  }

  @Override
  public int getHitCount() {
    return hitCount;
  }

  @Override
  public SuspendPolicy getSuspendPolicy() {
    return suspendPolicy;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((condition == null) ? 0 : condition.hashCode());
    result = prime * result + (conditionEnabled ? 1231 : 1237);
    result = prime * result + hitCount;
    result = prime * result + (hitCountEnabled ? 1231 : 1237);
    result = prime * result + ((suspendPolicy == null) ? 0 : suspendPolicy.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ImmutableBreakpointConfiguration other = (ImmutableBreakpointConfiguration) obj;
    if (condition == null) {
      if (other.condition != null) return false;
    } else if (!condition.equals(other.condition)) return false;
    if (conditionEnabled != other.conditionEnabled) return false;
    if (hitCount != other.hitCount) return false;
    if (hitCountEnabled != other.hitCountEnabled) return false;
    if (suspendPolicy != other.suspendPolicy) return false;
    return true;
  }
}
