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
package org.eclipse.che.api.user.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.model.user.User;

/**
 * Validates token.
 *
 * @author Eugene Voevodin
 * @see UserService
 */
public interface TokenValidator {

    /**
     * Validates {@code token}.
     *
     * @return user email
     * @throws ConflictException
     *         when token is not valid
     */
    User validateToken(String token) throws ConflictException;
}
