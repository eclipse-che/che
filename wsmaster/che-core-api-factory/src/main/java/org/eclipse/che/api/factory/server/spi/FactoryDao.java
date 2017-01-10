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
package org.eclipse.che.api.factory.server.spi;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.factory.server.model.impl.FactoryImpl;
import org.eclipse.che.commons.lang.Pair;

import java.util.List;

/**
 * Defines data access object contract for {@code FactoryImpl}.
 *
 * @author Max Shaposhnik
 * @author Anton Korneta
 */
public interface FactoryDao {

    /**
     * Creates factory.
     *
     * @param factory
     *         factory to create
     * @return created factory
     * @throws NullPointerException
     *         when {@code factory} is null
     * @throws ConflictException
     *         when {@code factory} with given name and creator already exists
     * @throws ServerException
     *         when any other error occurs
     */
    FactoryImpl create(FactoryImpl factory) throws ConflictException, ServerException;

    /**
     * Updates factory to the new entity, using replacement strategy.
     *
     * @param factory
     *         factory to update
     * @return updated factory
     * @throws NullPointerException
     *         when {@code factory} is null
     * @throws NotFoundException
     *         when given factory is not found
     * @throws ConflictException
     *         when {@code factory} with given name is already exist for creator
     * @throws ServerException
     *         when any other error occurs
     */
    FactoryImpl update(FactoryImpl factory) throws NotFoundException, ConflictException, ServerException;

    /**
     * Removes factory.
     *
     * @param id
     *         factory identifier
     * @throws NullPointerException
     *         when {@code id} is null
     * @throws ServerException
     *         when any other error occurs
     */
    void remove(String id) throws ServerException;

    /**
     * Gets factory by identifier.
     *
     * @param id
     *         factory identifier
     * @return factory instance, never null
     * @throws NullPointerException
     *         when {@code id} is null
     * @throws NotFoundException
     *         when factory with given {@code id} is not found
     * @throws ServerException
     *         when any other error occurs
     */
    FactoryImpl getById(String id) throws NotFoundException, ServerException;

    /**
     * Gets the factories for the list of attributes.
     *
     * @param maxItems
     *         the maximum count of items to fetch
     * @param skipCount
     *         count of items which should be skipped
     * @param attributes
     *         list of pairs of attributes to search for
     * @return list of the factories which contain the specified attributes
     * @throws IllegalArgumentException
     *         when {@code skipCount} or {@code maxItems} is negative
     * @throws ServerException
     *         when any other error occurs
     */
    List<FactoryImpl> getByAttribute(int maxItems,
                                     int skipCount,
                                     List<Pair<String, String>> attributes) throws ServerException;
}
