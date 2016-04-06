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
package org.eclipse.che.ide.ext.debugger.client.debug;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateEvent;
import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateHandler;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.JsPromise;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.debug.Breakpoint;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.debug.DebuggerObservable;
import org.eclipse.che.ide.debug.DebuggerObserver;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.debugger.client.fqn.FqnResolver;
import org.eclipse.che.ide.ext.debugger.client.fqn.FqnResolverFactory;
import org.eclipse.che.ide.ext.debugger.shared.BreakpointActivatedEvent;
import org.eclipse.che.ide.ext.debugger.shared.BreakpointEvent;
import org.eclipse.che.ide.ext.debugger.shared.DebuggerEvent;
import org.eclipse.che.ide.ext.debugger.shared.DebuggerEventList;
import org.eclipse.che.ide.ext.debugger.shared.DebuggerInfo;
import org.eclipse.che.ide.ext.debugger.shared.Location;
import org.eclipse.che.ide.ext.debugger.shared.StackFrameDump;
import org.eclipse.che.ide.ext.debugger.shared.StepEvent;
import org.eclipse.che.ide.ext.debugger.shared.UpdateVariableRequest;
import org.eclipse.che.ide.ext.debugger.shared.Value;
import org.eclipse.che.ide.ext.debugger.shared.Variable;
import org.eclipse.che.ide.ext.debugger.shared.VariablePath;
import org.eclipse.che.ide.rest.HTTPStatus;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.util.storage.LocalStorage;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.ide.websocket.rest.exceptions.ServerException;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.ext.debugger.shared.DebuggerEvent.BREAKPOINT;
import static org.eclipse.che.ide.ext.debugger.shared.DebuggerEvent.BREAKPOINT_ACTIVATED;
import static org.eclipse.che.ide.ext.debugger.shared.DebuggerEvent.DISCONNECTED;
import static org.eclipse.che.ide.ext.debugger.shared.DebuggerEvent.STEP;

/**
 * The common debugger.
 *
 * @author Anatoliy Bazko
 */
public abstract class AbstractDebugger implements Debugger, DebuggerObservable {

    public static final String LOCAL_STORAGE_DEBUGGER_KEY = "che-debugger";

    protected final DtoFactory dtoFactory;

    private final List<DebuggerObserver> observers;
    private final DebuggerServiceClient  service;
    private final LocalStorageProvider   localStorageProvider;
    private final EventBus               eventBus;
    private final FqnResolverFactory     fqnResolverFactory;
    private final ActiveFileHandler      activeFileHandler;
    private final DebuggerManager        debuggerManager;
    private final String                 id;
    private final String                 eventChannel;

    /** Channel identifier to receive events from debugger over WebSocket. */
    private String debuggerEventsChannel;

    private DebuggerInfo                           debuggerInfo;
    private Location                               currentLocation;
    private SubscriptionHandler<DebuggerEventList> debuggerEventsHandler;
    private FileTypeRegistry                       fileTypeRegistry;

    private MessageBus messageBus;

    public AbstractDebugger(DebuggerServiceClient service,
                            DtoFactory dtoFactory,
                            LocalStorageProvider localStorageProvider,
                            MessageBusProvider messageBusProvider,
                            EventBus eventBus,
                            FqnResolverFactory fqnResolverFactory,
                            ActiveFileHandler activeFileHandler,
                            DebuggerManager debuggerManager,
                            FileTypeRegistry fileTypeRegistry,
                            String id,
                            String eventChannel) {
        this.service = service;
        this.dtoFactory = dtoFactory;
        this.localStorageProvider = localStorageProvider;
        this.eventBus = eventBus;
        this.fqnResolverFactory = fqnResolverFactory;
        this.activeFileHandler = activeFileHandler;
        this.debuggerManager = debuggerManager;
        this.observers = new ArrayList<>();
        this.fileTypeRegistry = fileTypeRegistry;
        this.id = id;
        this.eventChannel = eventChannel;

        addHandlers(messageBusProvider);
    }

    private void addHandlers(final MessageBusProvider messageBusProvider) {
        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                messageBus = messageBusProvider.getMachineMessageBus();

                restoreDebuggerInfo();
                if (isConnected()) {
                    Promise<DebuggerInfo> promise = service.getInfo(debuggerInfo.getId());
                    promise.then(new Operation<DebuggerInfo>() {
                        @Override
                        public void apply(DebuggerInfo arg) throws OperationException {
                            debuggerManager.setActiveDebugger(AbstractDebugger.this);

                            String info = debuggerInfo.getName() + " " + debuggerInfo.getVersion();
                            String address = debuggerInfo.getHost() + ":" + debuggerInfo.getPort();
                            DebuggerDescriptor debuggerDescriptor = new DebuggerDescriptor(info, address);
                            JsPromise<Void> promise = Promises.resolve(null);

                            for (DebuggerObserver observer : observers) {
                                observer.onDebuggerAttached(debuggerDescriptor, promise);
                            }
                            startCheckingEvents();
                        }
                    }).catchError(new Operation<PromiseError>() {
                        @Override
                        public void apply(PromiseError arg) throws OperationException {
                            invalidateDebuggerInfo();
                            preserveDebuggerInfo();
                        }
                    });
                }
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {}
        });

        this.debuggerEventsHandler = new SubscriptionHandler<DebuggerEventList>(new DebuggerEventListUnmarshaller(dtoFactory)) {
            @Override
            public void onMessageReceived(DebuggerEventList result) {
                onEventListReceived(result);
            }

            @Override
            public void onErrorReceived(Throwable exception) {
                try {
                    messageBus.unsubscribe(debuggerEventsChannel, this);
                } catch (WebSocketException e) {
                    Log.error(AbstractDebugger.class, e);
                }

                if (exception instanceof ServerException) {
                    ServerException serverException = (ServerException)exception;
                    if (HTTPStatus.INTERNAL_ERROR == serverException.getHTTPStatus()
                        && serverException.getMessage() != null
                        && serverException.getMessage().contains("not found")) {

                        disconnectDebugger();
                    }
                }
            }
        };
    }

    private void onEventListReceived(@NotNull DebuggerEventList eventList) {
        Location location;

        List<DebuggerEvent> events = eventList.getEvents();
        for (DebuggerEvent event : events) {
            switch (event.getType()) {
                case STEP:
                    location = ((StepEvent)event).getLocation();
                    break;
                case BREAKPOINT_ACTIVATED:
                    org.eclipse.che.ide.ext.debugger.shared.Breakpoint breakpoint = ((BreakpointActivatedEvent)event).getBreakpoint();
                    onBreakpointActivated(breakpoint.getLocation());
                    return;
                case BREAKPOINT:
                    location = ((BreakpointEvent)event).getBreakpoint().getLocation();
                    break;
                case DISCONNECTED:
                    disconnectDebugger();
                    return;
                default:
                    Log.error(AbstractDebugger.class, "Unknown type of debugger event: " + event.getType());
                    return;
            }

            final Location fLocation = location;
            if (location != null) {
                currentLocation = location;
                activeFileHandler.openFile(resolveFilePathByLocation(location),
                                           location.getClassName(),
                                           location.getLineNumber(),
                                           new AsyncCallback<VirtualFile>() {
                                               @Override
                                               public void onFailure(Throwable caught) {
                                                   for (DebuggerObserver observer : observers) {
                                                       observer.onBreakpointStopped(fLocation.getClassName(),
                                                                                    fLocation.getClassName(),
                                                                                    fLocation.getLineNumber());
                                                   }
                                               }

                                               @Override
                                               public void onSuccess(VirtualFile result) {
                                                   for (DebuggerObserver observer : observers) {
                                                       observer.onBreakpointStopped(result.getPath(),
                                                                                    fLocation.getClassName(),
                                                                                    fLocation.getLineNumber());
                                                   }
                                               }
                                           });
            }
        }
    }

    /**
     * Breakpoint became active. It might happens because of different reasons:
     * <li>breakpoint was deferred and VM eventually loaded class and added it</li>
     * <li>condition triggered</li>
     * <li>etc</li>
     */
    private void onBreakpointActivated(Location location) {
        List<String> filePaths = resolveFilePathByLocation(location);
        for (String filePath : filePaths) {
            for (DebuggerObserver observer : observers) {
                observer.onBreakpointActivated(filePath, location.getLineNumber() - 1);
            }
        }
    }

    private void startCheckingEvents() {
        debuggerEventsChannel = eventChannel + debuggerInfo.getId();
        try {
            messageBus.subscribe(debuggerEventsChannel, debuggerEventsHandler);
        } catch (WebSocketException e) {
            Log.error(DebuggerPresenter.class, e);
        }
    }

    private void stopCheckingDebugEvents() {
        try {
            if (messageBus.isHandlerSubscribed(debuggerEventsHandler, debuggerEventsChannel)) {
                messageBus.unsubscribe(debuggerEventsChannel, debuggerEventsHandler);
            }
        } catch (WebSocketException e) {
            Log.error(AbstractDebugger.class, e);
        }
    }

    @Override
    public Promise<String> getValue(String variable) {
        if (!isConnected()) {
            return Promises.reject(JsPromiseError.create("Debugger is not connected"));
        }

        Promise<Value> promise = service.getValue(debuggerInfo.getId(), dtoFactory.createDtoFromJson(variable, Variable.class));
        return promise.then(new Function<Value, String>() {
            @Override
            public String apply(Value arg) throws FunctionException {
                List<Variable> variables = arg.getVariables();
                return dtoFactory.toJson(variables);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                Log.error(AbstractDebugger.class, arg.getMessage());
                throw new OperationException(arg.getCause());
            }
        });
    }

    @Override
    public Promise<String> getStackFrameDump() {
        if (!isConnected()) {
            return Promises.reject(JsPromiseError.create("Debugger is not connected"));
        }

        Promise<StackFrameDump> promise = service.getStackFrameDump(debuggerInfo.getId());
        return promise.then(new Function<StackFrameDump, String>() {
            @Override
            public String apply(StackFrameDump arg) throws FunctionException {
                return dtoFactory.toJson(arg);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                Log.error(AbstractDebugger.class, arg.getMessage());
                throw new OperationException(arg.getCause());
            }
        });
    }

    @Override
    public void addBreakpoint(final VirtualFile file, final int lineNumber) {
        if (isConnected()) {
            Location location = dtoFactory.createDto(Location.class);
            location.setLineNumber(lineNumber + 1);

            String mediaType = fileTypeRegistry.getFileTypeByFile(file).getMimeTypes().get(0);
            final FqnResolver resolver = fqnResolverFactory.getResolver(mediaType);
            if (resolver != null) {
                location.setClassName(resolver.resolveFqn(file));
            } else {
                return;
            }

            org.eclipse.che.ide.ext.debugger.shared.Breakpoint breakpoint =
                    dtoFactory.createDto(org.eclipse.che.ide.ext.debugger.shared.Breakpoint.class);
            breakpoint.setLocation(location);
            breakpoint.setEnabled(true);

            Promise<Void> promise = service.addBreakpoint(debuggerInfo.getId(), breakpoint);
            promise.then(new Operation<Void>() {
                @Override
                public void apply(Void arg) throws OperationException {
                    Breakpoint breakpoint = new Breakpoint(Breakpoint.Type.BREAKPOINT, lineNumber, file.getPath(), file, true);
                    for (DebuggerObserver observer : observers) {
                        observer.onBreakpointAdded(breakpoint);
                    }
                }
            }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                    Log.error(AbstractDebugger.class, arg.getMessage());
                }
            });
        } else {
            Breakpoint breakpoint = new Breakpoint(Breakpoint.Type.BREAKPOINT, lineNumber, file.getPath(), file, false);
            for (DebuggerObserver observer : observers) {
                observer.onBreakpointAdded(breakpoint);
            }
        }
    }

    @Override
    public void deleteBreakpoint(final VirtualFile file, final int lineNumber) {
        if (isConnected()) {
            Location location = dtoFactory.createDto(Location.class);
            location.setLineNumber(lineNumber + 1);

            String mediaType = fileTypeRegistry.getFileTypeByFile(file).getMimeTypes().get(0);
            final FqnResolver resolver = fqnResolverFactory.getResolver(mediaType);
            if (resolver != null) {
                location.setClassName(resolver.resolveFqn(file));
            } else {
                Log.warn(AbstractDebugger.class, "FqnResolver is not found");
            }

            org.eclipse.che.ide.ext.debugger.shared.Breakpoint breakpoint =
                    dtoFactory.createDto(org.eclipse.che.ide.ext.debugger.shared.Breakpoint.class);
            breakpoint.setLocation(location);
            breakpoint.setEnabled(true);

            Promise<Void> promise = service.deleteBreakpoint(debuggerInfo.getId(), breakpoint);
            promise.then(new Operation<Void>() {
                @Override
                public void apply(Void arg) throws OperationException {
                    for (DebuggerObserver observer : observers) {
                        Breakpoint breakpoint = new Breakpoint(Breakpoint.Type.BREAKPOINT, lineNumber, file.getPath(), file, false);
                        observer.onBreakpointDeleted(breakpoint);
                    }
                }
            }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                    Log.error(AbstractDebugger.class, arg.getMessage());
                }
            });
        }
    }

    @Override
    public void deleteAllBreakpoints() {
        if (isConnected()) {
            Promise<Void> promise = service.deleteAllBreakpoints(debuggerInfo.getId());

            promise.then(new Operation<Void>() {
                @Override
                public void apply(Void arg) throws OperationException {
                    for (DebuggerObserver observer : observers) {
                        observer.onAllBreakpointsDeleted();
                    }
                }
            }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                    Log.error(AbstractDebugger.class, arg.getMessage());
                }
            });
        }
    }

    @Override
    public Promise<Void> attachDebugger(Map<String, String> connectionProperties) {
        if (isConnected()) {
            return Promises.reject(JsPromiseError.create("Debugger already connected"));
        }

        Promise<DebuggerInfo> connect = service.connect(connectionProperties);
        final DebuggerDescriptor debuggerDescriptor = toDescriptor(connectionProperties);

        Promise<Void> promise = connect.then(new Function<DebuggerInfo, Void>() {
            @Override
            public Void apply(DebuggerInfo arg) throws FunctionException {
                debuggerDescriptor.setInfo(arg.getName() + " " + arg.getVersion());

                setDebuggerInfo(arg);
                preserveDebuggerInfo();
                startCheckingEvents();

                service.start(arg.getId());
                return null;
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                Log.error(AbstractDebugger.class, arg.getMessage());
                throw new OperationException(arg.getCause());
            }
        });

        for (DebuggerObserver observer : observers) {
            observer.onDebuggerAttached(debuggerDescriptor, promise);
        }

        return promise;
    }

    @Override
    public void disconnectDebugger() {
        stopCheckingDebugEvents();

        Promise<Void> disconnect;
        if (isConnected()) {
            disconnect = service.disconnect(debuggerInfo.getId());
        } else {
            disconnect = Promises.resolve(null);
        }

        invalidateDebuggerInfo();
        preserveDebuggerInfo();

        disconnect.then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                for (DebuggerObserver observer : observers) {
                    observer.onDebuggerDisconnected();
                }
                debuggerManager.setActiveDebugger(null);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                Log.error(AbstractDebugger.class, arg.getMessage());
                for (DebuggerObserver observer : observers) {
                    observer.onDebuggerDisconnected();
                }
                debuggerManager.setActiveDebugger(null);
            }
        });
    }

    @Override
    public void stepInto() {
        if (isConnected()) {
            for (DebuggerObserver observer : observers) {
                observer.onPreStepIn();
            }
            currentLocation = null;

            Promise<Void> promise = service.stepInto(debuggerInfo.getId());
            promise.catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                    Log.error(AbstractDebugger.class, arg.getCause());
                }
            });
        }
    }

    @Override
    public void stepOver() {
        if (isConnected()) {
            for (DebuggerObserver observer : observers) {
                observer.onPreStepOver();
            }
            currentLocation = null;

            Promise<Void> promise = service.stepOver(debuggerInfo.getId());
            promise.catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                    Log.error(AbstractDebugger.class, arg.getCause());
                }
            });
        }
    }

    @Override
    public void stepOut() {
        if (isConnected()) {
            for (DebuggerObserver observer : observers) {
                observer.onPreStepOut();
            }
            currentLocation = null;

            Promise<Void> promise = service.stepOut(debuggerInfo.getId());
            promise.catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                    Log.error(AbstractDebugger.class, arg.getCause());
                }
            });
        }
    }

    @Override
    public void resume() {
        if (isConnected()) {
            for (DebuggerObserver observer : observers) {
                observer.onPreResume();
            }
            currentLocation = null;

            Promise<Void> promise = service.resume(debuggerInfo.getId());
            promise.catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                    Log.error(AbstractDebugger.class, arg.getCause());
                }
            });
        }
    }

    @Override
    public Promise<String> evaluateExpression(String expression) {
        if (isConnected()) {
            return service.evaluateExpression(debuggerInfo.getId(), expression);
        }

        return Promises.reject(JsPromiseError.create("Debugger is not connected"));
    }

    @Override
    public void changeVariableValue(final List<String> path, final String newValue) {
        if (isConnected()) {
            VariablePath variablePath = dtoFactory.createDto(VariablePath.class);
            variablePath.setPath(path);

            UpdateVariableRequest updateVariableRequest = dtoFactory.createDto(UpdateVariableRequest.class);
            updateVariableRequest.setVariablePath(variablePath);
            updateVariableRequest.setExpression(newValue);

            Promise<Void> promise = service.setValue(debuggerInfo.getId(), updateVariableRequest);

            promise.then(new Operation<Void>() {
                @Override
                public void apply(Void arg) throws OperationException {
                    for (DebuggerObserver observer : observers) {
                        observer.onValueChanged(path, newValue);
                    }
                }
            }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                    Log.error(AbstractDebugger.class, arg.getMessage());
                }
            });
        }
    }

    @Override
    public boolean isConnected() {
        return debuggerInfo != null;
    }

    @Override
    public boolean isSuspended() {
        return isConnected() && currentLocation != null;
    }

    @Override
    public void addObserver(DebuggerObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(DebuggerObserver observer) {
        observers.remove(observer);
    }

    protected void setDebuggerInfo(DebuggerInfo debuggerInfo) {
        this.debuggerInfo = debuggerInfo;
    }

    private void invalidateDebuggerInfo() {
        this.debuggerInfo = null;
    }

    /**
     * Preserves debugger information into the local storage.
     */
    protected void preserveDebuggerInfo() {
        LocalStorage localStorage = localStorageProvider.get();

        if (localStorage == null) {
            return;
        }

        String data;
        if (!isConnected()) {
            data = "";
        } else {
            data = dtoFactory.toJson(debuggerInfo);
        }

        localStorage.setItem(LOCAL_STORAGE_DEBUGGER_KEY, data);
    }

    /**
     * Loads debugger information from the local storage.
     */
    protected void restoreDebuggerInfo() {
        LocalStorage localStorage = localStorageProvider.get();
        if (localStorage == null) {
            invalidateDebuggerInfo();
            return;
        }

        String data = localStorage.getItem(LOCAL_STORAGE_DEBUGGER_KEY);
        if (data == null || data.isEmpty()) {
            invalidateDebuggerInfo();
            return;
        }

        DebuggerInfo debuggerInfo = dtoFactory.createDtoFromJson(data, DebuggerInfo.class);
        setDebuggerInfo(debuggerInfo);
    }

    /**
     * Create file path from {@link Location}.
     *
     * @param location
     *         location of class
     * @return file path
     */
    abstract protected List<String> resolveFilePathByLocation(@NotNull Location location);

    abstract protected DebuggerDescriptor toDescriptor(Map<String, String> connectionProperties);
}
