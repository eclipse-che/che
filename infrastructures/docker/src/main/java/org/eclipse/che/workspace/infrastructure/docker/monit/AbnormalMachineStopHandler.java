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
package org.eclipse.che.workspace.infrastructure.docker.monit;

/**
 * Handles unexpected stop of a Docker machine.
 *
 * @author Alexander Garagatyi
 */
public interface AbnormalMachineStopHandler {
    /**
     * Accepts machine stop error message and handles unexpected stop of a machine.
     *
     * @param error
     */
    void handle(String error);
}
