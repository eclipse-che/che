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
package org.eclipse.che.plugin.gdb.ide;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.plugin.gdb.ide.GdbDebugger.ConnectionProperties.HOST;
import static org.eclipse.che.plugin.gdb.ide.GdbDebugger.ConnectionProperties.PORT;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Map;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.debug.DebuggerServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.debug.AbstractDebugger;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerLocationHandlerManager;

/**
 * The GDB debugger client.
 *
 * @author Anatoliy Bazko
 */
public class GdbDebugger extends AbstractDebugger {

  public static final String ID = "gdb";

  private GdbLocalizationConstant locale;

  @Inject
  public GdbDebugger(
      DebuggerServiceClient service,
      RequestTransmitter transmitter,
      RequestHandlerConfigurator configurator,
      GdbLocalizationConstant locale,
      DtoFactory dtoFactory,
      LocalStorageProvider localStorageProvider,
      EventBus eventBus,
      DebuggerManager debuggerManager,
      NotificationManager notificationManager,
      BreakpointManager breakpointManager,
      AppContext appContext,
      DebuggerLocalizationConstant constant,
      RequestHandlerManager requestHandlerManager,
      DebuggerLocationHandlerManager debuggerLocationHandlerManager,
      PromiseProvider promiseProvider) {
    super(
        service,
        transmitter,
        configurator,
        dtoFactory,
        localStorageProvider,
        eventBus,
        debuggerManager,
        notificationManager,
        appContext,
        breakpointManager,
        constant,
        requestHandlerManager,
        debuggerLocationHandlerManager,
        promiseProvider,
        ID);
    this.locale = locale;
  }

  @Override
  public Promise<Void> addBreakpoint(final Breakpoint breakpoint) {
    if (isConnected() && !isSuspended()) {
      notificationManager.notify(locale.messageSuspendToActivateBreakpoints(), FAIL, FLOAT_MODE);
    }

    return super.addBreakpoint(breakpoint);
  }

  @Override
  protected DebuggerDescriptor toDescriptor(Map<String, String> connectionProperties) {
    String host = connectionProperties.get(HOST.toString());
    String port = connectionProperties.get(PORT.toString());
    String address = host + (port.isEmpty() || port.equals("0") ? "" : (":" + port));
    return new DebuggerDescriptor("", address);
  }

  public enum ConnectionProperties {
    HOST,
    PORT,
    BINARY,
    SOURCES
  }
}
