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

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.eclipse.che.ide.api.debug.DebugPartPresenter;
import org.eclipse.che.ide.api.debug.DebugPartPresenterManager;
import com.google.inject.Inject;

public class DebugPartPresenterManagerImpl implements DebugPartPresenterManager {
  private static class PartInfo {
    DebugPartPresenter part;
    boolean openInitially;

    public PartInfo(DebugPartPresenter part, boolean openInitially) {
      this.part = part;
      this.openInitially = openInitially;
    }
  }

  Collection<PartInfo> registeredParts = new ArrayList<>();

  @Inject
  public DebugPartPresenterManagerImpl() {}

  @Override
  public void registerDebugPartPresenter(DebugPartPresenter presenter, boolean openInitially) {
    PartInfo partInfo = new PartInfo(presenter, openInitially);
    registeredParts.add(partInfo);
  }

  @Override
  public Collection<DebugPartPresenter> getDebugPartPresenters() {
    return registeredParts
        .stream()
        .map(partInfo -> partInfo.part)
        .collect(collectingAndThen(toList(), Collections::unmodifiableList));
  }

  @Override
  public Collection<DebugPartPresenter> getInitialDebugPartPresenters() {
    return registeredParts
        .stream()
        .filter(partInfo -> partInfo.openInitially)
        .map(partInfo -> partInfo.part)
        .collect(collectingAndThen(toList(), Collections::unmodifiableList));
  }
}
