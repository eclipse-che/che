package org.eclipse.che.api.languageserver.shared.model;

public class FileContentParameters {
  private String languagesServerId;
  private String uri;

  public FileContentParameters() {}

  public FileContentParameters(String languagesServerId, String uri) {
    this.languagesServerId = languagesServerId;
    this.uri = uri;
  }

  public String getLanguagesServerId() {
    return languagesServerId;
  }

  public void setLanguagesServerId(String languagesServerId) {
    this.languagesServerId = languagesServerId;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }
}
