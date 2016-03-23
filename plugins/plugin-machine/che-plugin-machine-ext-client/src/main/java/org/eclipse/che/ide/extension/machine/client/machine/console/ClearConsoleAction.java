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
package org.eclipse.che.ide.extension.machine.client.machine.console;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;

/**
 * Action to clear Machine console.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ClearConsoleAction extends Action {

    private final MachineConsolePresenter presenter;
    private final AppContext              appContext;

    @Inject
    public ClearConsoleAction(MachineConsolePresenter presenter,
                              AppContext appContext,
                              MachineResources resources,
                              MachineLocalizationConstant localizationConstant) {
        super(localizationConstant.clearConsoleControlTitle(),
              localizationConstant.clearConsoleControlDescription(),
              null,
              resources.clear());
        this.presenter = presenter;
        this.appContext = appContext;
    }

    /** {@inheritDoc} */
    @Override
    public void update(ActionEvent e) {
        e.getPresentation().setEnabledAndVisible(appContext.getCurrentProject() != null);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {

        presenter.clear();
    }
}
