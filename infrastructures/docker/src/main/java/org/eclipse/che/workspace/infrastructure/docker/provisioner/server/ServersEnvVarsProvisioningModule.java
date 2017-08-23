/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.provisioner.server;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/** @author Alexander Garagatyi */
public class ServersEnvVarsProvisioningModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder<ServerEnvironmentVariableProvider> mb =
        Multibinder.newSetBinder(binder(), ServerEnvironmentVariableProvider.class);
    mb.addBinding().to(ApiEndpointEnvVariableProvider.class);
    mb.addBinding().to(JavaOptsEnvVariableProvider.class);
    mb.addBinding().to(MavenOptsEnvVariableProvider.class);
    mb.addBinding().to(ProjectsRootEnvVariableProvider.class);
    mb.addBinding().to(UserTokenEnvVarProvider.class);
    mb.addBinding().to(WorkspaceIdEnvVarProvider.class);
  }
}
