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
package org.eclipse.che.api.factory.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.commons.lang.Pair;

import java.util.List;
import java.util.Set;

/** 
 * Interface for CRUD operations with factory data. 
 * 
 * @author Max Shaposhnik
 */

public interface FactoryStore {
    /**
     * Save factory at storage.
     *
     * @param factory
     *         factory information
     * @param images
     *         factory images
     * @return  id of stored factory
     * @throws java.lang.RuntimeException
     *          if {@code factory} is null
     * @throws org.eclipse.che.api.core.ConflictException
     *          if {@code factory} with given name and creator already exists
     * @throws org.eclipse.che.api.core.ServerException
     *          if other error occurs
     */
    public String saveFactory(Factory factory, Set<FactoryImage> images) throws ConflictException, ServerException;

    /**
     * Remove factory by id
     *
     * @param factoryId
     *         - id of factory to remove
     * @throws org.eclipse.che.api.core.NotFoundException
     *          if factory with given {@code factoryId} is not found
     * @throws java.lang.RuntimeException
     *          if {@code factoryId} is null
     * @throws org.eclipse.che.api.core.ServerException
     *          if other error occurs
     */
    public void removeFactory(String factoryId) throws NotFoundException, ServerException;

    /**
     * Retrieve factory data by its id
     *
     * @param factoryId
     *         - factory id
     * @return - {@code AdvancedFactoryUrl} if factory exist and found
     * @throws org.eclipse.che.api.core.NotFoundException
     *           if factory with given {@code factoryId} is not found
     * @throws java.lang.RuntimeException
     *          if {@code factoryId} is null
     * @throws org.eclipse.che.api.core.ServerException
     *          if other error occurs
     *
     */
    public Factory getFactory(String factoryId) throws NotFoundException, ServerException;

    /**
     * Retrieve factory by given list of pairs of attribute names and values.
     *
     * @param maxItems
     *         max number of items in response.
     * @param skipCount
     *         skip items. Must be equals or greater then {@code 0}. IllegalArgumentException thrown otherwise.
     * @param attributes
     *         attribute pairs to search for
     *
     * @return - List {@code AdvancedFactoryUrl} if factory(s) exist and found, empty list otherwise
     * @throws org.eclipse.che.api.core.IllegalArgumentException
     *          if {@code skipCount} is negative
     *         
     */
    public List<Factory> findByAttribute(int maxItems, int skipCount, List<Pair<String, String>> attributes) throws IllegalArgumentException;

    /**
     * Retrieve factory images by factory id
     *
     * @param factoryId
     *         factory id. Must not be null.
     * @param imageId
     *         id of the requested image. When null, all images for given factory will be returned.
     * @return {@code Set} of images if factory found, empty set otherwise
     * @throws java.lang.RuntimeException
     *          if {@code factoryId} is null
     * @throws org.eclipse.che.api.core.NotFoundException
     *          if factory with given {@code factoryId} is not found
     */
    public Set<FactoryImage> getFactoryImages(String factoryId, String imageId) throws NotFoundException;

    /**
     * Update factory at storage.
     *
     * @param factoryId
     *         factory id to update. Must not be null.
     * @param factory
     *         factory information. Must not be null.
     * @return id of stored factory
     * @throws org.eclipse.che.api.core.NotFoundException
     *         if factory with given {@code factoryId} is not found
     * @throws org.eclipse.che.api.core.ConflictException
     *          if {@code factory} with given name and creator already exists
     * @throws java.lang.RuntimeException
     *          if {@code factory} is null
     */
    public String updateFactory(String factoryId, Factory factory) throws NotFoundException, ConflictException;

}
