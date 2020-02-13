package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log;

import java.io.PipedInputStream;

public class PrefixedPipedInputStream extends PipedInputStream {
  private final String prefix;

  public PrefixedPipedInputStream(String prefix) {
    this.prefix = prefix;
  }

  public String prefix() {
    return prefix;
  }
}
