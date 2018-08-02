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
package org.eclipse.che.plugin.nodejsdbg.ide;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Map;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
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
 * The NodeJs Debugger Client.
 *
 * @author Anatoliy Bazko
 */
public class NodeJsDebugger extends AbstractDebugger {

  public static final String ID = "nodejsdbg";

  @Inject
  public NodeJsDebugger(
      DebuggerServiceClient service,
      RequestTransmitter transmitter,
      RequestHandlerConfigurator configurator,
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
  }

  @Override
  protected DebuggerDescriptor toDescriptor(Map<String, String> connectionProperties) {
    StringBuilder sb = new StringBuilder();

    for (String propName : connectionProperties.keySet()) {
      try {
        ConnectionProperties prop = ConnectionProperties.valueOf(propName.toUpperCase());
        String connectionInfo = prop.getConnectionInfo(connectionProperties.get(propName));
        if (!connectionInfo.isEmpty()) {
          if (sb.length() > 0) {
            sb.append(',');
          }
          sb.append(connectionInfo);
        }
      } catch (IllegalArgumentException ignored) {
        // unrecognized connection property
      }
    }

    return new DebuggerDescriptor("", "{ " + sb.toString() + " }");
  }

  public enum ConnectionProperties {
    SCRIPT {
      @Override
      public String getConnectionInfo(String value) {
        return value;
      }
    };

    public abstract String getConnectionInfo(String value);
  }
}
