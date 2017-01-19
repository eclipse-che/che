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
package org.eclipse.che.api.environment.server;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.environment.server.exception.EnvironmentException;
import org.eclipse.che.api.machine.server.spi.Instance;

/**
 * Used in couple with {@link CheEnvironmentEngine#start} method to
 * allow sequential handling and interruption of the start process.
 *
 * <p>This interface is a part of a contract for {@link CheEnvironmentEngine}.
 *
 * @author Yevhenii Voevodin
 */
public interface MachineStartedHandler {
    void started(Instance machine) throws EnvironmentException, ServerException;
}
