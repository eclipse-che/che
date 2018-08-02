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
package org.eclipse.che.api.workspace.server;

import com.google.inject.Singleton;
import org.eclipse.che.commons.lang.concurrent.StripedLocks;
import org.eclipse.che.commons.lang.concurrent.Unlocker;

/**
 * Default implementation of {@link WorkspaceLockService} that uses {@link StripedLocks}.
 *
 * @author Anton Korneta
 */
@Singleton
public class DefaultWorkspaceLockService implements WorkspaceLockService {
  private final StripedLocks delegate;

  public DefaultWorkspaceLockService() {
    this.delegate = new StripedLocks(16);
  }

  @Override
  public Unlocker readLock(String key) {
    return delegate.readLock(key);
  }

  @Override
  public Unlocker writeLock(String key) {
    return delegate.writeLock(key);
  }
}
