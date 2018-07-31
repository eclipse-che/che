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
import javax.inject.Singleton;
import org.eclipse.che.commons.annotation.Nullable;

/** Browser based {@link LocalStorageProvider} */
@Singleton
public class BrowserLocalStorageProviderImpl implements LocalStorageProvider {

  @Override
  @Nullable
  public LocalStorage get() {
    Storage localStorage = Storage.getLocalStorageIfSupported();
    return localStorage == null ? null : new BrowserLocalStorageImpl(localStorage);
  }
}
