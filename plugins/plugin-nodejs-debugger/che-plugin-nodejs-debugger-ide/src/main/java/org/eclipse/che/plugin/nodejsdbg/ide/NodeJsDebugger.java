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
package org.eclipse.che.plugin.nodejsdbg.ide;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.debug.DebuggerServiceClient;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.plugin.debugger.ide.debug.AbstractDebugger;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * The NodeJs Debugger Client.
 *
 * @author Anatoliy Bazko
 */
public class NodeJsDebugger extends AbstractDebugger {

    public static final String ID = "nodejsdbg";

    private final AppContext appContext;

    @Inject
    public NodeJsDebugger(DebuggerServiceClient service,
                          DtoFactory dtoFactory,
                          LocalStorageProvider localStorageProvider,
                          MessageBusProvider messageBusProvider,
                          EventBus eventBus,
                          NodeJsDebuggerFileHandler activeFileHandler,
                          DebuggerManager debuggerManager,
                          BreakpointManager breakpointManager,
                          AppContext appContext) {

        super(service,
              dtoFactory,
              localStorageProvider,
              messageBusProvider,
              eventBus,
              activeFileHandler,
              debuggerManager,
              breakpointManager,
              ID);
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

    @Override
    protected String pathToFqn(VirtualFile file) {
        return file.getName();
    }

    @Override
    protected DebuggerDescriptor toDescriptor(Map<String, String> connectionProperties) {
        StringBuilder sb = new StringBuilder();
        for (ConnectionProperties prop : ConnectionProperties.values()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(prop.process(connectionProperties));
        }

        return new DebuggerDescriptor("", sb.toString());
    }

    public enum ConnectionProperties {
        URI,
        PID,
        SCRIPT;

        public String process(Map<String, String> properties) {
            for (String prop : properties.keySet()) {
                if (this.toString().equalsIgnoreCase(prop)) {
                    return this.toString().toLowerCase() + " : " + properties.get(prop);
                }
            }

            return "";
        }
    }
}
