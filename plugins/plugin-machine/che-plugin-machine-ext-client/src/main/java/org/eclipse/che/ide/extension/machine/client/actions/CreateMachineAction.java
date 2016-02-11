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
package org.eclipse.che.ide.extension.machine.client.actions;

import com.google.inject.Inject;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.machine.create.CreateMachinePresenter;

import javax.validation.constraints.NotNull;
import java.util.Collections;

import static org.eclipse.che.ide.extension.machine.client.perspective.MachinePerspective.MACHINE_PERSPECTIVE_ID;

/**
 * The action contains business logic which calls special method to create machine.
 *
 * @author Dmitry Shnurenko
 */
public class CreateMachineAction extends AbstractPerspectiveAction {

    private final CreateMachinePresenter createMachinePresenter;
    private final AnalyticsEventLogger   eventLogger;

    @Inject
    public CreateMachineAction(MachineLocalizationConstant locale,
                               CreateMachinePresenter createMachinePresenter,
                               AnalyticsEventLogger eventLogger) {
        super(Collections.singletonList(MACHINE_PERSPECTIVE_ID),
              locale.machineCreateTitle(),
              locale.machineCreateDescription(),
              null, null);

        this.createMachinePresenter = createMachinePresenter;
        this.eventLogger = eventLogger;
    }

    /** {@inheritDoc} */
    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        //to do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(@NotNull ActionEvent event) {
        eventLogger.log(this);
        createMachinePresenter.showDialog();
    }
}
