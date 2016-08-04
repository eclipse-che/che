/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.account.spi;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

/**
 * Defines data access object for {@link AccountImpl}
 *
 * @author Sergii Leschenko
 */
public interface AccountDao {
    /**
     * Gets account by identifier.
     *
     * @param id
     *         account identifier
     * @return account instance with given id
     * @throws NullPointerException
     *         when {@code id} is null
     * @throws NotFoundException
     *         when account with given {@code id} was not found
     * @throws ServerException
     *         when any other error occurs during account fetching
     */
    AccountImpl getById(String id) throws NotFoundException, ServerException;

    /**
     * Gets account by name.
     *
     * @param name
     *         account name
     * @return account instance with given name
     * @throws NullPointerException
     *         when {@code name} is null
     * @throws NotFoundException
     *         when account with given {@code name} was not found
     * @throws ServerException
     *         when any other error occurs during account fetching
     */
    AccountImpl getByName(String name) throws ServerException, NotFoundException;
}
