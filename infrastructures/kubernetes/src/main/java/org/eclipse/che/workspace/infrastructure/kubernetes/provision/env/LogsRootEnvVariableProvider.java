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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision.env;

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.provision.env.EnvVarProvider;
import org.eclipse.che.commons.lang.Pair;

/**
 * Add env variable to machines with path to root folder of workspace logs.
 *
 * @author Anton Korneta
 */
public class LogsRootEnvVariableProvider implements EnvVarProvider {

  /** Environment variable that points to root folder of projects inside machine */
  public static final String WORKSPACE_LOGS_ROOT_ENV_VAR = "CHE_WORKSPACE_LOGS_ROOT__DIR";

  private String logsRootPath;

  @Inject
  public LogsRootEnvVariableProvider(@Named("che.workspace.logs.root_dir") String logsRootPath) {
    this.logsRootPath = logsRootPath;
  }

  @Override
  public Pair<String, String> get(RuntimeIdentity identity) throws InfrastructureException {
    return Pair.of(WORKSPACE_LOGS_ROOT_ENV_VAR, logsRootPath);
  }
}
