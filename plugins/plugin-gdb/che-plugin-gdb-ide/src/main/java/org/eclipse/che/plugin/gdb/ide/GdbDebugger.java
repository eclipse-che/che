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
package org.eclipse.che.plugin.gdb.ide;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.debug.DebuggerServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.plugin.debugger.ide.debug.AbstractDebugger;
import org.eclipse.che.plugin.debugger.ide.debug.BasicActiveFileHandler;

import javax.validation.constraints.NotNull;
import java.util.Map;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.plugin.gdb.ide.GdbDebugger.ConnectionProperties.HOST;
import static org.eclipse.che.plugin.gdb.ide.GdbDebugger.ConnectionProperties.PORT;

/**
 * The GDB debugger client.
 *
 * @author Anatoliy Bazko
 */
public class GdbDebugger extends AbstractDebugger {

    public static final String ID = "gdb";

    private GdbLocalizationConstant locale;
    private final AppContext appContext;

    @Inject
    public GdbDebugger(DebuggerServiceClient service,
                       GdbLocalizationConstant locale,
                       DtoFactory dtoFactory,
                       LocalStorageProvider localStorageProvider,
                       MessageBusProvider messageBusProvider,
                       EventBus eventBus,
                       BasicActiveFileHandler activeFileHandler,
                       DebuggerManager debuggerManager,
                       NotificationManager notificationManager,
                       BreakpointManager breakpointManager,
                       AppContext appContext) {

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
        this.locale = locale;
        this.appContext = appContext;
    }

    @Override
    protected String fqnToPath(@NotNull Location location) {
        final Resource resource = appContext.getResource();

        if (resource == null) {
            return location.getTarget();
        }

        final Optional<Project> project = resource.getRelatedProject();

        if (project.isPresent()) {
            return project.get().getLocation().append(location.getTarget()).toString();
        }

        return location.getTarget();
    }

    @Nullable
    @Override
    protected String pathToFqn(VirtualFile file) {
        return file.getName();
    }

    @Override
    public void addBreakpoint(final VirtualFile file, final int lineNumber) {
        if (isConnected() && !isSuspended()) {
            notificationManager.notify(locale.messageSuspendToActivateBreakpoints(), FAIL, FLOAT_MODE);
        }

        super.addBreakpoint(file, lineNumber);
    }

    @Override
    protected DebuggerDescriptor toDescriptor(Map<String, String> connectionProperties) {
        String host = connectionProperties.get(HOST.toString());
        String port = connectionProperties.get(PORT.toString());
        String address = host + (port.isEmpty() || port.equals("0") ? ""
                                                                    : (":" + port));
        return new DebuggerDescriptor("", address);
    }

    public enum ConnectionProperties {
        HOST,
        PORT,
        BINARY,
        SOURCES
    }
}
