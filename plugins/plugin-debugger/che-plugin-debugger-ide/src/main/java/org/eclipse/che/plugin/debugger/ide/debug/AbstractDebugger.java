/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.debugger.ide.debug;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

import com.google.common.base.Strings;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.debug.shared.dto.BreakpointConfigurationDto;
import org.eclipse.che.api.debug.shared.dto.BreakpointDto;
import org.eclipse.che.api.debug.shared.dto.DebugSessionDto;
import org.eclipse.che.api.debug.shared.dto.LocationDto;
import org.eclipse.che.api.debug.shared.dto.MethodDto;
import org.eclipse.che.api.debug.shared.dto.SimpleValueDto;
import org.eclipse.che.api.debug.shared.dto.ThreadStateDto;
import org.eclipse.che.api.debug.shared.dto.VariableDto;
import org.eclipse.che.api.debug.shared.dto.VariablePathDto;
import org.eclipse.che.api.debug.shared.dto.action.ResumeActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StartActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepIntoActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepOutActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepOverActionDto;
import org.eclipse.che.api.debug.shared.dto.action.SuspendActionDto;
import org.eclipse.che.api.debug.shared.dto.event.BreakpointActivatedEventDto;
import org.eclipse.che.api.debug.shared.dto.event.DisconnectEventDto;
import org.eclipse.che.api.debug.shared.dto.event.SuspendEventDto;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.BreakpointConfiguration;
import org.eclipse.che.api.debug.shared.model.DebuggerInfo;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.Method;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.action.Action;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.debug.DebuggerServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.api.workspace.event.WorkspaceRunningEvent;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.debug.DebuggerObservable;
import org.eclipse.che.ide.debug.DebuggerObserver;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.util.storage.LocalStorage;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;

/**
 * The common debugger.
 *
 * @author Anatoliy Bazko
 */
public abstract class AbstractDebugger implements Debugger, DebuggerObservable {
  public static final String LOCAL_STORAGE_DEBUGGER_SESSION_KEY = "che-debugger-session";
  public static final String LOCAL_STORAGE_DEBUGGER_STATE_KEY = "che-debugger-state";
  public static final String LOCAL_STORAGE_DEBUGGER_DISPOSABLE_BREAKPOINT_KEY =
      "che-debugger-disposable-breakpoint";

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
  private final DebuggerLocationHandlerManager debuggerLocationHandlerManager;
  private final DebuggerManager debuggerManager;
  private final BreakpointManager breakpointManager;
  private final String debuggerType;
  private final RequestHandlerManager requestHandlerManager;
  private final DebuggerLocalizationConstant constant;
  private final PromiseProvider promiseProvider;

  private DebugSessionDto debugSessionDto;
  private SuspendEventDto suspendEventDto;
  private BreakpointDto disposableBreakpoint;

  public AbstractDebugger(
      DebuggerServiceClient service,
      RequestTransmitter transmitter,
      RequestHandlerConfigurator configurator,
      DtoFactory dtoFactory,
      LocalStorageProvider localStorageProvider,
      EventBus eventBus,
      DebuggerManager debuggerManager,
      NotificationManager notificationManager,
      AppContext appContext,
      BreakpointManager breakpointManager,
      DebuggerLocalizationConstant constant,
      RequestHandlerManager requestHandlerManager,
      DebuggerLocationHandlerManager debuggerLocationHandlerManager,
      PromiseProvider promiseProvider,
      String type) {
    this.service = service;
    this.transmitter = transmitter;
    this.configurator = configurator;
    this.dtoFactory = dtoFactory;
    this.localStorageProvider = localStorageProvider;
    this.debuggerLocationHandlerManager = debuggerLocationHandlerManager;
    this.debuggerManager = debuggerManager;
    this.notificationManager = notificationManager;
    this.breakpointManager = breakpointManager;
    this.constant = constant;
    this.promiseProvider = promiseProvider;
    this.observers = new ArrayList<>();
    this.debuggerType = type;
    this.requestHandlerManager = requestHandlerManager;

    restoreDebuggerState();

    eventBus.addHandler(WorkspaceRunningEvent.TYPE, e -> initialize());

    if (appContext.getWorkspace().getStatus() == RUNNING) {
      initialize();
    }
  }

  private void initialize() {
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
                observer.onDebuggerAttached(debuggerDescriptor);
              }

              for (BreakpointDto breakpoint : debugSessionDto.getBreakpoints()) {
                onBreakpointActivated(breakpoint.getLocation());
              }

              if (suspendEventDto != null) {
                debuggerLocationHandlerManager
                    .getOrDefault(suspendEventDto.getLocation())
                    .find(
                        suspendEventDto.getLocation(),
                        new AsyncCallback<VirtualFile>() {
                          @Override
                          public void onFailure(Throwable caught) {
                            for (DebuggerObserver observer : observers) {
                              observer.onBreakpointStopped(
                                  suspendEventDto.getLocation().getTarget(),
                                  suspendEventDto.getLocation());
                            }
                          }

                          @Override
                          public void onSuccess(VirtualFile result) {
                            for (DebuggerObserver observer : observers) {
                              observer.onBreakpointStopped(
                                  result.getLocation().toString(), suspendEventDto.getLocation());
                            }
                          }
                        });
              }

              startCheckingEvents();
            })
        .catchError(
            error -> {
              disconnect();
            });
  }

  private void onEventListReceived(@NotNull DebuggerEvent event) {

    switch (event.getType()) {
      case SUSPEND:
        suspendEventDto = ((SuspendEventDto) event);
        open(suspendEventDto.getLocation());
        removeDisposableBreakpoint();
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

  private void removeDisposableBreakpoint() {
    if (disposableBreakpoint != null) {
      deleteBreakpoint(disposableBreakpoint);
    }
    invalidateDisposableBreakpoint();
    preserveDebuggerState();
  }

  private void open(Location location) {
    debuggerLocationHandlerManager
        .getOrDefault(location)
        .open(
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
  }

  /**
   * Breakpoint became active. It might happens because of different reasons:
   * <li>breakpoint was deferred and VM eventually loaded class and added it
   * <li>condition triggered
   * <li>etc
   */
  private void onBreakpointActivated(LocationDto locationDto) {
    for (DebuggerObserver observer : observers) {
      observer.onBreakpointActivated(locationDto.getTarget(), locationDto.getLineNumber());
    }
  }

  protected void startCheckingEvents() {
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

  protected void subscribeToDebuggerEvents() {
    transmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName(EVENT_DEBUGGER_SUBSCRIBE)
        .noParams()
        .sendAndSkipResult();
  }

  protected void unsubscribeFromDebuggerEvents() {
    transmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName(EVENT_DEBUGGER_UN_SUBSCRIBE)
        .noParams()
        .sendAndSkipResult();
  }

  @Override
  public Promise<? extends SimpleValue> getValue(Variable variable, long threadId, int frameIndex) {
    if (!isConnected()) {
      return Promises.reject(JsPromiseError.create("Debugger is not connected"));
    }

    return service.getValue(debugSessionDto.getId(), toDto(variable), threadId, frameIndex);
  }

  @Override
  public Promise<? extends StackFrameDump> getStackFrameDump(long threadId, int frameIndex) {
    if (!isConnected()) {
      return Promises.reject(JsPromiseError.create("Debugger is not connected"));
    }

    return service.getStackFrameDump(debugSessionDto.getId(), threadId, frameIndex);
  }

  @Override
  public Promise<List<ThreadStateDto>> getThreadDump() {
    if (!isConnected()) {
      promiseProvider.reject("Debugger is not connected");
    }

    return service.getThreadDump(debugSessionDto.getId());
  }

  @Override
  public Breakpoint createBreakpoint(VirtualFile file, int lineNumber) {
    return new BreakpointImpl(new LocationImpl(file.getLocation().toString(), lineNumber));
  }

  @Override
  public Promise<Void> addBreakpoint(final Breakpoint breakpoint) {
    if (!isConnected()) {
      promiseProvider.reject("Debugger is not connected");
    }

    return service
        .addBreakpoint(debugSessionDto.getId(), toDto(breakpoint))
        .then(
            it -> {
              for (DebuggerObserver observer : observers) {
                observer.onBreakpointAdded(breakpoint);
              }
            })
        .catchError(
            error -> {
              Log.error(AbstractDebugger.class, error.getMessage());
            });
  }

  @Override
  public Promise<Void> deleteBreakpoint(final Breakpoint breakpoint) {
    if (!isConnected()) {
      promiseProvider.reject("Debugger is not connected");
    }

    return service
        .deleteBreakpoint(debugSessionDto.getId(), toDto(breakpoint.getLocation()))
        .then(
            it -> {
              for (DebuggerObserver observer : observers) {
                observer.onBreakpointDeleted(breakpoint);
              }
            })
        .catchError(
            error -> {
              Log.error(AbstractDebugger.class, error.getMessage());
            });
  }

  @Override
  public Promise<Void> deleteAllBreakpoints() {
    if (!isConnected()) {
      promiseProvider.reject("Debugger is not connected");
    }

    return service
        .deleteAllBreakpoints(debugSessionDto.getId())
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
  public Promise<List<? extends Breakpoint>> getAllBreakpoints() {
    if (!isConnected()) {
      promiseProvider.reject("Debugger is not connected");
    }

    return service
        .getAllBreakpoints(debugSessionDto.getId())
        .thenPromise(
            breakpoints ->
                promiseProvider.resolve(
                    breakpoints.stream().map(BreakpointImpl::new).collect(Collectors.toList())));
  }

  @Override
  public Promise<Void> connect(Map<String, String> connectionProperties) {
    if (isConnected()) {
      return Promises.reject(JsPromiseError.create("Debugger already connected"));
    }

    Promise<DebugSessionDto> connect = service.connect(debuggerType, connectionProperties);
    final DebuggerDescriptor debuggerDescriptor = toDescriptor(connectionProperties);

    final StatusNotification notification =
        notificationManager.notify(
            constant.debuggerConnectingTitle(debuggerDescriptor.getAddress()),
            PROGRESS,
            FLOAT_MODE);

    return connect
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

                  for (DebuggerObserver observer : observers) {
                    observer.onDebuggerAttached(debuggerDescriptor);
                  }

                  startDebugger(debugSession);
                  notification.setTitle(constant.debuggerConnectedTitle());
                  notification.setContent(
                      constant.debuggerConnectedDescription(debuggerDescriptor.getAddress()));
                  notification.setStatus(SUCCESS);
                  return null;
                })
        .catchError(
            error -> {
              debuggerManager.setActiveDebugger(null);
              notification.setTitle(
                  constant.failedToConnectToRemoteDebuggerDescription(
                      debuggerDescriptor.getAddress(), error.getMessage()));
              notification.setStatus(FAIL);
              notification.setDisplayMode(FLOAT_MODE);
            });
  }

  protected void startDebugger(final DebugSessionDto debugSessionDto) {
    List<BreakpointDto> breakpoints =
        breakpointManager.getAll().stream().map(this::toDto).collect(Collectors.toList());

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

    invalidateDisposableBreakpoint();
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
      StepIntoActionDto action = dtoFactory.createDto(StepIntoActionDto.class);
      action.setType(Action.TYPE.STEP_INTO);
      action.setSuspendPolicy(suspendEventDto.getSuspendPolicy());

      removeCurrentLocation();
      preserveDebuggerState();

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
      StepOverActionDto action = dtoFactory.createDto(StepOverActionDto.class);
      action.setType(Action.TYPE.STEP_OVER);
      action.setSuspendPolicy(suspendEventDto.getSuspendPolicy());

      preserveDebuggerState();
      removeCurrentLocation();

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
      StepOutActionDto action = dtoFactory.createDto(StepOutActionDto.class);
      action.setType(Action.TYPE.STEP_OUT);
      action.setSuspendPolicy(suspendEventDto.getSuspendPolicy());

      removeCurrentLocation();
      preserveDebuggerState();

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
      ResumeActionDto action = dtoFactory.createDto(ResumeActionDto.class);
      action.setType(Action.TYPE.RESUME);

      removeCurrentLocation();
      preserveDebuggerState();

      Promise<Void> promise = service.resume(debugSessionDto.getId(), action);
      promise.catchError(
          error -> {
            Log.error(AbstractDebugger.class, error.getCause());
          });
    }
  }

  @Override
  public void runToLocation(Location location) {
    if (!isConnected()) {
      return;
    }

    if (disposableBreakpoint != null) {
      return;
    }

    disposableBreakpoint =
        dtoFactory.createDto(BreakpointDto.class).withLocation(toDto(location)).withEnabled(true);

    service
        .addBreakpoint(debugSessionDto.getId(), disposableBreakpoint)
        .then(
            it -> {
              preserveDebuggerState();
              resume();
            })
        .catchError(
            error -> {
              service
                  .deleteBreakpoint(debugSessionDto.getId(), disposableBreakpoint.getLocation())
                  .catchError(
                      err -> {
                        Log.error(AbstractDebugger.class, err.getMessage());
                      });

              invalidateDisposableBreakpoint();
              preserveDebuggerState();
            });
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
  public void setValue(final Variable variable, final long threadId, final int frameIndex) {
    if (isConnected()) {
      Promise<Void> promise =
          service.setValue(debugSessionDto.getId(), toDto(variable), threadId, frameIndex);

      promise
          .then(
              it -> {
                for (DebuggerObserver observer : observers) {
                  observer.onValueChanged(variable, threadId, frameIndex);
                }
              })
          .catchError(
              error -> {
                Log.error(AbstractDebugger.class, error.getMessage());
              });
    }
  }

  @Override
  public Promise<? extends Location> getStackFrameLocation(long threadId, int frameIndex) {
    if (isConnected()) {
      return service.getStackFrameLocation(debugSessionDto.getId(), threadId, frameIndex);
    }

    return promiseProvider.reject(JsPromiseError.create("Debugger is not connected"));
  }

  @Override
  public boolean isConnected() {
    return debugSessionDto != null;
  }

  @Override
  public boolean isSuspended() {
    return isConnected() && suspendEventDto != null;
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

  protected void setSuspendEvent(SuspendEventDto suspendEventDto) {
    this.suspendEventDto = suspendEventDto;
  }

  private void invalidateDebugSession() {
    this.debugSessionDto = null;
    this.removeCurrentLocation();
  }

  private void removeCurrentLocation() {
    suspendEventDto = null;
  }

  private void invalidateDisposableBreakpoint() {
    disposableBreakpoint = null;
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
      localStorage.setItem(LOCAL_STORAGE_DEBUGGER_DISPOSABLE_BREAKPOINT_KEY, "");
    } else {
      localStorage.setItem(LOCAL_STORAGE_DEBUGGER_SESSION_KEY, dtoFactory.toJson(debugSessionDto));

      if (suspendEventDto == null) {
        localStorage.setItem(LOCAL_STORAGE_DEBUGGER_STATE_KEY, "");
      } else {
        localStorage.setItem(LOCAL_STORAGE_DEBUGGER_STATE_KEY, dtoFactory.toJson(suspendEventDto));
      }

      if (disposableBreakpoint == null) {
        localStorage.setItem(LOCAL_STORAGE_DEBUGGER_DISPOSABLE_BREAKPOINT_KEY, "");
      } else {
        localStorage.setItem(
            LOCAL_STORAGE_DEBUGGER_DISPOSABLE_BREAKPOINT_KEY,
            dtoFactory.toJson(disposableBreakpoint));
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
    if (!Strings.isNullOrEmpty(data)) {
      DebugSessionDto debugSessionDto = dtoFactory.createDtoFromJson(data, DebugSessionDto.class);
      if (!debugSessionDto.getType().equals(getDebuggerType())) {
        return;
      }

      setDebugSession(debugSessionDto);
    }

    data = localStorage.getItem(LOCAL_STORAGE_DEBUGGER_STATE_KEY);
    if (!Strings.isNullOrEmpty(data)) {
      suspendEventDto = dtoFactory.createDtoFromJson(data, SuspendEventDto.class);
    }

    data = localStorage.getItem(LOCAL_STORAGE_DEBUGGER_DISPOSABLE_BREAKPOINT_KEY);
    if (!Strings.isNullOrEmpty(data)) {
      disposableBreakpoint = dtoFactory.createDtoFromJson(data, BreakpointDto.class);
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

  protected LocationDto toDto(Location location) {
    MethodDto methodDto = dtoFactory.createDto(MethodDto.class);
    Method method = location.getMethod();
    if (method != null) {
      List<VariableDto> arguments =
          method.getArguments().stream().map(this::toDto).collect(Collectors.toList());
      methodDto.setArguments(arguments);
      methodDto.setName(method.getName());
    }
    return dtoFactory
        .createDto(LocationDto.class)
        .withTarget(location.getTarget())
        .withLineNumber(location.getLineNumber())
        .withExternalResource(location.isExternalResource())
        .withExternalResourceId(location.getExternalResourceId())
        .withResourceProjectPath(location.getResourceProjectPath())
        .withMethod(methodDto)
        .withThreadId(location.getThreadId());
  }

  protected BreakpointConfigurationDto toDto(BreakpointConfiguration breakpointConfiguration) {
    return dtoFactory
        .createDto(BreakpointConfigurationDto.class)
        .withSuspendPolicy(breakpointConfiguration.getSuspendPolicy())
        .withHitCount(breakpointConfiguration.getHitCount())
        .withCondition(breakpointConfiguration.getCondition())
        .withConditionEnabled(breakpointConfiguration.isConditionEnabled())
        .withHitCountEnabled(breakpointConfiguration.isHitCountEnabled());
  }

  protected BreakpointDto toDto(Breakpoint breakpoint) {
    return dtoFactory
        .createDto(BreakpointDto.class)
        .withLocation(toDto(breakpoint.getLocation()))
        .withEnabled(true)
        .withBreakpointConfiguration(
            breakpoint.getBreakpointConfiguration() == null
                ? null
                : toDto(breakpoint.getBreakpointConfiguration()));
  }

  protected abstract DebuggerDescriptor toDescriptor(Map<String, String> connectionProperties);
}
