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
package org.eclipse.che.api.languageserver.shared.model;

import org.eclipse.lsp4j.Location;

public class ExtendedLocation {
  private String languageServerId;
  private Location location;

  public ExtendedLocation(String languageServerId, Location location) {
    this.languageServerId = languageServerId;
    this.location = location;
  }

  public ExtendedLocation() {}

  public String getLanguageServerId() {
    return languageServerId;
  }

  public void setLanguageServerId(String languageServerId) {
    this.languageServerId = languageServerId;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }
}
