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
