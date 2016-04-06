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
package org.eclipse.che.ide.gdb.client;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.debugger.client.debug.AbstractDebugger;
import org.eclipse.che.ide.ext.debugger.client.debug.ActiveFileHandler;
import org.eclipse.che.ide.ext.debugger.client.debug.DebuggerServiceClient;
import org.eclipse.che.ide.ext.debugger.client.fqn.FqnResolverFactory;
import org.eclipse.che.ide.ext.debugger.shared.Location;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;
import org.eclipse.che.ide.websocket.MessageBusProvider;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.gdb.client.GdbDebuggerClient.ConnectionProperties.HOST;
import static org.eclipse.che.ide.gdb.client.GdbDebuggerClient.ConnectionProperties.PORT;

/**
 * The GDB debugger client.
 *
 * @author Anatoliy Bazko
 */
public class GdbDebuggerClient extends AbstractDebugger {

    public static final String ID             = "gdb";
    public static final String EVENTS_CHANNEL = "gdbdebugger:events:";

    @Inject
    public GdbDebuggerClient(DebuggerServiceClient service,
                             DtoFactory dtoFactory,
                             LocalStorageProvider localStorageProvider,
                             MessageBusProvider messageBusProvider,
                             EventBus eventBus,
                             FqnResolverFactory fqnResolverFactory,
                             ActiveFileHandler activeFileHandler,
                             DebuggerManager debuggerManager,
                             FileTypeRegistry fileTypeRegistry) {

        super(service,
              dtoFactory,
              localStorageProvider,
              messageBusProvider,
              eventBus,
              fqnResolverFactory,
              activeFileHandler,
              debuggerManager,
              fileTypeRegistry,
              ID,
              EVENTS_CHANNEL);
    }

    @Override
    protected List<String> resolveFilePathByLocation(@NotNull Location location) {
        Collections.singleton(location.getClassName());
        return Collections.singletonList(location.getClassName());
    }

    @Override
    protected DebuggerDescriptor toDescriptor(Map<String, String> connectionProperties) {
        String address = connectionProperties.get(connectionProperties.get(HOST.toString()) + ":" +
                                                  connectionProperties.get(PORT.toString()));
        return new DebuggerDescriptor("", address);
    }

    public enum ConnectionProperties {
        HOST,
        PORT,
        FILE
    }
}
