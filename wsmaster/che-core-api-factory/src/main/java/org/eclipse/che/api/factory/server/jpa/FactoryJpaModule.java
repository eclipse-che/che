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
package org.eclipse.che.api.factory.server.jpa;

import com.google.inject.AbstractModule;

import org.eclipse.che.api.factory.server.jpa.JpaFactoryDao.RemoveFactoriesBeforeUserRemovedEventSubscriber;
import org.eclipse.che.api.factory.server.spi.FactoryDao;

/**
 * @author Yevhenii Voevodin
 */
public class FactoryJpaModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(FactoryDao.class).to(JpaFactoryDao.class);
        bind(RemoveFactoriesBeforeUserRemovedEventSubscriber.class).asEagerSingleton();
    }
}
