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
package org.eclipse.che.api.debugger.server;


import org.eclipse.che.api.debug.shared.dto.BreakpointDto;
import org.eclipse.che.api.debug.shared.dto.DebugSessionDto;
import org.eclipse.che.api.debug.shared.dto.DebuggerInfoDto;
import org.eclipse.che.api.debug.shared.dto.FieldDto;
import org.eclipse.che.api.debug.shared.dto.LocationDto;
import org.eclipse.che.api.debug.shared.dto.SimpleValueDto;
import org.eclipse.che.api.debug.shared.dto.StackFrameDumpDto;
import org.eclipse.che.api.debug.shared.dto.VariableDto;
import org.eclipse.che.api.debug.shared.dto.VariablePathDto;
import org.eclipse.che.api.debug.shared.dto.event.BreakpointActivatedEventDto;
import org.eclipse.che.api.debug.shared.dto.event.DebuggerEventDto;
import org.eclipse.che.api.debug.shared.dto.event.DisconnectEventDto;
import org.eclipse.che.api.debug.shared.dto.event.SuspendEventDto;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.DebugSession;
import org.eclipse.che.api.debug.shared.model.DebuggerInfo;
import org.eclipse.che.api.debug.shared.model.Field;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.event.BreakpointActivatedEvent;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.event.SuspendEvent;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Helps to convert to/from DTOs related to debugger.
 *
 * @author Anatoliy Bazko
 */
public final class DtoConverter {

    public static DebuggerInfoDto asDto(DebuggerInfo debuggerInfo) {
        return newDto(DebuggerInfoDto.class).withPort(debuggerInfo.getPort())
                                            .withFile(debuggerInfo.getFile())
                                            .withHost(debuggerInfo.getHost())
                                            .withName(debuggerInfo.getName())
                                            .withVersion(debuggerInfo.getVersion())
                                            .withPid(debuggerInfo.getPid());
    }

    public static DebugSessionDto asDto(DebugSession debugSession) {
        return newDto(DebugSessionDto.class).withDebuggerInfo(asDto(debugSession.getDebuggerInfo()))
                                            .withId(debugSession.getId())
                                            .withType(debugSession.getType());
    }

    public static BreakpointDto asDto(Breakpoint breakpoint) {
        return newDto(BreakpointDto.class).withCondition(breakpoint.getCondition())
                                          .withEnabled(breakpoint.isEnabled())
                                          .withLocation(asDto(breakpoint.getLocation()));
    }

    public static List<BreakpointDto> asBreakpointsDto(List<Breakpoint> breakpoints) {
        return breakpoints.stream().map(DtoConverter::asDto).collect(Collectors.toList());
    }

    public static LocationDto asDto(Location location) {
        return newDto(LocationDto.class).withTarget(location.getTarget())
                                        .withLineNumber(location.getLineNumber())
                                        .withExternalResourceId(location.getExternalResourceId())
                                        .withResourcePath(location.getResourcePath())
                                        .withResourceProjectPath(location.getResourceProjectPath())
                                        .withExternalResource(location.isExternalResource());
    }

    public static SimpleValueDto asDto(SimpleValue value) {
        return newDto(SimpleValueDto.class).withValue(value.getValue())
                                     .withVariables(asVariablesDto(value.getVariables()));
    }

    public static FieldDto asDto(Field field) {
        return newDto(FieldDto.class).withType(field.getType())
                                     .withExistInformation(field.isExistInformation())
                                     .withName(field.getName())
                                     .withPrimitive(field.isPrimitive())
                                     .withValue(field.getValue())
                                     .withVariablePath(asDto(field.getVariablePath()))
                                     .withVariables(asVariablesDto(field.getVariables()))
                                     .withIsFinal(field.isIsFinal())
                                     .withIsStatic(field.isIsStatic())
                                     .withIsTransient(field.isIsTransient())
                                     .withIsVolatile(field.isIsVolatile());
    }

    public static List<FieldDto> asFieldsDto(List<? extends Field> fields) {
        return fields.stream().map(DtoConverter::asDto).collect(Collectors.toList());
    }

    public static List<VariableDto> asVariablesDto(List<? extends Variable> variables) {
        return variables.stream().map(DtoConverter::asDto).collect(Collectors.toList());
    }

    public static VariableDto asDto(Variable variable) {
        return newDto(VariableDto.class).withType(variable.getType())
                                        .withExistInformation(variable.isExistInformation())
                                        .withName(variable.getName())
                                        .withPrimitive(variable.isPrimitive())
                                        .withValue(variable.getValue())
                                        .withVariablePath(asDto(variable.getVariablePath()))
                                        .withVariables(variable.getVariables().isEmpty() ? Collections.emptyList()
                                                                                         : asVariablesDto(variable.getVariables()));
    }

    public static VariablePathDto asDto(VariablePath variablePath) {
        return newDto(VariablePathDto.class).withPath(variablePath.getPath());
    }

    public static StackFrameDumpDto asDto(StackFrameDump stackFrameDump) {
        return newDto(StackFrameDumpDto.class).withVariables(asVariablesDto(stackFrameDump.getVariables()))
                                              .withFields(asFieldsDto(stackFrameDump.getFields()));
    }

    public static DebuggerEventDto asDto(DebuggerEvent debuggerEvent) {
        switch (debuggerEvent.getType()) {
            case DISCONNECT:
                return newDto(DisconnectEventDto.class).withType(DebuggerEvent.TYPE.DISCONNECT);
            case SUSPEND:
                return newDto(SuspendEventDto.class).withType(DebuggerEvent.TYPE.SUSPEND)
                                                    .withLocation(asDto(((SuspendEvent)debuggerEvent).getLocation()));
            case BREAKPOINT_ACTIVATED:
                return newDto(BreakpointActivatedEventDto.class)
                        .withType(DebuggerEvent.TYPE.BREAKPOINT_ACTIVATED)
                        .withBreakpoint(asDto(((BreakpointActivatedEvent)debuggerEvent).getBreakpoint()));
            default:
                throw new IllegalArgumentException("Illegal event type " + debuggerEvent.getType().toString());
        }
    }


    private DtoConverter() {}
}
