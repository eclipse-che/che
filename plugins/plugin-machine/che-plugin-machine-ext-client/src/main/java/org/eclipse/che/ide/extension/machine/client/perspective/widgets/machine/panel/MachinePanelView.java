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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.panel;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

/**
 * Provides methods to control view representation of machine panel.
 *
 * @author Dmitry Shnurenko
 */
@ImplementedBy(MachinePanelViewImpl.class)
public interface MachinePanelView extends View<MachinePanelView.ActionDelegate> {

    /**
     * Sets data to display it in special place on view.
     *
     * @param root
     *         data which will be displayed
     */
    void setData(MachineTreeNode root);

    /**
     * Calls special method which adds special styles to selected element.
     *
     * @param machineNode
     *         node which will be selected
     */
    void selectNode(MachineTreeNode machineNode);

    void setVisible(boolean visible);

    interface ActionDelegate extends BaseActionDelegate {
        /**
         * Performs some actions when user selects current machine
         *
         * @param selectedMachine
         *         machine which was selected
         */
        void onMachineSelected(MachineEntity selectedMachine);
    }
}
