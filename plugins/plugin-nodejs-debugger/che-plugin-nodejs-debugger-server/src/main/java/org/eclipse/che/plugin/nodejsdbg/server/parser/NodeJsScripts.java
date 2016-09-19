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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code scripts} command parser.
 *
 * @author Anatoliy Bazko
 */
public class NodeJsScripts {
    private static final Pattern SCRIPT = Pattern.compile(".* ([0-9]*): (.*)");

    private final Map<Integer, String> scripts;

    private NodeJsScripts(Map<Integer, String> scripts) {
        this.scripts = scripts;
    }

    public Map<Integer, String> getScripts() {
        return scripts;
    }

    /**
     * Factory method.
     */
    public static NodeJsScripts parse(NodeJsOutput nodeJsOutput) {
        Map<Integer, String> scripts = new HashMap<>();

        for (String line : nodeJsOutput.getOutput().split("\n")) {
            Matcher matcher = SCRIPT.matcher(line);
            if (matcher.find()) {
                int number = Integer.parseInt(matcher.group(1));
                String script = matcher.group(2);

                scripts.put(number, script);
            }
        }

        return new NodeJsScripts(scripts);
    }
}
