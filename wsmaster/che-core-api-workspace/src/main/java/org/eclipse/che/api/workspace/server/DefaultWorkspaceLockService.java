/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
