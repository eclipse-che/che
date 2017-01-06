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
package org.eclipse.che.api.local;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * Simplifies implementation of TckRepository for local data access objects.
 *
 * @param <STORAGE_T>
 *         the type of the storage
 * @param <ENTITY_T>
 *         the type of the entity
 * @author Yevhenii Voevodin
 */
public class LocalTckRepository<STORAGE_T, ENTITY_T> implements TckRepository<ENTITY_T> {

    private final STORAGE_T                       storage;
    private final BiConsumer<STORAGE_T, ENTITY_T> adder;
    private final Consumer<STORAGE_T>             cleaner;
    private final Object                          mutex;

    public LocalTckRepository(STORAGE_T storage,
                              BiConsumer<STORAGE_T, ENTITY_T> adder,
                              Consumer<STORAGE_T> cleaner,
                              @Nullable Object mutex) {
        this.storage = requireNonNull(storage, "Required non-null storage");
        this.adder = requireNonNull(adder, "Required non-null adder");
        this.cleaner = requireNonNull(cleaner, "Required non-null cleaner");
        this.mutex = mutex;
    }

    @Override
    public void createAll(Collection<? extends ENTITY_T> entities) throws TckRepositoryException {
        withMutex(() -> {
            for (ENTITY_T entity : entities) {
                adder.accept(storage, entity);
            }
        });
    }

    @Override
    public void removeAll() throws TckRepositoryException {
        withMutex(() -> cleaner.accept(storage));
    }

    private void withMutex(Runnable action) {
        if (mutex == null) {
            action.run();
        } else {
            synchronized (mutex) {
                action.run();
            }
        }
    }
}
