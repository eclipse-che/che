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
package org.eclipse.che.ide.extension.machine.client.inject.factories;

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.extension.machine.client.perspective.terminal.TerminalPresenter;

import javax.validation.constraints.NotNull;

/**
 * Special factory for creating {@link TerminalPresenter} instances.
 *
 * @author Dmitry Shnurenko
 */
public interface TerminalFactory {

    /**
     * Creates terminal for current machine.
     *
     * @param machine
     *         machine for which terminal will be created
     * @return an instance of {@link TerminalPresenter}
     */
    TerminalPresenter create(@NotNull @Assisted MachineEntity machine, Object source);
}
