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
package org.eclipse.che.plugin.nodejsdbg.server.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.plugin.nodejsdbg.server.NodeJsOutput;
import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerParseException;

/**
 * {@code backtrace} command parser.
 *
 * @author Anatoliy Bazko
 */
public class NodeJsStepParser implements NodeJsOutputParser<Location> {

  public static final NodeJsStepParser INSTANCE = new NodeJsStepParser();
  public static final Pattern PATTERN = Pattern.compile("^break in (.*):([0-9]+)");

  private NodeJsStepParser() {}

  @Override
  public boolean match(NodeJsOutput nodeJsOutput) {
    return nodeJsOutput.getOutput().startsWith("break in");
  }

  @Override
  public Location parse(NodeJsOutput nodeJsOutput) throws NodeJsDebuggerParseException {
    String output = nodeJsOutput.getOutput();

    for (String line : output.split("\n")) {
      Matcher matcher = PATTERN.matcher(line);
      if (matcher.find()) {
        String file = matcher.group(1);
        String lineNumber = matcher.group(2);
        return new LocationImpl(file, Integer.parseInt(lineNumber));
      }
    }

    throw new NodeJsDebuggerParseException(Location.class, output);
  }
}
