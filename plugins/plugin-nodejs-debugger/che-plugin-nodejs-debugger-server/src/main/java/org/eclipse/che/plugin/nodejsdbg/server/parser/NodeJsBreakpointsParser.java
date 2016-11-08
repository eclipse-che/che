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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

/**
 * {@code breakpoints} command parser.
 *
 * @author Anatoliy Bazko
 */
public class NodeJsBreakpointsParser implements NodeJsOutputParser<NodeJsBreakpointsParser.Breakpoints> {
    private static final Logger LOG  = LoggerFactory.getLogger(NodeJsBreakpointsParser.class);

    public static final NodeJsBreakpointsParser INSTANCE = new NodeJsBreakpointsParser();

    @Override
    public boolean match(NodeJsOutput nodeJsOutput) {
        return nodeJsOutput.getOutput().startsWith("{ breakpoints:");
    }

    @Override
    public Breakpoints parse(NodeJsOutput nodeJsOutput) throws NodeJsDebuggerParseException {
        final List<Breakpoint> breakpoints = new ArrayList<>();

        JsonObject json = new JsonParser().parse(nodeJsOutput.getOutput()).getAsJsonObject();
        if (json.has("breakpoints")) {
            Iterator<JsonElement> iter = json.getAsJsonArray("breakpoints").iterator();
            while (iter.hasNext()) {
                JsonObject item = iter.next().getAsJsonObject();
                try {
                    final String condition = item.has("condition") && !item.get("condition").isJsonNull()
                                             ? item.get("condition").getAsString()
                                             : null;
                    final boolean isEnabled = item.has("active") && !item.get("active").isJsonNull() && item.get("active").getAsBoolean();
                    final int lineNumber = item.get("line").getAsInt();

                    final String target;
                    String targetType = item.get("type").getAsString();

                    switch (targetType) {
                        case "scriptId":
                            target = String.valueOf(item.get("script_id").getAsInt());
                            break;
                        case "scriptRegExp":
                            target = item.get("script_regexp").getAsString();
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
