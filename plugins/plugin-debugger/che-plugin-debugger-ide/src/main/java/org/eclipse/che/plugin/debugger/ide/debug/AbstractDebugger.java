/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.debugger.ide.debug;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.common.base.Optional;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.debug.shared.dto.BreakpointDto;
import org.eclipse.che.api.debug.shared.dto.DebugSessionDto;
import org.eclipse.che.api.debug.shared.dto.LocationDto;
import org.eclipse.che.api.debug.shared.dto.SimpleValueDto;
import org.eclipse.che.api.debug.shared.dto.StackFrameDumpDto;
import org.eclipse.che.api.debug.shared.dto.ThreadDumpDto;
import org.eclipse.che.api.debug.shared.dto.VariableDto;
import org.eclipse.che.api.debug.shared.dto.VariablePathDto;
import org.eclipse.che.api.debug.shared.dto.action.ResumeActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StartActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepIntoActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepOutActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepOverActionDto;
import org.eclipse.che.api.debug.shared.dto.action.SuspendActionDto;
import org.eclipse.che.api.debug.shared.dto.event.BreakpointActivatedEventDto;
import org.eclipse.che.api.debug.shared.dto.event.DebuggerEventDto;
import org.eclipse.che.api.debug.shared.dto.event.DisconnectEventDto;
import org.eclipse.che.api.debug.shared.dto.event.SuspendEventDto;
import org.eclipse.che.api.debug.shared.model.DebuggerInfo;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.action.Action;
import org.eclipse.che.api.debug.shared.model.impl.SimpleValueImpl;
import org.eclipse.che.api.debug.shared.model.impl.StackFrameDumpImpl;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.debug.Breakpoint;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.debug.DebuggerServiceClient;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.debug.DebuggerObservable;
import org.eclipse.che.ide.debug.DebuggerObserver;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.util.storage.LocalStorage;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;

/**
 * The common debugger.
 *
 * @author Anatoliy Bazko
 */
public abstract class AbstractDebugger implements Debugger, DebuggerObservable {
  public static final String LOCAL_STORAGE_DEBUGGER_SESSION_KEY = "che-debugger-session";
  public static final String LOCAL_STORAGE_DEBUGGER_STATE_KEY = "che-debugger-state";
  public static final String WS_AGENT_ENDPOINT = "ws-agent";

  public static final String EVENT_DEBUGGER_MESSAGE_BREAKPOINT = "event:debugger:breakpoint";
  public static final String EVENT_DEBUGGER_MESSAGE_DISCONNECT = "event:debugger:disconnect";
  public static final String EVENT_DEBUGGER_MESSAGE_SUSPEND = "event:debugger:suspend";
  public static final String EVENT_DEBUGGER_UN_SUBSCRIBE = "event:debugger:un-subscribe";
  public static final String EVENT_DEBUGGER_SUBSCRIBE = "event:debugger:subscribe";

  protected final DtoFactory dtoFactory;
  protected final NotificationManager notificationManager;
  private final List<DebuggerObserver> observers;

  private final RequestTransmitter transmitter;
  private final RequestHandlerConfigurator configurator;
  private final DebuggerServiceClient service;
  private final LocalStorageProvider localStorageProvider;
  private final EventBus eventBus;
  private final DebuggerResourceHandler debuggerResourceHandler;
  private final DebuggerManager debuggerManager;
  private final BreakpointManager breakpointManager;
  private final String debuggerType;
  private final RequestHandlerManager requestHandlerManager;

  private DebugSessionDto debugSessionDto;
  private Location currentLocation;

  public AbstractDebugger(
      DebuggerServiceClient service,
      RequestTransmitter transmitter,
      RequestHandlerConfigurator configurator,
      DtoFactory dtoFactory,
      LocalStorageProvider localStorageProvider,
      EventBus eventBus,
      DebuggerManager debuggerManager,
      NotificationManager notificationManager,
      BreakpointManager breakpointManager,
      RequestHandlerManager requestHandlerManager,
      DebuggerResourceHandlerFactory debuggerResourceHandlerFactory,
      String type) {
    this.service = service;
    this.transmitter = transmitter;
    this.configurator = configurator;
    this.dtoFactory = dtoFactory;
    this.localStorageProvider = localStorageProvider;
    this.eventBus = eventBus;
    this.debuggerResourceHandler = debuggerResourceHandlerFactory.getOrDefault(getDebuggerType());
    this.debuggerManager = debuggerManager;
    this.notificationManager = notificationManager;
    this.breakpointManager = breakpointManager;
    this.observers = new ArrayList<>();
    this.debuggerType = type;
    this.requestHandlerManager = requestHandlerManager;

    restoreDebuggerState();
    addHandlers();
  }

  private void addHandlers() {
    eventBus.addHandler(
        WsAgentStateEvent.TYPE,
        new WsAgentStateHandler() {
          @Override
          public void onWsAgentStarted(WsAgentStateEvent event) {
            subscribeToDebuggerEvents();

            if (!isConnected()) {
              return;
            }
            Promise<DebugSessionDto> promise = service.getSessionInfo(debugSessionDto.getId());
            promise
                .then(
                    debugSessionDto -> {
                      debuggerManager.setActiveDebugger(AbstractDebugger.this);
                      setDebugSession(debugSessionDto);

                      DebuggerInfo debuggerInfo = debugSessionDto.getDebuggerInfo();
                      String info = debuggerInfo.getName() + " " + debuggerInfo.getVersion();
                      String address = debuggerInfo.getHost() + ":" + debuggerInfo.getPort();
                      DebuggerDescriptor debuggerDescriptor = new DebuggerDescriptor(info, address);

                      for (DebuggerObserver observer : observers) {
                        observer.onDebuggerAttached(debuggerDescriptor, Promises.resolve(null));
                      }

                      for (BreakpointDto breakpoint : debugSessionDto.getBreakpoints()) {
                        onBreakpointActivated(breakpoint.getLocation());
                      }

                      if (currentLocation != null) {
                        open(currentLocation);
                      }

                      startCheckingEvents();
                    })
                .catchError(
                    error -> {
                      disconnect();
                    });
          }

          @Override
          public void onWsAgentStopped(WsAgentStateEvent event) {}
        });
  }

  private void onEventListReceived(@NotNull DebuggerEventDto event) {

    switch (event.getType()) {
      case SUSPEND:
        currentLocation = ((SuspendEventDto) event).getLocation();
        open(currentLocation);
        break;
      case BREAKPOINT_ACTIVATED:
        BreakpointDto breakpointDto = ((BreakpointActivatedEventDto) event).getBreakpoint();
        onBreakpointActivated(breakpointDto.getLocation());
        return;
      case DISCONNECT:
        disconnect();
        return;
      default:
        Log.error(
            AbstractDebugger.class, "Unknown debuggerType of debugger event: " + event.getType());
        return;
    }

    preserveDebuggerState();
  }

  private void open(Location location) {
    try {
      debuggerResourceHandler.open(
          location,
          new AsyncCallback<VirtualFile>() {
            @Override
            public void onFailure(Throwable caught) {
              for (DebuggerObserver observer : observers) {
                observer.onBreakpointStopped(location.getTarget(), location);
              }
            }

            @Override
            public void onSuccess(VirtualFile result) {
              for (DebuggerObserver observer : observers) {
                observer.onBreakpointStopped(result.getLocation().toString(), location);
              }
            }
          });
    } catch (Exception e) {
      for (DebuggerObserver observer : observers) {
        observer.onBreakpointStopped(location.getTarget(), location);
      }
    }
  }

  /**
   * Breakpoint became active. It might happens because of different reasons:
   * <li>breakpoint was deferred and VM eventually loaded class and added it
   * <li>condition triggered
   * <li>etc
   */
  private void onBreakpointActivated(LocationDto locationDto) {
    String filePath = fqnToPath(locationDto);
    for (DebuggerObserver observer : observers) {
      observer.onBreakpointActivated(filePath, locationDto.getLineNumber() - 1);
    }
  }

  private void startCheckingEvents() {
    if (!requestHandlerManager.isRegistered(EVENT_DEBUGGER_MESSAGE_SUSPEND)) {
      configurator
          .newConfiguration()
          .methodName(EVENT_DEBUGGER_MESSAGE_SUSPEND)
          .paramsAsDto(SuspendEventDto.class)
          .noResult()
          .withBiConsumer(
              (endpointId, event) -> {
                Log.debug(getClass(), "Received suspend message from endpoint: " + endpointId);
                onEventListReceived(event);
              });
    }

    if (!requestHandlerManager.isRegistered(EVENT_DEBUGGER_MESSAGE_DISCONNECT)) {
      configurator
          .newConfiguration()
          .methodName(EVENT_DEBUGGER_MESSAGE_DISCONNECT)
          .paramsAsDto(DisconnectEventDto.class)
          .noResult()
          .withBiConsumer(
              (endpointId, event) -> {
                Log.debug(getClass(), "Received disconnect message from endpoint: " + endpointId);
                onEventListReceived(event);
              });
    }

    if (!requestHandlerManager.isRegistered(EVENT_DEBUGGER_MESSAGE_BREAKPOINT)) {
      configurator
          .newConfiguration()
          .methodName(EVENT_DEBUGGER_MESSAGE_BREAKPOINT)
          .paramsAsDto(BreakpointActivatedEventDto.class)
          .noResult()
          .withBiConsumer(
              (endpointId, event) -> {
                Log.debug(
                    getClass(),
                    "Received breakpoint activated message from endpoint: " + endpointId);
                onEventListReceived(event);
              });
    }
  }

  private void subscribeToDebuggerEvents() {
    transmitter
        .newRequest()
        .endpointId(WS_AGENT_ENDPOINT)
        .methodName(EVENT_DEBUGGER_SUBSCRIBE)
        .noParams()
        .sendAndSkipResult();
  }

  private void unsubscribeFromDebuggerEvents() {
    transmitter
        .newRequest()
        .endpointId(WS_AGENT_ENDPOINT)
        .methodName(EVENT_DEBUGGER_UN_SUBSCRIBE)
        .noParams()
        .sendAndSkipResult();
  }

  @Override
  public Promise<SimpleValue> getValue(Variable variable, long threadId, int frameIndex) {
    if (!isConnected()) {
      return Promises.reject(JsPromiseError.create("Debugger is not connected"));
    }

    Promise<SimpleValueDto> promise =
        service.getValue(debugSessionDto.getId(), toDto(variable), threadId, frameIndex);
    return promise.then((Function<SimpleValueDto, SimpleValue>) SimpleValueImpl::new);
  }

  @Override
  public Promise<StackFrameDump> getStackFrameDump(long threadId, int frameIndex) {
    if (!isConnected()) {
      return Promises.reject(JsPromiseError.create("Debugger is not connected"));
    }

    Promise<StackFrameDumpDto> stackFrameDump =
        service.getStackFrameDump(debugSessionDto.getId(), threadId, frameIndex);
    return stackFrameDump.then(
        (Function<StackFrameDumpDto, StackFrameDump>) StackFrameDumpImpl::new);
  }

  @Override
  public Promise<List<ThreadDumpDto>> getThreadDump() {
    if (!isConnected()) {
      return Promises.reject(JsPromiseError.create("Debugger is not connected"));
    }

    return service.getThreadDump(debugSessionDto.getId());
  }

  @Override
  public void addBreakpoint(final VirtualFile file, final int lineNumber) {
    if (isConnected()) {
      String fqn = pathToFqn(file);
      if (fqn == null) {
        return;
      }

      final String filePath = file.getLocation().toString();
      LocationDto locationDto =
          dtoFactory
              .createDto(LocationDto.class)
              .withLineNumber(lineNumber + 1)
              .withTarget(fqn)
              .withResourcePath(filePath)
              .withResourceProjectPath(getProject(file).getPath());

      BreakpointDto breakpointDto =
          dtoFactory.createDto(BreakpointDto.class).withLocation(locationDto).withEnabled(true);

      Promise<Void> promise = service.addBreakpoint(debugSessionDto.getId(), breakpointDto);
      promise
          .then(
              it -> {
                Breakpoint breakpoint =
                    new Breakpoint(Breakpoint.Type.BREAKPOINT, lineNumber, filePath, file, true);
                for (DebuggerObserver observer : observers) {
                  observer.onBreakpointAdded(breakpoint);
                }
              })
          .catchError(
              error -> {
                Log.error(AbstractDebugger.class, error.getMessage());
              });
    } else {
      Breakpoint breakpoint =
          new Breakpoint(
              Breakpoint.Type.BREAKPOINT, lineNumber, file.getLocation().toString(), file, false);
      for (DebuggerObserver observer : observers) {
        observer.onBreakpointAdded(breakpoint);
      }
    }
  }

  @Override
  public void deleteBreakpoint(final VirtualFile file, final int lineNumber) {
    if (!isConnected()) {
      return;
    }
    LocationDto locationDto = dtoFactory.createDto(LocationDto.class);
    locationDto.setLineNumber(lineNumber + 1);

    String fqn = pathToFqn(file);
    if (fqn == null) {
      return;
    }
    locationDto.setTarget(fqn);

    Promise<Void> promise = service.deleteBreakpoint(debugSessionDto.getId(), locationDto);
    promise
        .then(
            it -> {
              for (DebuggerObserver observer : observers) {
                Breakpoint breakpoint =
                    new Breakpoint(
                        Breakpoint.Type.BREAKPOINT,
                        lineNumber,
                        file.getLocation().toString(),
                        file,
                        false);
                observer.onBreakpointDeleted(breakpoint);
              }
            })
        .catchError(
            error -> {
              Log.error(AbstractDebugger.class, error.getMessage());
            });
  }

  @Override
  public void deleteAllBreakpoints() {
    if (!isConnected()) {
      return;
    }
    Promise<Void> promise = service.deleteAllBreakpoints(debugSessionDto.getId());

    promise
        .then(
            it -> {
              for (DebuggerObserver observer : observers) {
                observer.onAllBreakpointsDeleted();
              }
            })
        .catchError(
            error -> {
              Log.error(AbstractDebugger.class, error.getMessage());
            });
  }

  @Override
  public Promise<Void> connect(Map<String, String> connectionProperties) {
    if (isConnected()) {
      return Promises.reject(JsPromiseError.create("Debugger already connected"));
    }

    Promise<DebugSessionDto> connect = service.connect(debuggerType, connectionProperties);
    final DebuggerDescriptor debuggerDescriptor = toDescriptor(connectionProperties);

    Promise<Void> promise =
        connect
            .then(
                (Function<DebugSessionDto, Void>)
                    debugSession -> {
                      DebuggerInfo debuggerInfo = debugSession.getDebuggerInfo();
                      debuggerDescriptor.setInfo(
                          debuggerInfo.getName() + " " + debuggerInfo.getVersion());

                      setDebugSession(debugSession);
                      preserveDebuggerState();

                      subscribeToDebuggerEvents();
                      startCheckingEvents();

                      startDebugger(debugSession);

                      return null;
                    })
            .catchError(
                (Operation<PromiseError>)
                    error -> {
                      Log.error(AbstractDebugger.class, error.getMessage());
                      throw new OperationException(error.getCause());
                    });

    for (DebuggerObserver observer : observers) {
      observer.onDebuggerAttached(debuggerDescriptor, promise);
    }

    return promise;
  }

  protected void startDebugger(final DebugSessionDto debugSessionDto) {
    List<BreakpointDto> breakpoints = new ArrayList<>();
    for (Breakpoint breakpoint : breakpointManager.getBreakpointList()) {
      LocationDto locationDto =
          dtoFactory
              .createDto(LocationDto.class)
              .withLineNumber(breakpoint.getLineNumber() + 1)
              .withResourcePath(breakpoint.getPath())
              .withResourceProjectPath(getProject(breakpoint.getFile()).getPath());

      String target = pathToFqn(breakpoint.getFile());
      if (target != null) {
        locationDto.setTarget(target);

        BreakpointDto breakpointDto = dtoFactory.createDto(BreakpointDto.class);
        breakpointDto.setLocation(locationDto);
        breakpointDto.setEnabled(true);

        breakpoints.add(breakpointDto);
      }
    }

    StartActionDto action = dtoFactory.createDto(StartActionDto.class);
    action.setType(Action.TYPE.START);
    action.setBreakpoints(breakpoints);

    service.start(debugSessionDto.getId(), action);
  }

  @Override
  public void disconnect() {
    unsubscribeFromDebuggerEvents();

    Promise<Void> disconnect;
    if (isConnected()) {
      disconnect = service.disconnect(debugSessionDto.getId());
    } else {
      disconnect = Promises.resolve(null);
    }

    invalidateDebugSession();
    preserveDebuggerState();

    disconnect
        .then(
            it -> {
              for (DebuggerObserver observer : observers) {
                observer.onDebuggerDisconnected();
              }
              debuggerManager.setActiveDebugger(null);
            })
        .catchError(
            error -> {
              for (DebuggerObserver observer : observers) {
                observer.onDebuggerDisconnected();
              }
              debuggerManager.setActiveDebugger(null);
            });
  }

  @Override
  public void stepInto() {
    if (isConnected()) {
      for (DebuggerObserver observer : observers) {
        observer.onPreStepInto();
      }
      removeCurrentLocation();
      preserveDebuggerState();

      StepIntoActionDto action = dtoFactory.createDto(StepIntoActionDto.class);
      action.setType(Action.TYPE.STEP_INTO);

      Promise<Void> promise = service.stepInto(debugSessionDto.getId(), action);
      promise.catchError(
          error -> {
            Log.error(AbstractDebugger.class, error.getCause());
          });
    }
  }

  @Override
  public void stepOver() {
    if (isConnected()) {
      for (DebuggerObserver observer : observers) {
        observer.onPreStepOver();
      }
      removeCurrentLocation();
      preserveDebuggerState();

      StepOverActionDto action = dtoFactory.createDto(StepOverActionDto.class);
      action.setType(Action.TYPE.STEP_OVER);

      Promise<Void> promise = service.stepOver(debugSessionDto.getId(), action);
      promise.catchError(
          error -> {
            Log.error(AbstractDebugger.class, error.getCause());
          });
    }
  }

  @Override
  public void stepOut() {
    if (isConnected()) {
      for (DebuggerObserver observer : observers) {
        observer.onPreStepOut();
      }
      removeCurrentLocation();
      preserveDebuggerState();

      StepOutActionDto action = dtoFactory.createDto(StepOutActionDto.class);
      action.setType(Action.TYPE.STEP_OUT);

      Promise<Void> promise = service.stepOut(debugSessionDto.getId(), action);
      promise.catchError(
          error -> {
            Log.error(AbstractDebugger.class, error.getCause());
          });
    }
  }

  @Override
  public void resume() {
    if (isConnected()) {
      for (DebuggerObserver observer : observers) {
        observer.onPreResume();
      }
      removeCurrentLocation();
      preserveDebuggerState();

      ResumeActionDto action = dtoFactory.createDto(ResumeActionDto.class);
      action.setType(Action.TYPE.RESUME);

      Promise<Void> promise = service.resume(debugSessionDto.getId(), action);
      promise.catchError(
          error -> {
            Log.error(AbstractDebugger.class, error.getCause());
          });
    }
  }

  @Override
  public void suspend() {
    if (!isConnected()) {
      return;
    }

    SuspendActionDto suspendAction = dtoFactory.createDto(SuspendActionDto.class);
    suspendAction.setType(Action.TYPE.SUSPEND);

    service
        .suspend(debugSessionDto.getId(), suspendAction)
        .catchError(
            error -> {
              notificationManager.notify(error.getMessage(), FAIL, FLOAT_MODE);
            });
  }

  @Override
  public Promise<String> evaluate(String expression, long threadId, int frameIndex) {
    if (isConnected()) {
      return service.evaluate(debugSessionDto.getId(), expression, threadId, frameIndex);
    }

    return Promises.reject(JsPromiseError.create("Debugger is not connected"));
  }

  @Override
  public void setValue(final Variable variable, long threadId, int frameIndex) {
    if (isConnected()) {
      Promise<Void> promise =
          service.setValue(debugSessionDto.getId(), toDto(variable), threadId, frameIndex);

      promise
          .then(
              it -> {
                for (DebuggerObserver observer : observers) {
                  observer.onValueChanged(
                      variable.getVariablePath().getPath(), variable.getValue().getString());
                }
              })
          .catchError(
              error -> {
                Log.error(AbstractDebugger.class, error.getMessage());
              });
    }
  }

  @Override
  public boolean isConnected() {
    return debugSessionDto != null;
  }

  @Override
  public boolean isSuspended() {
    return isConnected() && currentLocation != null;
  }

  @Override
  public String getDebuggerType() {
    return debuggerType;
  }

  @Override
  public void addObserver(DebuggerObserver observer) {
    observers.add(observer);
  }

  @Override
  public void removeObserver(DebuggerObserver observer) {
    observers.remove(observer);
  }

  protected void setDebugSession(DebugSessionDto debugSessionDto) {
    this.debugSessionDto = debugSessionDto;
  }

  private void invalidateDebugSession() {
    this.debugSessionDto = null;
    this.removeCurrentLocation();
  }

  private void removeCurrentLocation() {
    currentLocation = null;
  }

  /** Preserves debugger information into the local storage. */
  protected void preserveDebuggerState() {
    LocalStorage localStorage = localStorageProvider.get();

    if (localStorage == null) {
      return;
    }

    if (!isConnected()) {
      localStorage.setItem(LOCAL_STORAGE_DEBUGGER_SESSION_KEY, "");
      localStorage.setItem(LOCAL_STORAGE_DEBUGGER_STATE_KEY, "");
    } else {
      localStorage.setItem(LOCAL_STORAGE_DEBUGGER_SESSION_KEY, dtoFactory.toJson(debugSessionDto));
      if (currentLocation == null) {
        localStorage.setItem(LOCAL_STORAGE_DEBUGGER_STATE_KEY, "");
      } else {
        localStorage.setItem(LOCAL_STORAGE_DEBUGGER_STATE_KEY, dtoFactory.toJson(currentLocation));
      }
    }
  }

  /** Loads debugger information from the local storage. */
  protected void restoreDebuggerState() {
    invalidateDebugSession();

    LocalStorage localStorage = localStorageProvider.get();
    if (localStorage == null) {
      return;
    }

    String data = localStorage.getItem(LOCAL_STORAGE_DEBUGGER_SESSION_KEY);
    if (data != null && !data.isEmpty()) {
      DebugSessionDto debugSessionDto = dtoFactory.createDtoFromJson(data, DebugSessionDto.class);
      if (!debugSessionDto.getType().equals(getDebuggerType())) {
        return;
      }

      setDebugSession(debugSessionDto);
    }

    data = localStorage.getItem(LOCAL_STORAGE_DEBUGGER_STATE_KEY);
    if (data != null && !data.isEmpty()) {
      currentLocation = dtoFactory.createDtoFromJson(data, LocationDto.class);
    }
  }

  private List<VariableDto> toDto(List<? extends Variable> variables) {
    if (variables == null || variables.isEmpty()) {
      return Collections.emptyList();
    }

    List<VariableDto> dtos = new ArrayList<>(variables.size());
    for (Variable v : variables) {
      dtos.add(toDto(v));
    }
    return dtos;
  }

  private VariableDto toDto(Variable variable) {
    SimpleValue simpleValue = variable.getValue();
    VariablePath variablePath = variable.getVariablePath();
    VariableDto dto = dtoFactory.createDto(VariableDto.class);
    dto.withValue(
        dtoFactory
            .createDto(SimpleValueDto.class)
            .withString(simpleValue.getString())
            .withVariables(toDto(simpleValue.getVariables())));
    dto.withVariablePath(
        dtoFactory.createDto(VariablePathDto.class).withPath(variablePath.getPath()));
    dto.withPrimitive(variable.isPrimitive());
    dto.withType(variable.getType());
    dto.withName(variable.getName());

    return dto;
  }

  @Nullable
  private Project getProject(VirtualFile virtualFile) {
    if (virtualFile instanceof Resource) {
      Optional<Project> projectOptional = ((Resource) virtualFile).getRelatedProject();
      if (projectOptional.isPresent()) {
        return projectOptional.get();
      }
    }
    return null;
  }

  /** Transforms FQN to file path. */
  protected abstract String fqnToPath(@NotNull Location location);

  /** Transforms file path to FQN> */
  @Nullable
  protected abstract String pathToFqn(VirtualFile file);

  protected abstract DebuggerDescriptor toDescriptor(Map<String, String> connectionProperties);
}
