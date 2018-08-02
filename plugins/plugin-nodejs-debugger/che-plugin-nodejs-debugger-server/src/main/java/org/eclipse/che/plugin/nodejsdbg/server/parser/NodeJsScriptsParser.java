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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.che.plugin.nodejsdbg.server.NodeJsOutput;

/**
 * {@code scripts} command parser.
 *
 * @author Anatoliy Bazko
 */
public class NodeJsScriptsParser implements NodeJsOutputParser<NodeJsScriptsParser.Scripts> {
  private static final Pattern SCRIPT = Pattern.compile(".* ([0-9]*): (.*)");

  public static final NodeJsScriptsParser INSTANCE = new NodeJsScriptsParser();

  @Override
  public boolean match(NodeJsOutput nodeJsOutput) {
    for (String line : nodeJsOutput.getOutput().split("\n")) {
      Matcher matcher = SCRIPT.matcher(line);
      if (!matcher.find()) {
        return false;
      }
    }

    return !nodeJsOutput.isEmpty();
  }

  @Override
  public Scripts parse(NodeJsOutput nodeJsOutput) {
    Map<Integer, String> scripts = new HashMap<>();

    for (String line : nodeJsOutput.getOutput().split("\n")) {
      Matcher matcher = SCRIPT.matcher(line);
      if (matcher.find()) {
        int number = Integer.parseInt(matcher.group(1));
        String script = matcher.group(2);

        scripts.put(number, script);
      }
    }

    return new Scripts(scripts);
  }

  public static class Scripts {
    private final Map<Integer, String> scripts;

    public Scripts(Map<Integer, String> scripts) {
      this.scripts = scripts;
    }

    public Map<Integer, String> getAll() {
      return scripts;
    }
  }
}
