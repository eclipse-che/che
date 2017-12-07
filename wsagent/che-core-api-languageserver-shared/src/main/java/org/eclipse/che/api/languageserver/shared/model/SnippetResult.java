package org.eclipse.che.api.languageserver.shared.model;

import org.eclipse.che.jdt.ls.extension.api.dto.LinearRange;

public class SnippetResult {
  private String snippet;
  private LinearRange linearRange;

  public SnippetResult() {}

  public SnippetResult(LinearRange linearRange, String snippet) {
    this.linearRange = linearRange;
    this.snippet = snippet;
  }

  public LinearRange getLinearRange() {
    return linearRange;
  }

  public void setLinearRange(LinearRange linearRange) {
    this.linearRange = linearRange;
  }

  public String getSnippet() {
    return snippet;
  }

  public void setSnippet(String snippet) {
    this.snippet = snippet;
  }
}
