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

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.Method;

public class ImmutableLocation implements Location {

  private String target;
  private int lineNumber;
  private String resourceProjectPath;

  public ImmutableLocation(Location location) {
    target = location.getTarget();
    lineNumber = location.getLineNumber();
    resourceProjectPath = location.getResourceProjectPath();
  }

  @Override
  public String getTarget() {
    return target;
  }

  @Override
  public int getLineNumber() {
    return lineNumber;
  }

  @Override
  public boolean isExternalResource() {
    return false;
  }

  @Override
  public int getExternalResourceId() {
    return 0;
  }

  @Override
  public String getResourceProjectPath() {
    return resourceProjectPath;
  }

  @Override
  public Method getMethod() {
    return null;
  }

  @Override
  public long getThreadId() {
    return -1;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + lineNumber;
    result = prime * result + ((resourceProjectPath == null) ? 0 : resourceProjectPath.hashCode());
    result = prime * result + ((target == null) ? 0 : target.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ImmutableLocation other = (ImmutableLocation) obj;
    if (lineNumber != other.lineNumber) return false;
    if (resourceProjectPath == null) {
      if (other.resourceProjectPath != null) return false;
    } else if (!resourceProjectPath.equals(other.resourceProjectPath)) return false;
    if (target == null) {
      if (other.target != null) return false;
    } else if (!target.equals(other.target)) return false;
    return true;
  }
}
