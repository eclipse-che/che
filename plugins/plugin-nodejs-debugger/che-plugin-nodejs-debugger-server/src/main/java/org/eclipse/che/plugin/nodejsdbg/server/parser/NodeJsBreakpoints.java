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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.lang.Double.parseDouble;

/**
 * {@code breakpoints} command parser.
 *
 * @author Anatoliy Bazko
 */
public class NodeJsBreakpoints {
    private static final Logger LOG  = LoggerFactory.getLogger(NodeJsBreakpoints.class);
    private static final Gson   GSON = new GsonBuilder().serializeNulls().create();

    private final List<Breakpoint> breakpoints;

    private NodeJsBreakpoints(List<Breakpoint> breakpoints) {
        this.breakpoints = breakpoints;
    }

    public List<Breakpoint> getBreakpoints() {
        return breakpoints;
    }

    /**
     * Factory method.
     */
    public static NodeJsBreakpoints parse(NodeJsOutput scriptsOutput, NodeJsOutput breakpointsOutput) {
        final List<Breakpoint> breakpoints = new ArrayList<>();
        final Map<Integer, String> scripts = NodeJsScripts.parse(scriptsOutput).getScripts();

        @SuppressWarnings("unchecked")
        Map<String, Object> m = GSON.fromJson(breakpointsOutput.getOutput(), Map.class);
        if (m.containsKey("breakpoints")) {

            @SuppressWarnings("unchecked")
            Iterator<Map> iter = ((Iterable)m.get("breakpoints")).iterator();
            while (iter.hasNext()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> item = iter.next();

                try {
                    final String condition = (String)item.get("condition");
                    final boolean isEnabled = (Boolean)item.getOrDefault("active", false);
                    final int lineNumber = ((Double)item.get("line")).intValue();

                    final String target;
                    String targetType = (String)item.get("type");

                    switch (targetType) {
                        case "scriptId":
                            int scriptId = (int)parseDouble(item.get("script_id").toString());
                            target = scripts.get(scriptId);
                            break;
                        case "scriptRegExp":
                            target = (String)item.get("script_regexp");
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported 'type' value: " + targetType);
                    }

                    Breakpoint breakpoint = new BreakpointImpl(new LocationImpl(target, lineNumber), isEnabled, condition);
                    breakpoints.add(breakpoint);
                } catch (Exception e) {
                    LOG.error("Failed to parse breakpoint: " + item.toString(), e);
                }
            }
        }

        return new NodeJsBreakpoints(breakpoints);
    }
}
