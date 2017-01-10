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

import java.util.HashMap;
import java.util.Map;

/**
 * Dummy implementation of the {@link LocalStorage}.
 * 
 * @author Anatoliy Bazko
 */
public class DummyLocalStorageImpl implements LocalStorage {

    private Map<String, String> m;

    public DummyLocalStorageImpl() {
        m = new HashMap<String, String>();
    }

    @Override
    public String getItem(String key) {
        return m.get(key);
    }

    @Override
    public void removeItem(String key) {
        m.remove(key);
    }

    @Override
    public void setItem(String key, String value) {
        m.put(key, value);
    }
}
