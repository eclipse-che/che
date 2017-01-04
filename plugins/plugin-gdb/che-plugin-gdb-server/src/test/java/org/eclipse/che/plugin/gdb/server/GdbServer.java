/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.gdb.server;

import org.eclipse.che.plugin.gdb.server.exception.GdbParseException;

import java.io.IOException;

/**
 * GDB server.
 *
 * @author Anatoliy Bazko
 */
public class GdbServer extends GdbProcess {

    private static final String PROCESS_NAME     = "gdbserver";
    private static final String OUTPUT_SEPARATOR = "\n";

    private GdbServer(String host, int port, String file) throws IOException,
                                                                 GdbParseException,
                                                                 InterruptedException {
        super(OUTPUT_SEPARATOR, PROCESS_NAME, host + ":" + port, file);
    }

    /**
     * Starts gdb server.
     */
    public static GdbServer start(String host, int port, String file) throws InterruptedException,
                                                                             GdbParseException,
                                                                             IOException {
        return new GdbServer(host, port, file);
    }
}
