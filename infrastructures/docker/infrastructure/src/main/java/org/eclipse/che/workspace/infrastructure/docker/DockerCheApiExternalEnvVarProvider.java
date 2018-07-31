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
package org.eclipse.che.workspace.infrastructure.docker;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.provision.env.CheApiExternalEnvVarProvider;
import org.eclipse.che.commons.lang.Pair;

/**
 * Provides env variable to docker machine with url of Che external API.
 *
 * @author Mykhailo Kuznietsov
 */
@Singleton
public class DockerCheApiExternalEnvVarProvider implements CheApiExternalEnvVarProvider {

  private final Pair<String, String> apiEnvVar;

  @Inject
  public DockerCheApiExternalEnvVarProvider(@Named("che.api") String apiEndpoint) {
    apiEnvVar = Pair.of(CHE_API_EXTERNAL_VARIABLE, apiEndpoint);
  }

  @Override
  public Pair<String, String> get(RuntimeIdentity runtimeIdentity) {
    return apiEnvVar;
  }
}
