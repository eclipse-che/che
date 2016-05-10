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
package org.eclipse.che.plugin.gdb.ide;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.debugger.gwt.client.DebuggerServiceClient;
import org.eclipse.che.api.debugger.shared.model.Location;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.plugin.debugger.ide.debug.AbstractDebugger;
import org.eclipse.che.plugin.debugger.ide.fqn.FqnResolverFactory;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.plugin.gdb.ide.GdbDebugger.ConnectionProperties.HOST;
import static org.eclipse.che.plugin.gdb.ide.GdbDebugger.ConnectionProperties.PORT;

/**
 * The GDB debugger client.
 *
 * @author Anatoliy Bazko
 */
public class GdbDebugger extends AbstractDebugger {

    public static final String ID = "gdb";

    private final AppContext appContext;

    @Inject
    public GdbDebugger(DebuggerServiceClient service,
                       DtoFactory dtoFactory,
                       LocalStorageProvider localStorageProvider,
                       MessageBusProvider messageBusProvider,
                       EventBus eventBus,
                       FqnResolverFactory fqnResolverFactory,
                       GdbDebuggerFileHandler activeFileHandler,
                       DebuggerManager debuggerManager,
                       FileTypeRegistry fileTypeRegistry,
                       AppContext appContext) {

        super(service,
              dtoFactory,
              localStorageProvider,
              messageBusProvider,
              eventBus,
              fqnResolverFactory,
              activeFileHandler,
              debuggerManager,
              fileTypeRegistry,
              ID);
        this.appContext = appContext;
    }

    @Override
    protected List<String> fqnToPath(@NotNull Location location) {
        CurrentProject currentProject = appContext.getCurrentProject();
        if (currentProject == null) {
            return Collections.singletonList(location.getTarget());
        }

        String projectPath = currentProject.getProjectConfig().getPath();
        String path = projectPath + "/" + location.getTarget();
        return Collections.singletonList(path);
    }

    @Override
    protected String pathToFqn(VirtualFile file) {
        return file.getName();
    }

    @Override
    protected DebuggerDescriptor toDescriptor(Map<String, String> connectionProperties) {
        String address = connectionProperties.get(HOST.toString()) + ":" +
                         connectionProperties.get(PORT.toString());
        return new DebuggerDescriptor("", address);
    }

    public enum ConnectionProperties {
        HOST,
        PORT,
        BINARY,
        SOURCES
    }
}
