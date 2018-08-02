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
package org.eclipse.che.api.factory.server.jpa;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.factory.server.jpa.JpaFactoryDao.RemoveFactoriesBeforeUserRemovedEventSubscriber;
import org.eclipse.che.api.factory.server.spi.FactoryDao;

/** @author Yevhenii Voevodin */
public class FactoryJpaModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(FactoryDao.class).to(JpaFactoryDao.class);
    bind(RemoveFactoriesBeforeUserRemovedEventSubscriber.class).asEagerSingleton();
  }
}
