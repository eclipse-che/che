/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.workspace.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/** Fired when installer in some machine goes into a failed state. */
public class InstallerFailedEvent extends GwtEvent<InstallerFailedEvent.Handler> {

  public static final Type<InstallerFailedEvent.Handler> TYPE = new Type<>();

  private final String installer;
  private final String error;
  private final String machineName;

  public InstallerFailedEvent(String installer, String error, String machineName) {
    this.installer = installer;
    this.error = error;
    this.machineName = machineName;
  }

  /** Returns the installer identifier. */
  public String getInstaller() {
    return installer;
  }

  /** Returns the error message. */
  public String getError() {
    return error;
  }

  /** Returns the related machine's name. */
  public String getMachineName() {
    return machineName;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onInstallerFailed(this);
  }

  public interface Handler extends EventHandler {
    void onInstallerFailed(InstallerFailedEvent event);
  }
}
