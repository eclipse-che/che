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
package org.eclipse.che.selenium.core;

import com.google.inject.AbstractModule;
import org.eclipse.che.selenium.core.client.keycloak.cli.DockerKeycloakCliCommandExecutor;
import org.eclipse.che.selenium.core.client.keycloak.cli.KeycloakCliCommandExecutor;
import org.eclipse.che.selenium.core.workspace.CheTestDockerWorkspaceLogsReader;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceLogsReader;

/** @author Dmytro Nochevnov */
public class CheSeleniumDockerModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(TestWorkspaceLogsReader.class).to(CheTestDockerWorkspaceLogsReader.class);
    bind(KeycloakCliCommandExecutor.class).to(DockerKeycloakCliCommandExecutor.class);
  }
}
