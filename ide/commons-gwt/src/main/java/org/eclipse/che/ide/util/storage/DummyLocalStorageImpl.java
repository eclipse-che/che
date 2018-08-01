/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.util.storage;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Dummy implementation of the {@link LocalStorage}.
 *
 * @author Anatoliy Bazko
 */
public class DummyLocalStorageImpl implements LocalStorage {

  private Map<String, String> m;

  public DummyLocalStorageImpl() {
    m = new LinkedHashMap<>();
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

  @Override
  public String key(int index) {
    String key = null;

    Iterator<String> iter = m.keySet().iterator();
    for (int i = 0; i <= index; i++) {
      if (!iter.hasNext()) {
        return null;
      }
      key = iter.next();
    }

    return key;
  }

  @Override
  public int getLength() {
    return m.size();
  }
}
