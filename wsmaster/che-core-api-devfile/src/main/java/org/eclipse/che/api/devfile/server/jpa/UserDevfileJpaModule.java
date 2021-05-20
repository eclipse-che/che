/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.devfile.server.jpa;

import com.google.common.annotations.Beta;
import com.google.inject.AbstractModule;
import org.eclipse.che.api.devfile.server.RemoveUserDevfileBeforeAccountRemovedEventSubscriber;
import org.eclipse.che.api.devfile.server.spi.UserDevfileDao;

@Beta
public class UserDevfileJpaModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(UserDevfileDao.class).to(JpaUserDevfileDao.class);
    bind(RemoveUserDevfileBeforeAccountRemovedEventSubscriber.class).asEagerSingleton();
  }
}
