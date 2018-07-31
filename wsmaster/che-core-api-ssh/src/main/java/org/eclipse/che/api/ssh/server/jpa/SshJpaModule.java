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
package org.eclipse.che.api.ssh.server.jpa;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.ssh.server.jpa.JpaSshDao.RemoveSshKeysBeforeUserRemovedEventSubscriber;
import org.eclipse.che.api.ssh.server.spi.SshDao;

/** @author Yevhenii Voevodin */
public class SshJpaModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(SshDao.class).to(JpaSshDao.class);
    bind(RemoveSshKeysBeforeUserRemovedEventSubscriber.class).asEagerSingleton();
  }
}
