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

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import org.eclipse.che.ide.api.debug.DebugPartPresenter;

public class OpenDebugViewEvent extends GwtEvent<OpenDebugViewEvent.OpenDebugViewHandler> {

  public interface OpenDebugViewHandler extends EventHandler {
    void onOpenDebugView(OpenDebugViewEvent event);
  }

  public static final Type<OpenDebugViewEvent.OpenDebugViewHandler> TYPE = new Type<>();
  private DebugPartPresenter debugPartPresenter;

  public OpenDebugViewEvent(DebugPartPresenter debugPartPresenter) {
    this.debugPartPresenter = debugPartPresenter;
  }

  public DebugPartPresenter getDebugPartPresenter() {
    return debugPartPresenter;
  }

  @Override
  public Type<OpenDebugViewHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(OpenDebugViewHandler handler) {
    handler.onOpenDebugView(this);
  }
}
