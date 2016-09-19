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

/**
 * {@code exec} command parser.
 *
 * @author Anatoliy Bazko
 */
public class NodeJsExec {

    private final String value;

    public NodeJsExec(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Factory method.
     */
    public static NodeJsExec parse(NodeJsOutput nodeJsOutput) {
        String output = nodeJsOutput.getOutput();
        return new NodeJsExec(output);
    }
}
