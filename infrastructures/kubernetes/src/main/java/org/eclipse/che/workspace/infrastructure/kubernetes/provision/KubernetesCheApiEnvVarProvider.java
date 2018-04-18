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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.provision.env.CheApiEnvVarProvider;
import org.eclipse.che.commons.lang.Pair;

/**
 * Provides env variable to Kubernetes machine with url of Che API.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesCheApiEnvVarProvider implements CheApiEnvVarProvider {

  private final Pair<String, String> apiEnvVar;

  @Inject
  public KubernetesCheApiEnvVarProvider(@Named("che.api") String cheServerEndpoint) {
    this.apiEnvVar = Pair.of(CHE_API_VARIABLE, cheServerEndpoint);
  }

  @Override
  public Pair<String, String> get(RuntimeIdentity runtimeIdentity) throws InfrastructureException {
    return apiEnvVar;
  }
}
