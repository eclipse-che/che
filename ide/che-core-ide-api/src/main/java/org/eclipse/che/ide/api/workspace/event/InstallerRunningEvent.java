/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.workspace.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/** Fired when installer in some machine goes into a running state. */
public class InstallerRunningEvent extends GwtEvent<InstallerRunningEvent.Handler> {

  public static final Type<InstallerRunningEvent.Handler> TYPE = new Type<>();

  private final String installer;
  private final String machineName;
  private final boolean serverRunning;

  public InstallerRunningEvent(String installer, String machineName, boolean serverRunning) {
    this.installer = installer;
    this.machineName = machineName;
    this.serverRunning = serverRunning;
  }

  /** Returns the installer identifier. */
  public String getInstaller() {
    return installer;
  }

  /** Returns the related machine's name. */
  public String getMachineName() {
    return machineName;
  }

  /** Returns true if corresponding server is defined and running. */
  public boolean isServerRunning() {
    return serverRunning;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onInstallerRunning(this);
  }

  public interface Handler extends EventHandler {
    void onInstallerRunning(InstallerRunningEvent event);
  }
}
