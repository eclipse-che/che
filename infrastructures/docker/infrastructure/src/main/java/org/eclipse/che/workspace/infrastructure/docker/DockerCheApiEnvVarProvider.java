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
package org.eclipse.che.workspace.infrastructure.docker;

import com.google.common.base.Strings;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.provision.env.CheApiEnvVarProvider;
import org.eclipse.che.commons.lang.Pair;

/**
 * Provides env variable to docker machine with url of Che API.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class DockerCheApiEnvVarProvider implements CheApiEnvVarProvider {

  private Pair<String, String> apiEnvVar;

  @Inject
  public DockerCheApiEnvVarProvider(
      @Named("che.infra.docker.master_api_endpoint") String apiEndpoint) {
    String apiEndpointEnvVar = System.getenv(API_ENDPOINT_URL_VARIABLE);
    if (Strings.isNullOrEmpty(apiEndpoint) && !Strings.isNullOrEmpty(apiEndpointEnvVar)) {
      apiEndpoint = apiEndpointEnvVar;
    }
    apiEnvVar = Pair.of(API_ENDPOINT_URL_VARIABLE, apiEndpoint);
  }

  @Override
  public Pair<String, String> get(RuntimeIdentity runtimeIdentity) {
    return new Pair<>(apiEnvVar.first, apiEnvVar.second);
  }
}
