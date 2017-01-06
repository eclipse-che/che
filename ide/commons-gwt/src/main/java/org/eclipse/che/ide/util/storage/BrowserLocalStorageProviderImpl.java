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

import org.eclipse.che.commons.annotation.Nullable;

/**
 * Browser based {@link LocalStorageProvider}
 */
public class BrowserLocalStorageProviderImpl implements LocalStorageProvider {

    @Override
    @Nullable
    public LocalStorage get() {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        return localStorage == null ? null : new BrowserLocalStorageImpl(localStorage);
    }
}
