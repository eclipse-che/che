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

import java.util.Map;
import java.util.function.Function;

/**
 * Simplifies implementation of TckRepository for local data access objects which use map for backend.
 *
 * @param <T>
 *         the type of the repository
 * @author Yevhenii Voevodin
 */
public class LocalMapTckRepository<T> extends LocalTckRepository<Map<String, T>, T> {

    public LocalMapTckRepository(Map<String, T> storage, Function<T, String> keyMapper, @Nullable Object mutex) {
        super(storage, (s, entity) -> storage.put(keyMapper.apply(entity), entity), Map::clear, mutex);
    }
}
