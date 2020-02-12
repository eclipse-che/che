package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log;

import java.io.IOException;
import java.io.PipedInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrefixedPipedInputStream extends PipedInputStream {
  private static final Logger LOG = LoggerFactory.getLogger(PrefixedPipedInputStream.class);
  private final String prefix;

  public PrefixedPipedInputStream(String prefix) {
    this.prefix = prefix;
  }

  public String prefix() {
    return prefix;
  }

  @Override
  public void close() throws IOException {
    LOG.info("closing inputstream");
    super.close();
  }
}
