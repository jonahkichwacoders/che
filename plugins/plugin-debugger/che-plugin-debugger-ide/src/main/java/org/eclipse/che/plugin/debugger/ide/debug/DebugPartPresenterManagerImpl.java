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
package org.eclipse.che.plugin.debugger.ide.debug;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.eclipse.che.ide.api.debug.DebugPartPresenter;
import org.eclipse.che.ide.api.debug.DebugPartPresenterManager;

public class DebugPartPresenterManagerImpl implements DebugPartPresenterManager {

  Collection<DebugPartPresenter> registeredParts = new ArrayList<>();

  @Inject
  public DebugPartPresenterManagerImpl() {}

  @Override
  public void registerDebugPartPresenter(DebugPartPresenter presenter) {
    registeredParts.add(presenter);
  }

  @Override
  public Collection<DebugPartPresenter> getDebugPartPresenters() {
    return Collections.unmodifiableCollection(registeredParts);
  }

  @Override
  public Collection<DebugPartPresenter> getInitialDebugPartPresenters() {
    // TODO new API to set this up for real
    return Collections.unmodifiableCollection(registeredParts);
  }
}
