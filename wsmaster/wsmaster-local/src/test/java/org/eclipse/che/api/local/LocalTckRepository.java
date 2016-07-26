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
package org.eclipse.che.api.local;

import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * Simplifies implementation of TckRepository for local data access objects.
 *
 * @param <T>
 *         the type of the repository
 * @author Yevhenii Voevodin
 */
public class LocalTckRepository<T> implements TckRepository<T> {

    private final Map<String, T>      storage;
    private final Function<T, String> keyMapper;

    public LocalTckRepository(Map<String, T> storage, Function<T, String> keyMapper) {
        this.storage = storage;
        this.keyMapper = keyMapper;
    }

    @Override
    public void createAll(Collection<? extends T> entities) throws TckRepositoryException {
        for (T entity : entities) {
            storage.put(keyMapper.apply(entity), entity);
        }
    }

    @Override
    public void removeAll() throws TckRepositoryException {
        storage.clear();
    }
}
