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
package org.eclipse.che.multiuser.api.account.personal;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.multiuser.api.permission.server.account.AccountPermissionsChecker;
import org.eclipse.che.multiuser.permission.account.PersonalAccountPermissionsChecker;
import org.eclipse.che.multiuser.resource.api.free.DefaultResourcesProvider;

/** @author Sergii Leschenko */
@DynaModule
public class PersonalAccountModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), DefaultResourcesProvider.class)
        .addBinding()
        .to(DefaultUserResourcesProvider.class);

    Multibinder.newSetBinder(binder(), AccountPermissionsChecker.class)
        .addBinding()
        .to(PersonalAccountPermissionsChecker.class);

    bind(UserManager.class).to(PersonalAccountUserManager.class);
  }
}
