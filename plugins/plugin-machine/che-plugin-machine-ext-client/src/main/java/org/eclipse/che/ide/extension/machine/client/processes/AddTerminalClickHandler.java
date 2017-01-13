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
package org.eclipse.che.ide.extension.machine.client.processes;

import javax.validation.constraints.NotNull;

/**
 * Handler for the processing of click on 'Add new terminal' button
 *
 * @author Anna Shumilova
 */
public interface AddTerminalClickHandler {

    /**
     * Will be called when user clicks 'Add new terminal' button
     *
     * @param machineId
     *         id of machine in which the terminal will be added
     */
    void onAddTerminalClick(@NotNull String machineId);
}
