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
package org.eclipse.che.plugin.nodejsdbg.server.command;

import com.google.common.util.concurrent.SettableFuture;

import org.eclipse.che.plugin.nodejsdbg.server.NodeJsDebugProcess;
import org.eclipse.che.plugin.nodejsdbg.server.NodeJsOutput;
import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerException;
import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerParseException;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsOutputParser;

import java.util.concurrent.Future;

/**
 * Basic implementation of {@link NodeJsDebugCommand}.
 * When command is executed, it scans outputs until appropriate found, parses it and returns result of execution.
 *
 * @author Anatolii Bazko
 */
public class NodeJsDebugCommandImpl<T> implements NodeJsDebugCommand<T> {

    private final SettableFuture<T>     result;
    public final  NodeJsOutputParser<T> parser;
    private final String                input;

    public NodeJsDebugCommandImpl(NodeJsOutputParser<T> parser, String input) {
        this.parser = parser;
        this.input = input;
        this.result = SettableFuture.create();
    }

    @Override
    public Future<T> execute(NodeJsDebugProcess process) throws NodeJsDebuggerException {
        process.send(input);
        return result;
    }

    @Override
    public boolean onOutputProduced(NodeJsOutput nodeJsOutput) throws NodeJsDebuggerParseException {
        if (parser.match(nodeJsOutput)) {
            T t = parser.parse(nodeJsOutput);
            return result.set(t);
        }

        return false;
    }
}
