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
package org.eclipse.che.api.environment.server.exception;

/**
 * Thrown when environment start is interrupted.
 *
 * @author Yevhenii Voevodin
 */
public class EnvironmentStartInterruptedException extends EnvironmentException {
    public EnvironmentStartInterruptedException(String workspaceId, String envName) {
        super(String.format("Start of environment '%s' in workspace '%s' is interrupted",
                            envName,
                            workspaceId));
    }
}
