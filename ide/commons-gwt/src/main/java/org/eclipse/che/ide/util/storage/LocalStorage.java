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
package org.eclipse.che.ide.util.storage;

import org.eclipse.che.commons.annotation.Nullable;

import javax.annotation.Nonnull;


/**
 * Stores client-side data into a storage.
 * 
 * @author Anatoliy Bazko
 */
public interface LocalStorage {

    /**
     * Gets value from the storage. Method returns null if value doesn't exist.
     */
    @Nullable
    String getItem(@Nonnull String key);
    
    
    /**
     * Removes value from the storage.
     */
    void removeItem(@Nonnull String key);

    /**
     * Puts value into the storage.
     */
    void setItem(@Nonnull String key, @Nonnull String value);
}
