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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.processes;

import com.google.inject.ImplementedBy;

import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.ide.api.mvp.View;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * The interface defines methods to control displaying of processes.
 *
 * @author Dmitry Shnurenko
 */
@ImplementedBy(ProcessesViewImpl.class)
public interface ProcessesView extends View<ProcessesView.ActionDelegate> {

    /**
     * Sets processes in special table to display them on view.
     *
     * @param descriptors
     *         descriptors which need display
     */
    void setProcesses(@NotNull List<MachineProcessDto> descriptors);

    /**
     * Change visibility state of panel.
     *
     * @param visible
     *         <code>true</code> panel is visible,<code>false</code> panel is not visible
     */
    void setVisible(boolean visible);

    interface ActionDelegate {
        /**
         * Performs some actions when user selects process in table.
         *
         * @param descriptor
         *         process which is selected
         */
        void onProcessClicked(@NotNull MachineProcessDto descriptor);
    }
}
