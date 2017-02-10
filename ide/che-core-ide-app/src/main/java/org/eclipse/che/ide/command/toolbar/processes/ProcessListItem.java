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
package org.eclipse.che.ide.command.toolbar.processes;

import org.eclipse.che.ide.ui.dropdown.DropDownListItem;

/**
 * {@link DropDownListItem} that represents a {@link Process}.
 */
class ProcessListItem implements DropDownListItem {

    private final Process process;

    /**
     * Creates new item to represent the given {@code process}.
     *
     * @param process
     *         process to represent
     */
    ProcessListItem(Process process) {
        this.process = process;
    }

    /** Returns represented {@link Process}. */
    Process getProcess() {
        return process;
    }
}
