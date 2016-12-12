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
package org.eclipse.che.api.debugger.server;

import com.google.inject.Inject;

import org.eclipse.che.api.debug.shared.dto.SimpleValueDto;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerNotFoundException;
import org.eclipse.che.api.debug.shared.dto.BreakpointDto;
import org.eclipse.che.api.debug.shared.dto.DebugSessionDto;
import org.eclipse.che.api.debug.shared.dto.StackFrameDumpDto;
import org.eclipse.che.api.debug.shared.dto.VariableDto;
import org.eclipse.che.api.debug.shared.dto.action.ActionDto;
import org.eclipse.che.api.debug.shared.model.DebuggerInfo;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.action.ResumeAction;
import org.eclipse.che.api.debug.shared.model.action.StartAction;
import org.eclipse.che.api.debug.shared.model.action.StepIntoAction;
import org.eclipse.che.api.debug.shared.model.action.StepOutAction;
import org.eclipse.che.api.debug.shared.model.action.StepOverAction;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.api.debugger.server.DtoConverter.asBreakpointsDto;
import static org.eclipse.che.api.debugger.server.DtoConverter.asDto;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Debugger REST API.
 *
 * @author Anatoliy Bazko
 */
@Path("debugger")
public class DebuggerService {
    private final DebuggerManager debuggerManager;

    @Inject
    public DebuggerService(DebuggerManager debuggerManager) {
        this.debuggerManager = debuggerManager;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public DebugSessionDto connect(final @QueryParam("type") String debuggerType,
                                   final Map<String, String> properties) throws DebuggerException {
        String sessionId = debuggerManager.create(debuggerType, properties);
        return getDebugSession(sessionId);
    }

    @DELETE
    @Path("{id}")
    public void disconnect(@PathParam("id") String sessionId) throws DebuggerException {
        Debugger debugger;
        try {
            debugger = debuggerManager.getDebugger(sessionId);
        } catch (DebuggerNotFoundException e) {
            // probably already disconnected
            return;
        }

        debugger.disconnect();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public DebugSessionDto getDebugSession(@PathParam("id") String sessionId) throws DebuggerException {
        DebuggerInfo debuggerInfo = debuggerManager.getDebugger(sessionId).getInfo();

        DebugSessionDto debugSessionDto = newDto(DebugSessionDto.class);
        debugSessionDto.setDebuggerInfo(asDto(debuggerInfo));
        debugSessionDto.setId(sessionId);
        debugSessionDto.setType(debuggerManager.getDebuggerType(sessionId));

        return debugSessionDto;
    }

    @POST
    @Path("{id}")
    public void performAction(@PathParam("id") String sessionId, ActionDto action) throws DebuggerException {
        Debugger debugger = debuggerManager.getDebugger(sessionId);
        switch (action.getType()) {
            case START:
                debugger.start((StartAction)action);
                break;
            case RESUME:
                debugger.resume((ResumeAction)action);
                break;
            case SUSPEND:
                debugger.suspend();
                break;
            case STEP_INTO:
                debugger.stepInto((StepIntoAction)action);
                break;
            case STEP_OUT:
                debugger.stepOut((StepOutAction)action);
                break;
            case STEP_OVER:
                debugger.stepOver((StepOverAction)action);
                break;
            default:
                throw new DebuggerException("Unknown debugger action type " + action.getType());
        }
    }

    @POST
    @Path("{id}/breakpoint")
    @Consumes(MediaType.APPLICATION_JSON)
    public void addBreakpoint(@PathParam("id") String sessionId, BreakpointDto breakpoint) throws DebuggerException {
        debuggerManager.getDebugger(sessionId).addBreakpoint(breakpoint);
    }

    @GET
    @Path("{id}/breakpoint")
    @Produces(MediaType.APPLICATION_JSON)
    public List<BreakpointDto> getBreakpoints(@PathParam("id") String sessionId) throws DebuggerException {
        return asBreakpointsDto(debuggerManager.getDebugger(sessionId).getAllBreakpoints());
    }

    @DELETE
    @Path("{id}/breakpoint")
    public void deleteBreakpoint(@PathParam("id") String sessionId,
                                 @QueryParam("target") String target,
                                 @QueryParam("line") @DefaultValue("0") int lineNumber) throws DebuggerException {
        if (target == null) {
            debuggerManager.getDebugger(sessionId).deleteAllBreakpoints();
        } else {
            Location location = new LocationImpl(target, lineNumber);
            debuggerManager.getDebugger(sessionId).deleteBreakpoint(location);
        }
    }

    @GET
    @Path("{id}/dump")
    @Produces(MediaType.APPLICATION_JSON)
    public StackFrameDumpDto getStackFrameDump(@PathParam("id") String sessionId) throws DebuggerException {
        return asDto(debuggerManager.getDebugger(sessionId).dumpStackFrame());
    }

    @GET
    @Path("{id}/value")
    @Produces(MediaType.APPLICATION_JSON)
    public SimpleValueDto getValue(@PathParam("id") String sessionId, @Context UriInfo uriInfo) throws DebuggerException {
        List<String> path = new ArrayList<>();

        MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();

        int i = 0;
        String item;
        while ((item = parameters.getFirst("path" + (i++))) != null) {
            path.add(item);
        }

        VariablePath variablePath = new VariablePathImpl(path);
        return asDto(debuggerManager.getDebugger(sessionId).getValue(variablePath));
    }

    @PUT
    @Path("{id}/value")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setValue(@PathParam("id") String sessionId, VariableDto variable) throws DebuggerException {
        debuggerManager.getDebugger(sessionId).setValue(variable);
    }

    @GET
    @Path("{id}/evaluation")
    @Produces(MediaType.TEXT_PLAIN)
    public String expression(@PathParam("id") String sessionId,
                             @QueryParam("expression") String expression) throws DebuggerException {
        return debuggerManager.getDebugger(sessionId).evaluate(expression);
    }
}
