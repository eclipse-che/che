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
package org.eclipse.che.multiuser.machine.authentication.server;

import static com.google.inject.matcher.Matchers.subclassesOf;
import static org.eclipse.che.inject.Matchers.names;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.token.MachineTokenProvider;

/**
 * Machine auth module.
 *
 * @author Max Shaposhnik
 * @author Sergii Leshchenko
 */
public class MachineAuthModule extends AbstractModule {
  @Override
  protected void configure() {
    final MachineTokenInterceptor tokenInterceptor = new MachineTokenInterceptor();
    requestInjection(tokenInterceptor);
    bindInterceptor(subclassesOf(WorkspaceRuntimes.class), names("startAsync"), tokenInterceptor);

    bind(MachineSessionInvalidator.class).asEagerSingleton();

    bind(MachineTokenProvider.class).to(MachineTokenProviderImpl.class);
  }
}
