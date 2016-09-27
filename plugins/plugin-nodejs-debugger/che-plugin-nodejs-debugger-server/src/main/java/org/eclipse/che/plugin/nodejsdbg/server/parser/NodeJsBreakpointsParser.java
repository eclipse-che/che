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
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.plugin.nodejsdbg.server.NodeJsOutput;
import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * {@code breakpoints} command parser.
 *
 * @author Anatoliy Bazko
 */
public class NodeJsBreakpointsParser implements NodeJsOutputParser<NodeJsBreakpointsParser.Breakpoints> {
    private static final Logger LOG  = LoggerFactory.getLogger(NodeJsBreakpointsParser.class);
    private static final Gson   GSON = new GsonBuilder().serializeNulls().create();

    public static final NodeJsBreakpointsParser INSTANCE = new NodeJsBreakpointsParser();

    @Override
    public boolean match(NodeJsOutput nodeJsOutput) {
        return nodeJsOutput.getOutput().startsWith("{ breakpoints:");
    }

    @Override
    public Breakpoints parse(NodeJsOutput nodeJsOutput) throws NodeJsDebuggerParseException {
        final List<Breakpoint> breakpoints = new ArrayList<>();

        @SuppressWarnings("unchecked")
        Map<String, Object> m = GSON.fromJson(nodeJsOutput.getOutput(), Map.class);
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
                            target = item.get("script_id").toString();
                            break;
                        case "scriptRegExp":
                            target = (String)item.get("script_regexp");
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported 'type' value: " + targetType);
                    }

                    Location location = new LocationImpl(targetType + ":" + target, lineNumber + 1);
                    Breakpoint breakpoint = new BreakpointImpl(location, isEnabled, condition);
                    breakpoints.add(breakpoint);
                } catch (Exception e) {
                    LOG.error("Failed to parse breakpoint: " + item.toString(), e);
                }
            }
        }

        return new Breakpoints(breakpoints);
    }

    public static class Breakpoints {
        private final List<Breakpoint> breakpoints;

        private Breakpoints(List<Breakpoint> breakpoints) {this.breakpoints = breakpoints;}

        public List<Breakpoint> getAll() {
            return breakpoints;
        }
    }
}
