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
package org.eclipse.che.ide.ext.debugger.client.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.ext.debugger.client.DebuggerLocalizationConstant;
import org.eclipse.che.ide.ext.debugger.client.DebuggerResources;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action that allows to connect to the debugger with the selected debug configuration.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class DebugAction extends AbstractPerspectiveAction {

    private final SelectDebugConfigurationComboBoxAction selectConfigurationAction;
    private final DebuggerLocalizationConstant           localizationConstants;
    private final DebuggerManager                        debuggerManager;
    private final DialogFactory                          dialogFactory;

    @Inject
    public DebugAction(SelectDebugConfigurationComboBoxAction selectConfigurationAction,
                       DebuggerLocalizationConstant localizationConstants,
                       DebuggerResources resources,
                       DebuggerManager debuggerManager,
                       DialogFactory dialogFactory) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID),
              localizationConstants.debugActionTitle(),
              localizationConstants.debugActionDescription(),
              null,
              resources.debug());
        this.selectConfigurationAction = selectConfigurationAction;
        this.localizationConstants = localizationConstants;
        this.debuggerManager = debuggerManager;
        this.dialogFactory = dialogFactory;
    }

    @Override
    public void updateInPerspective(ActionEvent event) {
        event.getPresentation().setVisible(selectConfigurationAction.getSelectedConfiguration() != null);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        final DebugConfiguration debugConfiguration = selectConfigurationAction.getSelectedConfiguration();
        if (debugConfiguration != null) {
            connect(debugConfiguration);
        }
    }

    private void connect(DebugConfiguration debugConfiguration) {
        if (debuggerManager.getActiveDebugger() != null) {
            dialogFactory.createMessageDialog(localizationConstants.connectToRemote(),
                                              localizationConstants.debuggerAlreadyConnected(),
                                              null).show();
            return;
        }

        final Debugger debugger = debuggerManager.getDebugger(debugConfiguration.getType().getId());
        if (debugger != null) {
            debuggerManager.setActiveDebugger(debugger);

            Map<String, String> connectionProperties = new HashMap<>(2 + debugConfiguration.getConnectionProperties().size());
            connectionProperties.put("HOST", debugConfiguration.getHost());
            connectionProperties.put("PORT", String.valueOf(debugConfiguration.getPort()));
            connectionProperties.putAll(debugConfiguration.getConnectionProperties());

            debugger.attachDebugger(connectionProperties).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                    debuggerManager.setActiveDebugger(null);
                }
            });
        }
    }
}
