/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.nodejsdbg.server.parser;

import org.eclipse.che.plugin.nodejsdbg.server.NodeJsOutput;
import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerParseException;

import java.util.regex.Pattern;

/**
 * @author Anatolii Bazko
 */
public interface NodeJsOutputParser<T> {

    /**
     * Indicates if output matches
     */
    boolean match(NodeJsOutput nodeJsOutput);

    /**
     * Parses {@link NodeJsOutput} into valuable result.
     */
    T parse(NodeJsOutput nodeJsOutput) throws NodeJsDebuggerParseException;

    /**
     * Doesn't parse output, just returns as is.
     */
    NodeJsOutputParser<String> DEFAULT = new NodeJsOutputParser<String>() {
        @Override
        public boolean match(NodeJsOutput nodeJsOutput) {
            return true;
        }

        @Override
        public String parse(NodeJsOutput nodeJsOutput) throws NodeJsDebuggerParseException {
            return nodeJsOutput.getOutput();
        }
    };

    /**
     * {@link NodeJsOutputParser} when result will be skipped.
     */
    NodeJsOutputParser<Void> VOID = new NodeJsOutputParser<Void>() {
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
            return nodeJsOutput.getOutput();
        }
    }
}
