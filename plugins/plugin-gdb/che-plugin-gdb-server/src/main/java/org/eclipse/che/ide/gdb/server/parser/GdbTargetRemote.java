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
package org.eclipse.che.ide.gdb.server.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 'target remote' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbTargetRemote {

    private static final Pattern GDB_TARGET_REMOTE = Pattern.compile("Remote debugging using (.*):(.*)\n.*");

    private final String host;
    private final String port;

    public GdbTargetRemote(String host, String port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    /**
     * Factory method.
     */
    public static GdbTargetRemote parse(GdbOutput gdbOutput) throws GdbParseException {
        String output = gdbOutput.getOutput();

        Matcher matcher = GDB_TARGET_REMOTE.matcher(output);
        if (matcher.find()) {
            String host = matcher.group(1);
            String port = matcher.group(2);
            return new GdbTargetRemote(host, port);
        }

        throw new GdbParseException(GdbTargetRemote.class, output);
    }
}
