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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.debug.shared.model.VariablePath;

public class ImmutableVariablePath implements VariablePath {
  private List<String> path;

  public ImmutableVariablePath(List<String> path) {
    this.path = Collections.unmodifiableList(new ArrayList<>(path));
  }

  public ImmutableVariablePath(String oneSegmentPath) {
    this.path = Collections.singletonList(oneSegmentPath);
  }

  public ImmutableVariablePath(VariablePath variablePath) {
    this(variablePath.getPath());
  }

  @Override
  public List<String> getPath() {
    return path;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ImmutableVariablePath other = (ImmutableVariablePath) obj;
    if (path == null) {
      if (other.path != null) return false;
    } else if (!path.equals(other.path)) return false;
    return true;
  }
}
