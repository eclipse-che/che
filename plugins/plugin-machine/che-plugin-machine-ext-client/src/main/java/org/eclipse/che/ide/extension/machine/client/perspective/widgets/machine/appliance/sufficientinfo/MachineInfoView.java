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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.sufficientinfo;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.machine.MachineEntity;

import javax.validation.constraints.NotNull;

/**
 * Defines methods which allows update displaying information about current machine.
 *
 * @author Dmitry Shnurenko
 */
@ImplementedBy(MachineInfoViewImpl.class)
public interface MachineInfoView extends IsWidget {

    /**
     * Updates view representation which contains information about current machine.
     *
     * @param machine
     *         machine for which need update view
     */
    void updateInfo(@NotNull MachineEntity machine);

    /**
     * Sets owner's name in special place on view.
     *
     * @param ownerName
     *         name which need set
     */
    void setOwner(@NotNull String ownerName);

    /**
     * Sets workspace in special place on view.
     *
     * @param workspaceName
     *         name which need set
     */
    void setWorkspaceName(@NotNull String workspaceName);

    /**
     * Sets view visibility.
     *
     * @param visible
     *         <code>true</code> panel is visible,<code>false</code> panel isn't visible
     */
    void setVisible(boolean visible);
}
