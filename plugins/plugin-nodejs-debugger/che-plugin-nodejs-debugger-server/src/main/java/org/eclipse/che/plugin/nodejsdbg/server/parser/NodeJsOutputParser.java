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

import java.util.regex.Pattern;
import org.eclipse.che.plugin.nodejsdbg.server.NodeJsOutput;
import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Anatolii Bazko */
public interface NodeJsOutputParser<T> {

  /** Indicates if output matches */
  boolean match(NodeJsOutput nodeJsOutput);

  /** Parses {@link NodeJsOutput} into valuable result. */
  T parse(NodeJsOutput nodeJsOutput) throws NodeJsDebuggerParseException;

  /** Doesn't parse output, just returns as is. */
  NodeJsOutputParser<String> DEFAULT =
      new NodeJsOutputParser<String>() {
        @Override
        public boolean match(NodeJsOutput nodeJsOutput) {
          return true;
        }

        @Override
        public String parse(NodeJsOutput nodeJsOutput) throws NodeJsDebuggerParseException {
          return nodeJsOutput.getOutput();
        }
      };

  /** {@link NodeJsOutputParser} when result will be skipped. */
  NodeJsOutputParser<Void> VOID =
      new NodeJsOutputParser<Void>() {
        @Override
        public boolean match(NodeJsOutput nodeJsOutput) {
          return true;
        }

        @Override
        public Void parse(NodeJsOutput nodeJsOutput) throws NodeJsDebuggerParseException {
          return null;
        }
      };

  class NodeJsOutputRegExpParser implements NodeJsOutputParser<String> {
    private static final Logger LOG = LoggerFactory.getLogger(NodeJsOutputRegExpParser.class);
    private final Pattern pattern;

    public NodeJsOutputRegExpParser(Pattern pattern) {
      this.pattern = pattern;
    }

    @Override
    public boolean match(NodeJsOutput nodeJsOutput) {
      return pattern.matcher(nodeJsOutput.getOutput()).find();
    }

    @Override
    public String parse(NodeJsOutput nodeJsOutput) throws NodeJsDebuggerParseException {
      LOG.debug("{} parse {}", pattern.pattern(), nodeJsOutput.getOutput());
      return nodeJsOutput.getOutput();
    }
  }
}
