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
package org.eclipse.che.ide.api.debug;

import java.util.Collection;

public interface DebugPartPresenterManager {
  /**
   * Register a view that can be opened from the Add menu in the Debug part.
   *
   * @param presenter part presenter for the debug view
   */
  void registerDebugPartPresenter(DebugPartPresenter presenter);

  /**
   * Returns a map of registered debug views.
   *
   * @return views
   */
  Collection<DebugPartPresenter> getDebugPartPresenters();

  /**
   * Returns a map of debug views to open initially
   *
   * @return
   */
  Collection<DebugPartPresenter> getInitialDebugPartPresenters();
}
