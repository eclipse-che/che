/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.ssh.server.jpa;

import com.google.inject.AbstractModule;

import org.eclipse.che.api.ssh.server.jpa.JpaSshDao.RemoveSshKeysBeforeUserRemovedEventSubscriber;
import org.eclipse.che.api.ssh.server.spi.SshDao;

/**
 * @author Yevhenii Voevodin
 */
public class SshJpaModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SshDao.class).to(JpaSshDao.class);
        bind(RemoveSshKeysBeforeUserRemovedEventSubscriber.class).asEagerSingleton();
    }
}
