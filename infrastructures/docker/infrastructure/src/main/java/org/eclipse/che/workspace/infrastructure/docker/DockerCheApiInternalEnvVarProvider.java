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

import com.google.common.base.Strings;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.provision.env.CheApiInternalEnvVarProvider;
import org.eclipse.che.commons.lang.Pair;

/**
 * Provides env variable to docker machine with url of Che API.
 *
 * @author Mykhailo Kuznietsov
 */
@Singleton
public class DockerCheApiInternalEnvVarProvider implements CheApiInternalEnvVarProvider {

  private static final String CHE_API_VARIABLE = "CHE_API";

  private final Pair<String, String> apiEnvVar;

  @Inject
  public DockerCheApiInternalEnvVarProvider(
      @Named("che.infra.docker.master_api_endpoint") String apiEndpoint) {
    String apiEndpointEnvVar = System.getenv(CHE_API_VARIABLE);
    if (Strings.isNullOrEmpty(apiEndpoint) && !Strings.isNullOrEmpty(apiEndpointEnvVar)) {
      apiEndpoint = apiEndpointEnvVar;
    }
    apiEnvVar = Pair.of(CHE_API_INTERNAL_VARIABLE, apiEndpoint);
  }

  @Override
  public Pair<String, String> get(RuntimeIdentity runtimeIdentity) {
    return new Pair<>(apiEnvVar.first, apiEnvVar.second);
  }
}
