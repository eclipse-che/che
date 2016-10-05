/*******************************************************************************
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend Technologies - initial API and implementation
 *******************************************************************************/
package zend.com.che.plugin.zdb.server;

import java.util.List;

import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.action.ResumeAction;
import org.eclipse.che.api.debug.shared.model.action.StepIntoAction;
import org.eclipse.che.api.debug.shared.model.action.StepOutAction;
import org.eclipse.che.api.debug.shared.model.action.StepOverAction;
import org.eclipse.che.api.debug.shared.model.event.BreakpointActivatedEvent;
import org.eclipse.che.api.debug.shared.model.event.DisconnectEvent;
import org.eclipse.che.api.debug.shared.model.event.SuspendEvent;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;

/**
 * Debugger delegate interface.
 * 
 * @author Bartlomiej Laczkowski
 */
public interface IDebuggerDelegate {

   /**
    * Disconnects from the process is being debugged.
    * Must be fired {@link DisconnectEvent} if succeeded.
    *
    * @throws DebuggerException
    *      if any error occur
    */
   void disconnect() throws DebuggerException;

   /**
    * Adds given breakpoint. When breakpoint is accepted by server
    * then {@link BreakpointActivatedEvent} must be fired. If breakpoint becomes
    * deferred or just ignored then no events should be fired.
    *
    * @param breakpoint
    *      the breakpoint to add
    * @throws DebuggerException
    *      if any error occur
    */
   void addBreakpoint(Breakpoint breakpoint) throws DebuggerException;

   /**
    * Deletes given breakpoint.
    *
    * @param location
    *      the location of the breakpoint to delete
    * @throws DebuggerException
    *      if any error occur
    */
   void deleteBreakpoint(Location location) throws DebuggerException;

   /**
    * Deletes all breakpoints.
    *
    * @throws DebuggerException
    *      if any error occur
    */
   void deleteAllBreakpoints() throws DebuggerException;

   /**
    * Gets all breakpoints.
    *
    * @throws DebuggerException
    *      if any error occur
    */
   List<Breakpoint> getAllBreakpoints() throws DebuggerException;

   /**
    * Gets the current value of the given variable.
    *
    * @param variablePath
    *      the path to the variable
    * @return {@link SimpleValue}
    * @throws DebuggerException
    *      if any error occur
    */
   SimpleValue getValue(VariablePath variablePath) throws DebuggerException;

   /**
    * Sets the new value {@link Variable#getValue()} of the variable {@link Variable#getVariablePath()}.
    *
    * @param variable
    *      the variable to update
    * @throws DebuggerException
    *      if any error occur
    */
   void setValue(Variable variable) throws DebuggerException;

   /**
    * Evaluates the given expression.
    *
    * @param expression
    *      the expression to evaluate
    * @return the result
    * @throws DebuggerException
    *      if any error occur
    */
   String evaluate(String expression) throws DebuggerException;

   /**
    * Performs step over action.
    * When process stops then {@link SuspendEvent} must be fired.
    *
    * @param action
    *      contains specific parameters
    * @throws DebuggerException
    *      if any error occur
    */
   void stepOver(StepOverAction action) throws DebuggerException;

   /**
    * Performs step into action.
    * When process stops then {@link SuspendEvent} must be fired.
    *
    * @param action
    *      contains specific parameters
    * @throws DebuggerException
    *      if any error occur
    */
   void stepInto(StepIntoAction action) throws DebuggerException;

   /**
    * Performs step out action.
    * When process stops then {@link SuspendEvent} must be fired.
    *
    * @param action
    *      contains specific parameters
    * @throws DebuggerException
    *      if any error occur
    */
   void stepOut(StepOutAction action) throws DebuggerException;

   /**
    * Resume application is being debugged.
    * When process stops then {@link SuspendEvent} must be fired.
    *
    * @param action
    *      contains specific parameters
    * @throws DebuggerException
    *      if any error occur
    */
   void resume(ResumeAction action) throws DebuggerException;

   /**
    * Dump values of local variables, fields and method arguments of the current frame.
    *
    * @return {@link StackFrameDump}
    * @throws DebuggerException
    *      if any error occur
    */
   StackFrameDump dumpStackFrame() throws DebuggerException;

}
