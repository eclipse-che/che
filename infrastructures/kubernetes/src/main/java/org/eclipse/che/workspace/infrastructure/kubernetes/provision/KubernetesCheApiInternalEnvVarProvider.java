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
import org.eclipse.che.api.workspace.server.spi.provision.env.CheApiInternalEnvVarProvider;
import org.eclipse.che.commons.lang.Pair;

/**
 * Provides env variable to Kubernetes machine with url of Che API.
 *
 * @author Mykhailo Kuznietsov
 */
public class KubernetesCheApiInternalEnvVarProvider implements CheApiInternalEnvVarProvider {

  private final String cheServerEndpoint;

  @Inject
  public KubernetesCheApiInternalEnvVarProvider(@Named("che.api") String cheServerEndpoint) {
    this.cheServerEndpoint = cheServerEndpoint;
  }

  @Override
  public Pair<String, String> get(RuntimeIdentity runtimeIdentity) throws InfrastructureException {
    return Pair.of(CHE_API_INTERNAL_VARIABLE, cheServerEndpoint);
  }
}
