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

        return output.replaceAll("\\u001B\\[[0-9][0-9]m", "")
                     .replace("\b", "");
    }
}
