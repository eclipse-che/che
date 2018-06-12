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
package org.eclipse.che.selenium.core.utils.process;

import static java.lang.String.format;

import com.google.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Dmytro Nochevnov */
@Singleton
public class ProcessAgent {
  private static final Logger LOG = LoggerFactory.getLogger(ProcessAgent.class);

  public String process(String command) throws ProcessAgentException {
    try {
      Process process = getProcess(command);

      int exitStatus = process.waitFor();
      InputStream in = process.getInputStream();
      InputStream err = process.getErrorStream();

      return processOutput(exitStatus, in, err);
    } catch (Exception e) {
      String errMessage = format("Can't process command '%s'.", command);
      throw new ProcessAgentException(errMessage, e);
    }
  }

  private Process getProcess(String command) throws IOException {
    ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", command);
    return pb.start();
  }

  private String processOutput(int exitStatus, InputStream in, InputStream error) throws Exception {
    String output = readOutput(in);
    String errorOutput = readOutput(error);

    if (exitStatus == 0 && errorOutput.isEmpty()) {
      return output;
    }

    throw new Exception(format("Output: %s; Error: %s.", output, errorOutput));
  }

  private String readOutput(InputStream in) throws IOException {
    try {
      String output = IOUtils.toString(in, Charset.forName("UTF-8"));
      if (output.endsWith("\n")) {
        output = output.substring(0, output.length() - 1);
      }
      return output;
    } finally {
      in.close();
    }
  }
}
