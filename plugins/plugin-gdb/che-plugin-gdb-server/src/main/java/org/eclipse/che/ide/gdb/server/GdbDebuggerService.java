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
package org.eclipse.che.ide.gdb.server;

import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.debugger.shared.Breakpoint;
import org.eclipse.che.ide.ext.debugger.shared.BreakpointList;
import org.eclipse.che.ide.ext.debugger.shared.DebuggerInfo;
import org.eclipse.che.ide.ext.debugger.shared.StackFrameDump;
import org.eclipse.che.ide.ext.debugger.shared.UpdateVariableRequest;
import org.eclipse.che.ide.ext.debugger.shared.Value;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.nio.file.Paths;

/**
 * Provides access to {@link GdbDebugger} through HTTP.
 *
 * @author Anatoliy Bazko
 */
@Path("gdb/{ws-id}")
public class GdbDebuggerService {

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public DebuggerInfo isConnected(@PathParam("id") String id) throws GdbDebuggerException {
        GdbDebugger d = GdbDebugger.getInstance(id);
        return DtoFactory.getInstance().createDto(DebuggerInfo.class)
                         .withHost(d.getHost())
                         .withPort(d.getPort())
                         .withId(d.getId())
                         .withName(d.getName())
                         .withFile(d.getFile())
                         .withVersion(d.getVersion());
    }

    @GET
    @Path("connect")
    @Produces(MediaType.APPLICATION_JSON)
    public DebuggerInfo create(@QueryParam("host") String host,
                               @QueryParam("port") @DefaultValue("0") int port,
                               @QueryParam("file") String file,
                               @QueryParam("sources") String srcDirectory) throws GdbDebuggerException {
        if (srcDirectory == null) {
            srcDirectory = Paths.get(file).getParent().toString();
        }

        GdbDebugger d = GdbDebugger.newInstance(host, port, file, srcDirectory);
        return DtoFactory.getInstance().createDto(DebuggerInfo.class)
                         .withHost(d.getHost())
                         .withPort(d.getPort())
                         .withId(d.getId())
                         .withName(d.getName())
                         .withFile(d.getFile())
                         .withVersion(d.getVersion());
    }

    @GET
    @Path("start/{id}")
    public void start(@PathParam("id") String id) throws GdbDebuggerException {
        GdbDebugger.getInstance(id).start();
    }

    @GET
    @Path("disconnect/{id}")
    public void disconnect(@PathParam("id") String id) throws GdbDebuggerException {
        GdbDebugger.getInstance(id).disconnect();
    }

    @GET
    @Path("resume/{id}")
    public void resume(@PathParam("id") String id) throws GdbDebuggerException {
        GdbDebugger.getInstance(id).resume();
    }

    @POST
    @Path("breakpoints/add/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void addBreakpoint(@PathParam("id") String id, Breakpoint breakpoint) throws GdbDebuggerException {
        GdbDebugger.getInstance(id).addBreakpoint(breakpoint);
    }

    @GET
    @Path("breakpoints/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public BreakpointList getBreakpoints(@PathParam("id") String id) throws GdbDebuggerException {
        return GdbDebugger.getInstance(id).getBreakpoints();
    }

    @POST
    @Path("breakpoints/delete/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void deleteBreakpoint(@PathParam("id") String id, Breakpoint breakpoint) throws GdbDebuggerException {
        GdbDebugger.getInstance(id).deleteBreakpoint(breakpoint);
    }

    @GET
    @Path("breakpoints/delete_all/{id}")
    public void deleteAllBreakpoint(@PathParam("id") String id) throws GdbDebuggerException {
        GdbDebugger.getInstance(id).deleteAllBreakPoints();
    }

    @GET
    @Path("dump/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public StackFrameDump getStackFrameDump(@PathParam("id") String id) throws GdbDebuggerException {
        return GdbDebugger.getInstance(id).dumpStackFrame();
    }

    @POST
    @Path("value/get/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Value getValue(@PathParam("id") String id, String variable) throws GdbDebuggerException {
        return GdbDebugger.getInstance(id).getValue(variable);
    }

    @POST
    @Path("value/set/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setValue(@PathParam("id") String id, UpdateVariableRequest updateVariableRequest) throws GdbDebuggerException {
        String variable = updateVariableRequest.getVariablePath().getPath().get(0);
        String value = updateVariableRequest.getExpression();
        GdbDebugger.getInstance(id).setValue(variable, value);
    }

    @GET
    @Path("step/over/{id}")
    public void stepOver(@PathParam("id") String id) throws GdbDebuggerException {
        GdbDebugger.getInstance(id).stepOver();
    }

    @GET
    @Path("step/into/{id}")
    public void stepInto(@PathParam("id") String id) throws GdbDebuggerException {
        GdbDebugger.getInstance(id).stepInto();
    }

    @GET
    @Path("step/out/{id}")
    public void stepOut(@PathParam("id") String id) throws GdbDebuggerException {
        GdbDebugger.getInstance(id).stepOut();
    }

    @POST
    @Path("expression/{id}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String expression(@PathParam("id") String id, String expression) throws GdbDebuggerException {
        return GdbDebugger.getInstance(id).expression(expression);
    }
}
