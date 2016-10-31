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
package org.eclipse.che.api.local.storage;


import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Map;

/**
 * Factory for injection to LocalStorage stored file.
 *
 * @author Anton Korneta
 */
@Singleton
public class LocalStorageFactory {

    /** Path to storage root folder. */
    private final String pathToStorage;

    @Inject
    public LocalStorageFactory(@Named("che.database") String pathToStorage) {
        this.pathToStorage = pathToStorage;
    }

    /**
     * @param fileName
     *         name of file in local storage.
     * @return instance of LocalStorage.
     * @throws IOException
     *         occurs when cannot create root storage directory.
     */
    public LocalStorage create(String fileName) throws IOException {
        return new LocalStorage(pathToStorage, fileName);
    }

    /**
     * @param fileName
     *         name of file in local storage.
     * @param typeAdapters
     *         types and object adapters when need a special deserialization.
     * @return instance of LocalStorage.
     * @throws IOException
     *         occurs when cannot create root storage directory.
     */
    public LocalStorage create(String fileName, Map<Class<?>, Object> typeAdapters) throws IOException {
        return new LocalStorage(pathToStorage, fileName, typeAdapters);
    }
}