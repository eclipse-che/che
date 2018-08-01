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
package org.eclipse.che.plugin.nodejsdbg.server;

/**
 * Wrapper for NodeJs output.
 *
 * @author Anatoliy Bazko
 */
public class NodeJsOutput {
  private final String output;

  private NodeJsOutput(String output) {
    this.output = output;
  }

  public static NodeJsOutput of(String output) {
    return new NodeJsOutput(strip(output));
  }

  public String getOutput() {
    return output;
  }

  public boolean isEmpty() {
    return output.isEmpty();
  }

  private static String strip(String output) {
    if (output.endsWith("\n")) {
      output = output.substring(0, output.length() - 1);
    }

    return output.replaceAll("\\u001B\\[[0-9][0-9]m", "").replace("\b", "");
  }
}
