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
package org.eclipse.che.plugin.jdb.server;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMCannotBeModifiedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.InvalidRequestStateException;
import com.sun.jdi.request.StepRequest;

import org.eclipse.che.api.debug.shared.dto.BreakpointDto;
import org.eclipse.che.api.debug.shared.dto.FieldDto;
import org.eclipse.che.api.debug.shared.dto.LocationDto;
import org.eclipse.che.api.debug.shared.dto.StackFrameDumpDto;
import org.eclipse.che.api.debug.shared.dto.VariableDto;
import org.eclipse.che.api.debug.shared.dto.VariablePathDto;
import org.eclipse.che.api.debug.shared.dto.action.ResumeActionDto;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.DebuggerInfo;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.action.ResumeAction;
import org.eclipse.che.api.debug.shared.model.action.StartAction;
import org.eclipse.che.api.debug.shared.model.action.StepIntoAction;
import org.eclipse.che.api.debug.shared.model.action.StepOutAction;
import org.eclipse.che.api.debug.shared.model.action.StepOverAction;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.DebuggerInfoImpl;
import org.eclipse.che.api.debug.shared.model.impl.FieldImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.SimpleValueImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariableImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.BreakpointActivatedEventImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.DisconnectEventImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.SuspendEventImpl;
import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.plugin.jdb.server.exceptions.DebuggerAbsentInformationException;
import org.eclipse.che.plugin.jdb.server.expression.Evaluator;
import org.eclipse.che.plugin.jdb.server.expression.ExpressionException;
import org.eclipse.che.plugin.jdb.server.expression.ExpressionParser;
import org.eclipse.che.plugin.jdb.server.utils.JavaDebuggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Connects to JVM over Java Debug Wire Protocol handle its events. All methods of this class may throws
 * DebuggerException. Typically such exception caused by errors in underlying JDI (Java Debug Interface), e.g.
 * connection errors. Instance of Debugger is not thread-safe.
 *
 * @author andrew00x
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 */
public class JavaDebugger implements EventsHandler, Debugger {
    private static final Logger            LOG          = LoggerFactory.getLogger(JavaDebugger.class);
    private static final JavaDebuggerUtils debuggerUtil = new JavaDebuggerUtils();

    private final String           host;
    private final int              port;
    private final DebuggerCallback debuggerCallback;

    /**
     * A mapping of source file names to breakpoints. This mapping is used to set
     * breakpoints in files that haven't been loaded yet by a target Java VM.
     */
    private final ConcurrentMap<String, List<Breakpoint>> deferredBreakpoints = new ConcurrentHashMap<>();

    /** Stores ClassPrepareRequests to prevent making duplicate class prepare requests. */
    private final ConcurrentMap<String, ClassPrepareRequest> classPrepareRequests = new ConcurrentHashMap<>();

    /** Target Java VM representation. */
    private VirtualMachine  vm;
    private EventsCollector eventsCollector;

    /** Current thread. Not <code>null</code> is thread suspended, e.g breakpoint reached. */
    private ThreadReference thread;
    /** Current stack frame. Not <code>null</code> is thread suspended, e.g breakpoint reached. */
    private JdiStackFrame   stackFrame;
    /** Lock for synchronization debug processes. */
    private Lock lock = new ReentrantLock();

    /**
     * Create debugger and connect it to the JVM which already running at the specified host and port.
     *
     * @param host
     *         the host where JVM running
     * @param port
     *         the Java Debug Wire Protocol (JDWP) port
     * @throws DebuggerException
     *         when connection to Java VM is not established
     */
    JavaDebugger(String host, int port, DebuggerCallback debuggerCallback) throws DebuggerException {
        this.host = host;
        this.port = port;
        this.debuggerCallback = debuggerCallback;
        connect();
    }

    /**
     * Attach to a JVM that is already running at specified host.
     *
     * @throws DebuggerException
     *         when connection to Java VM is not established
     */
    private void connect() throws DebuggerException {
        final String connectorName = "com.sun.jdi.SocketAttach";
        AttachingConnector connector = connector(connectorName);
        if (connector == null) {
            throw new DebuggerException(
                    String.format("Unable connect to target Java VM. Requested connector '%s' not found. ", connectorName));
        }
        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        arguments.get("hostname").setValue(host);
        ((Connector.IntegerArgument)arguments.get("port")).setValue(port);
        int attempt = 0;
        for (; ; ) {
            try {
                Thread.sleep(2000);
                vm = connector.attach(arguments);
                vm.suspend();
                break;
            } catch (UnknownHostException | IllegalConnectorArgumentsException e) {
                throw new DebuggerException(e.getMessage(), e);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
                if (++attempt > 10) {
                    throw new DebuggerException(e.getMessage(), e);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
            } catch (InterruptedException ignored) {
            }
        }
        eventsCollector = new EventsCollector(vm.eventQueue(), this);
        LOG.debug("Connect {}:{}", host, port);
    }

    private AttachingConnector connector(String connectorName) {
        for (AttachingConnector c : Bootstrap.virtualMachineManager().attachingConnectors()) {
            if (connectorName.equals(c.name())) {
                return c;
            }
        }
        return null;
    }

    @Override
    public DebuggerInfo getInfo() throws DebuggerException {
        return new DebuggerInfoImpl(host, port, vm.name(), vm.version(), 0, null);
    }

    @Override
    public void start(StartAction action) throws DebuggerException {
        for (Breakpoint b : action.getBreakpoints()) {
            try {
                addBreakpoint(b);
            } catch (DebuggerException e) {
                // can't add breakpoint, skip it
            }
        }
        vm.resume();
    }

    @Override
    public void disconnect() throws DebuggerException {
        vm.dispose();
        LOG.debug("Close connection to {}:{}", host, port);
    }

    @Override
    public void addBreakpoint(Breakpoint breakpoint) throws DebuggerException {
        final String className = findFQN(breakpoint);
        final int lineNumber = breakpoint.getLocation().getLineNumber();
        List<ReferenceType> classes = vm.classesByName(className);
        // it may mean that class doesn't loaded by a target JVM yet
        if (classes.isEmpty()) {
            deferBreakpoint(breakpoint);
            throw new DebuggerException("Class not loaded");
        }

        ReferenceType clazz = classes.get(0);
        List<com.sun.jdi.Location> locations;
        try {
            locations = clazz.locationsOfLine(lineNumber);
        } catch (AbsentInformationException | ClassNotPreparedException e) {
            throw new DebuggerException(e.getMessage(), e);
        }

        if (locations.isEmpty()) {
            throw new DebuggerException("Line " + lineNumber + " not found in class " + className);
        }

        com.sun.jdi.Location location = locations.get(0);
        if (location.method() == null) {
            // Line is out of method.
            throw new DebuggerException("Invalid line " + lineNumber + " in class " + className);
        }

        // Ignore new breakpoint if already have breakpoint at the same location.
        EventRequestManager requestManager = getEventManager();
        for (BreakpointRequest breakpointRequest : requestManager.breakpointRequests()) {
            if (location.equals(breakpointRequest.location())) {
                LOG.debug("Breakpoint at {} already set", location);
                return;
            }
        }

        try {
            EventRequest breakPointRequest = requestManager.createBreakpointRequest(location);
            breakPointRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
            String expression = breakpoint.getCondition();
            if (!(expression == null || expression.isEmpty())) {
                ExpressionParser parser = ExpressionParser.newInstance(expression);
                breakPointRequest.putProperty("org.eclipse.che.ide.java.debug.condition.expression.parser", parser);
            }
            breakPointRequest.setEnabled(true);
        } catch (NativeMethodException | IllegalThreadStateException | InvalidRequestStateException e) {
            throw new DebuggerException(e.getMessage(), e);
        }

        debuggerCallback.onEvent(
                new BreakpointActivatedEventImpl(
                        new BreakpointImpl(breakpoint.getLocation(),
                                           true,
                                           breakpoint.getCondition())));

        LOG.debug("Add breakpoint: {}", location);
    }

    private String findFQN(Breakpoint breakpoint) throws DebuggerException {
        Location location = breakpoint.getLocation();
        final String parentFqn = location.getTarget();
        final String projectPath = location.getResourceProjectPath();
        int lineNumber = location.getLineNumber();

        return debuggerUtil.findFqnByPosition(projectPath, parentFqn, lineNumber);
    }

    private void deferBreakpoint(Breakpoint breakpoint) throws DebuggerException {
        final String className = breakpoint.getLocation().getTarget();
        List<Breakpoint> newList = new ArrayList<>();
        List<Breakpoint> list = deferredBreakpoints.putIfAbsent(className, newList);
        if (list == null) {
            list = newList;
        }
        list.add(breakpoint);

        // start listening for the load of the type
        if (!classPrepareRequests.containsKey(className)) {
            ClassPrepareRequest request = getEventManager().createClassPrepareRequest();
            // set class filter in order to reduce the amount of event traffic sent from the target VM to the debugger VM
            request.addClassFilter(className);
            request.enable();
            classPrepareRequests.put(className, request);
        }

        LOG.debug("Deferred breakpoint: {}", breakpoint.getLocation().toString());
    }

    @Override
    public List<Breakpoint> getAllBreakpoints() throws DebuggerException {
        List<BreakpointRequest> breakpointRequests;
        try {
            breakpointRequests = getEventManager().breakpointRequests();
        } catch (DebuggerException e) {
            Throwable cause = e.getCause();
            if (cause instanceof VMCannotBeModifiedException) {
                // If target VM in read-only state then list of break point always empty.
                return Collections.emptyList();
            }
            throw e;
        }
        List<Breakpoint> breakPoints = new ArrayList<>(breakpointRequests.size());
        for (BreakpointRequest breakpointRequest : breakpointRequests) {
            com.sun.jdi.Location location = breakpointRequest.location();
            // Breakpoint always enabled at the moment. Managing states of breakpoint is not supported for now.
            breakPoints.add(newDto(BreakpointDto.class).withEnabled(true)
                                                       .withLocation(newDto(LocationDto.class).withTarget(location.declaringType().name())
                                                                                              .withLineNumber(location.lineNumber())));
        }
        Collections.sort(breakPoints, BREAKPOINT_COMPARATOR);
        return breakPoints;
    }

    private static final Comparator<Breakpoint> BREAKPOINT_COMPARATOR = new BreakPointComparator();

    @Override
    public void deleteBreakpoint(Location location) throws DebuggerException {
        final String className = location.getTarget();
        final int lineNumber = location.getLineNumber();
        EventRequestManager requestManager = getEventManager();
        List<BreakpointRequest> snapshot = new ArrayList<>(requestManager.breakpointRequests());
        for (BreakpointRequest breakpointRequest : snapshot) {
            com.sun.jdi.Location jdiLocation = breakpointRequest.location();
            if (jdiLocation.declaringType().name().equals(className) && jdiLocation.lineNumber() == lineNumber) {
                requestManager.deleteEventRequest(breakpointRequest);
                LOG.debug("Delete breakpoint: {}", location);
            }
        }
    }

    @Override
    public void deleteAllBreakpoints() throws DebuggerException {
        getEventManager().deleteAllBreakpoints();
    }

    @Override
    public void resume(ResumeAction action) throws DebuggerException {
        lock.lock();
        try {
            invalidateCurrentThread();
            vm.resume();
            LOG.debug("Resume VM");
        } catch (VMCannotBeModifiedException e) {
            throw new DebuggerException(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public StackFrameDumpDto dumpStackFrame() throws DebuggerException {
        lock.lock();
        try {
            final JdiStackFrame currentFrame = getCurrentFrame();
            StackFrameDumpDto dump = newDto(StackFrameDumpDto.class);
            boolean existInformation = true;
            JdiLocalVariable[] variables = new JdiLocalVariable[0];
            try {
                variables = currentFrame.getLocalVariables();
            } catch (DebuggerAbsentInformationException e) {
                existInformation = false;
            }
            for (JdiField f : currentFrame.getFields()) {
                List<String> variablePath = asList(f.isStatic() ? "static" : "this", f.getName());
                dump.getFields().add(newDto(FieldDto.class).withIsFinal(f.isFinal())
                                                           .withIsStatic(f.isStatic())
                                                           .withIsTransient(f.isTransient())
                                                           .withIsVolatile(f.isVolatile())
                                                           .withName(f.getName())
                                                           .withExistInformation(existInformation)
                                                           .withValue(f.getValue().getAsString())
                                                           .withType(f.getTypeName())
                                                           .withVariablePath(newDto(VariablePathDto.class).withPath(variablePath))
                                                           .withPrimitive(f.isPrimitive()));
            }
            for (JdiLocalVariable var : variables) {
                dump.getVariables().add(newDto(VariableDto.class).withName(var.getName())
                                                                 .withExistInformation(existInformation)
                                                                 .withValue(var.getValue().getAsString())
                                                                 .withType(var.getTypeName())
                                                                 .withVariablePath(
                                                                         newDto(VariablePathDto.class)
                                                                                 .withPath(singletonList(var.getName()))
                                                                 )
                                                                 .withPrimitive(var.isPrimitive()));
            }
            return dump;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get value of variable with specified path. Each item in path is name of variable.
     * <p>
     * Path must be specified according to the following rules:
     * <ol>
     * <li>If need to get field of this object of current frame then first element in array always should be
     * 'this'.</li>
     * <li>If need to get static field in current frame then first element in array always should be 'static'.</li>
     * <li>If need to get local variable in current frame then first element should be the name of local variable.</li>
     * </ol>
     * </p>
     * Here is example. <br/>
     * Assume we have next hierarchy of classes and breakpoint set in line: <i>// breakpoint</i>:
     * <pre>
     *    class A {
     *       private String str;
     *       ...
     *    }
     *
     *    class B {
     *       private A a;
     *       ....
     *
     *       void method() {
     *          A var = new A();
     *          var.setStr(...);
     *          a = var;
     *          // breakpoint
     *       }
     *    }
     * </pre>
     * There are two ways to access variable <i>str</i> in class <i>A</i>:
     * <ol>
     * <li>Through field <i>a</i> in class <i>B</i>: ['this', 'a', 'str']</li>
     * <li>Through local variable <i>var</i> in method <i>B.method()</i>: ['var', 'str']</li>
     * </ol>
     *
     * @param variablePath
     *         path to variable
     * @return variable or <code>null</code> if variable not found
     * @throws DebuggerException
     *         when any other errors occur when try to access the variable
     */
    @Override
    public SimpleValue getValue(VariablePath variablePath) throws DebuggerException {
        List<String> path = variablePath.getPath();
        if (path.size() == 0) {
            throw new IllegalArgumentException("Path to value may not be empty. ");
        }
        JdiVariable variable;
        int offset;
        if ("this".equals(path.get(0)) || "static".equals(path.get(0))) {
            if (path.size() < 2) {
                throw new IllegalArgumentException("Name of field required. ");
            }
            variable = getCurrentFrame().getFieldByName(path.get(1));
            offset = 2;
        } else {
            try {
                variable = getCurrentFrame().getLocalVariableByName(path.get(0));
            } catch (DebuggerAbsentInformationException e) {
                return null;
            }
            offset = 1;
        }

        for (int i = offset; variable != null && i < path.size(); i++) {
            variable = variable.getValue().getVariableByName(path.get(i));
        }

        if (variable == null) {
            return null;
        }

        List<Variable> variables = new ArrayList<>();
        for (JdiVariable ch : variable.getValue().getVariables()) {
            VariablePathDto chPath = newDto(VariablePathDto.class).withPath(new ArrayList<>(path));
            chPath.getPath().add(ch.getName());
            if (ch instanceof JdiField) {
                JdiField f = (JdiField)ch;
                variables.add(new FieldImpl(f.getName(),
                                            true,
                                            f.getValue().getAsString(),
                                            f.getTypeName(),
                                            f.isPrimitive(),
                                            Collections.<Variable>emptyList(),
                                            chPath,
                                            f.isFinal(),
                                            f.isStatic(),
                                            f.isTransient(),
                                            f.isVolatile()));
            } else {
                // Array element.
                variables.add(new VariableImpl(ch.getTypeName(),
                                               ch.getName(),
                                               ch.getValue().getAsString(),
                                               ch.isPrimitive(),
                                               chPath,
                                               Collections.emptyList(),
                                               true));
            }
        }
        return new SimpleValueImpl(variables, variable.getValue().getAsString());
    }

    @Override
    public void setValue(Variable variable) throws DebuggerException {
        StringBuilder expression = new StringBuilder();
        for (String s : variable.getVariablePath().getPath()) {
            if ("static".equals(s)) {
                continue;
            }
            // Here we need !s.startsWith("[") condition because
            // we shouldn't add '.' between arrayName and index of a element
            // For example we can receive ["arrayName", "[index]"]
            if (expression.length() > 0 && !s.startsWith("[")) {
                expression.append('.');
            }
            expression.append(s);
        }
        expression.append('=');
        expression.append(variable.getValue());
        evaluate(expression.toString());
    }

    @Override
    public void handleEvents(com.sun.jdi.event.EventSet eventSet) throws DebuggerException {
        boolean resume = true;
        try {
            for (com.sun.jdi.event.Event event : eventSet) {
                LOG.debug("New event: {}", event);
                if (event instanceof com.sun.jdi.event.BreakpointEvent) {
                    lock.lock();
                    try {
                        resume = processBreakPointEvent((com.sun.jdi.event.BreakpointEvent)event);
                    } finally {
                        lock.unlock();
                    }
                } else if (event instanceof com.sun.jdi.event.StepEvent) {
                    lock.lock();
                    try {
                        resume = processStepEvent((com.sun.jdi.event.StepEvent)event);
                    } finally {
                        lock.unlock();
                    }
                } else if (event instanceof com.sun.jdi.event.VMDisconnectEvent) {
                    resume = processDisconnectEvent();
                } else if (event instanceof com.sun.jdi.event.ClassPrepareEvent) {
                    resume = processClassPrepareEvent((com.sun.jdi.event.ClassPrepareEvent)event);
                }
            }
        } finally {
            if (resume) {
                eventSet.resume();
            }
        }
    }

    private boolean processBreakPointEvent(com.sun.jdi.event.BreakpointEvent event) throws DebuggerException {
        setCurrentThread(event.thread());
        boolean hitBreakpoint;
        ExpressionParser parser =
                (ExpressionParser)event.request().getProperty("org.eclipse.che.ide.java.debug.condition.expression.parser");
        if (parser != null) {
            com.sun.jdi.Value result = evaluate(parser);
            hitBreakpoint = result instanceof com.sun.jdi.BooleanValue && ((com.sun.jdi.BooleanValue)result).value();
        } else {
            // If there is no expression.
            hitBreakpoint = true;
        }

        if (hitBreakpoint) {
            com.sun.jdi.Location jdiLocation = event.location();

            Location location;
            try {
                location = debuggerUtil.getLocation(jdiLocation);
            } catch (DebuggerException e) {
                location = new LocationImpl(jdiLocation.declaringType().name(), jdiLocation.lineNumber());
            }

            debuggerCallback.onEvent(new SuspendEventImpl(location));
        }

        // Left target JVM in suspended state if result of evaluation of expression is boolean value and true
        // or if condition expression is not set.
        return !hitBreakpoint;
    }

    private boolean processStepEvent(com.sun.jdi.event.StepEvent event) throws DebuggerException {
        setCurrentThread(event.thread());
        com.sun.jdi.Location jdiLocation = event.location();

        Location location = debuggerUtil.getLocation(jdiLocation);
        debuggerCallback.onEvent(new SuspendEventImpl(location));
        return false;
    }

    private boolean processDisconnectEvent() {
        debuggerCallback.onEvent(new DisconnectEventImpl());
        eventsCollector.stop();
        return true;
    }

    private boolean processClassPrepareEvent(com.sun.jdi.event.ClassPrepareEvent event) throws DebuggerException {
        setCurrentThread(event.thread());
        final String className = event.referenceType().name();

        // add deferred breakpoints
        List<Breakpoint> breakpointsToAdd = deferredBreakpoints.get(className);
        if (breakpointsToAdd != null) {

            for (Breakpoint b : breakpointsToAdd) {
                addBreakpoint(b);
            }
            deferredBreakpoints.remove(className);

            // All deferred breakpoints for className have been already added,
            // so no need to listen for an appropriate ClassPrepareRequests any more.
            ClassPrepareRequest request = classPrepareRequests.remove(className);
            if (request != null) {
                getEventManager().deleteEventRequest(request);
            }
        }
        return true;
    }

    @Override
    public void stepOver(StepOverAction action) throws DebuggerException {
        doStep(StepRequest.STEP_OVER);
    }

    @Override
    public void stepInto(StepIntoAction action) throws DebuggerException {
        doStep(StepRequest.STEP_INTO);
    }

    @Override
    public void stepOut(StepOutAction action) throws DebuggerException {
        doStep(StepRequest.STEP_OUT);
    }

    private void doStep(int depth) throws DebuggerException {
        lock.lock();
        try {
            clearSteps();

            StepRequest request = getEventManager().createStepRequest(getCurrentThread(), StepRequest.STEP_LINE, depth);
            request.addCountFilter(1);
            request.enable();

            resume(newDto(ResumeActionDto.class));
        } finally {
            lock.unlock();
        }
    }

    private void clearSteps() throws DebuggerException {
        List<StepRequest> snapshot = new ArrayList<>(getEventManager().stepRequests());
        for (StepRequest stepRequest : snapshot) {
            if (stepRequest.thread().equals(getCurrentThread())) {
                getEventManager().deleteEventRequest(stepRequest);
            }
        }
    }

    @Override
    public String evaluate(String expression) throws DebuggerException {
        com.sun.jdi.Value result = evaluate(ExpressionParser.newInstance(expression));
        return result == null ? "null" : result.toString();
    }

    private com.sun.jdi.Value evaluate(ExpressionParser parser) throws DebuggerException {
        final long startTime = System.currentTimeMillis();
        try {
            return parser.evaluate(new Evaluator(vm, getCurrentThread()));
        } catch (ExpressionException e) {
            throw new DebuggerException(e.getMessage(), e);
        } finally {
            final long endTime = System.currentTimeMillis();
            LOG.debug("==>> Evaluate time: {} ms", (endTime - startTime));
            // Evaluation of expression may update state of frame.
            invalidateCurrentFrame();
        }
    }

    private ThreadReference getCurrentThread() throws DebuggerException {
        if (thread == null) {
            throw new DebuggerException("Target Java VM is not suspended. ");
        }
        return thread;
    }

    private JdiStackFrame getCurrentFrame() throws DebuggerException {
        if (stackFrame != null) {
            return stackFrame;
        }
        try {
            stackFrame = new JdiStackFrameImpl(getCurrentThread().frame(0));
        } catch (IncompatibleThreadStateException e) {
            throw new DebuggerException("Thread is not suspended. ", e);
        }
        return stackFrame;
    }

    private void setCurrentThread(ThreadReference t) {
        stackFrame = null;
        thread = t;
    }

    private void invalidateCurrentFrame() {
        stackFrame = null;
    }

    private void invalidateCurrentThread() {
        this.thread = null;
        invalidateCurrentFrame();
    }

    private EventRequestManager getEventManager() throws DebuggerException {
        try {
            return vm.eventRequestManager();
        } catch (VMCannotBeModifiedException e) {
            throw new DebuggerException(e.getMessage(), e);
        }
    }
}
