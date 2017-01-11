/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.zdb.ide;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.debug.DebuggerServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.plugin.debugger.ide.debug.AbstractDebugger;
import org.eclipse.che.plugin.debugger.ide.debug.BasicActiveFileHandler;
import org.eclipse.che.plugin.zdb.ide.configuration.ZendDbgConfigurationType;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Zend PHP debugger.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDebugger extends AbstractDebugger {

    public static final String ID = "zend-debugger";

    @Inject
    public ZendDebugger(DebuggerServiceClient service,
                        DtoFactory dtoFactory,
                        LocalStorageProvider localStorageProvider,
                        MessageBusProvider messageBusProvider,
                        EventBus eventBus,
                        BasicActiveFileHandler activeFileHandler,
                        NotificationManager notificationManager,
                        DebuggerManager debuggerManager,
                        BreakpointManager breakpointManager) {
        super(service,
              dtoFactory,
              localStorageProvider,
              messageBusProvider,
              eventBus,
              activeFileHandler,
              debuggerManager,
              notificationManager,
              breakpointManager,
              ID);
    }

    @Override
    protected String fqnToPath(@NotNull Location location) {
        String resourcePath = location.getResourcePath();
        return resourcePath != null ? resourcePath : location.getTarget();
    }

    @Override
    protected String pathToFqn(VirtualFile file) {
        return file.getName();
    }

    @Override
    protected DebuggerDescriptor toDescriptor(Map<String, String> connectionProperties) {
        return new DebuggerDescriptor("Zend Debugger",
                "Zend Debugger, port: " + connectionProperties.get(ZendDbgConfigurationType.ATTR_DEBUG_PORT));
    }

}
