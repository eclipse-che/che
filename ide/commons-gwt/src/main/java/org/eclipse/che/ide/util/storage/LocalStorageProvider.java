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

import com.google.inject.ImplementedBy;
import org.eclipse.che.commons.annotation.Nullable;

/** @author Anatoliy Bazko */
@ImplementedBy(BrowserLocalStorageProviderImpl.class)
public interface LocalStorageProvider {

  /** Returns {@link LocalStorage} if it is supported or null otherwise. */
  @Nullable
  LocalStorage get();
}
