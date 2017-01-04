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

import com.google.gwt.storage.client.Storage;

import javax.annotation.Nonnull;

/**
 * Browser based {@link LocalStorage}.
 *
 * @author Anatoliy Bazko
 */
public class BrowserLocalStorageImpl implements LocalStorage {

    private final Storage storage;

    public BrowserLocalStorageImpl(@Nonnull Storage storage) {
        this.storage = storage;
    }

    @Override
    public String getItem(String key) {
        return storage.getItem(key);
    }

    @Override
    public void removeItem(String key) {
        storage.removeItem(key);
    }

    @Override
    public void setItem(String key, String value) {
        storage.setItem(key, value);
    }
}
