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
package org.eclipse.che.datasource.ide;

import com.google.inject.Inject;

import org.eclipse.che.datasource.ide.newDatasource.NewDatasourceWizardAction;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_HELP;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_MENU;
import static org.eclipse.che.ide.api.constraints.Anchor.AFTER;

@Extension(title = "Datasource Extension", version = "0.0.1")
public class DatasourceExtension {

    /**
     * Constructor.
     *
     * @param actionManager
     *         the {@link ActionManager} that is used to register our actions
     * @param newDatasourceWizardAction
     *         new datasource wizard action
     * @param connectorsInitializer
     *         initializes the available connectors
     * @param availableJdbcDrivers
     *         available jdbc drivers in the agent
     * @param newDatasourceWizardAction
     *         new datasource wizard action
     */
    @Inject
    public DatasourceExtension(
            ActionManager actionManager,
            ConnectorsInitializer connectorsInitializer,
            AvailableJdbcDriversService availableJdbcDrivers,
            NewDatasourceWizardAction newDatasourceWizardAction) {

        actionManager.registerAction("New Datasource", newDatasourceWizardAction);
        DefaultActionGroup datasourceGroup = new DefaultActionGroup("Datasource", true, actionManager);

        datasourceGroup.add(newDatasourceWizardAction);

        DefaultActionGroup mainMenu = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_MENU);
        mainMenu.add(datasourceGroup, new Constraints(AFTER, GROUP_HELP));


        // add Datasource to context menu as last entry
        DefaultActionGroup mainContextMenuGroup = (DefaultActionGroup)actionManager.getAction("resourceOperation");
        mainContextMenuGroup.add(newDatasourceWizardAction, Constraints.LAST);

        // do after adding new datasource page provider to keep page order
        connectorsInitializer.initConnectors();

        // fetching available drivers list from the server
        availableJdbcDrivers.fetch();

    }
}
