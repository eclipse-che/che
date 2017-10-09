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
package org.eclipse.che.api.deploy;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import javax.sql.DataSource;
import org.eclipse.che.api.user.server.TokenValidator;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.workspace.infrastructure.docker.DockerInfraModule;
import org.eclipse.che.workspace.infrastructure.docker.local.LocalDockerModule;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftInfraModule;

/**
 * Single-user version Che specific bindings
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
@DynaModule
public class CheWsMasterModule extends AbstractModule {
  @Override
  protected void configure() {

    String infrastructure = System.getenv("CHE_INFRASTRUCTURE_ACTIVE");
    if ("openshift".equals(infrastructure)) {
      install(new OpenShiftInfraModule());
    } else {
      install(new LocalDockerModule());
      install(new DockerInfraModule());
    }

    bind(TokenValidator.class).to(org.eclipse.che.api.local.DummyTokenValidator.class);

    bind(org.eclipse.che.api.workspace.server.stack.StackLoader.class);
    bind(DataSource.class).toProvider(org.eclipse.che.core.db.h2.H2DataSourceProvider.class);

    install(new org.eclipse.che.api.user.server.jpa.UserJpaModule());
    install(new org.eclipse.che.api.workspace.server.jpa.WorkspaceJpaModule());

    bind(org.eclipse.che.api.user.server.CheUserCreator.class);

    bindConstant()
        .annotatedWith(Names.named("machine.terminal_agent.run_command"))
        .to(
            "$HOME/che/terminal/che-websocket-terminal "
                + "-addr :4411 "
                + "-cmd ${SHELL_INTERPRETER} "
                + "-enable-activity-tracking");
    bindConstant()
        .annotatedWith(Names.named("machine.exec_agent.run_command"))
        .to(
            "$HOME/che/exec-agent/che-exec-agent "
                + "-addr :4412 "
                + "-cmd ${SHELL_INTERPRETER} "
                + "-logs-dir $HOME/che/exec-agent/logs");
  }
}
