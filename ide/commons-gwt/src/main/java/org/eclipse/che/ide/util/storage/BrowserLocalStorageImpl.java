/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
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

  @Override
  public String key(int index) {
    return storage.key(index);
  }

  @Override
  public int getLength() {
    return storage.getLength();
  }
}
