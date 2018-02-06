/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.debug.shared.model;

public interface Register {
  /** The register name. */
  String getName();

  /** The register value. */
  SimpleValue getValue();

  /** The register type. E.g.: String, int etc. */
  String getType();

  /** The path to the register. */
  VariablePath getVariablePath();

  /**
   * Indicates if register is primitive. Most registers are primitive, but some, like flags
   * registers, can have children, so they are not native.
   */
  boolean isPrimitive();
}
