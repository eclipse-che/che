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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import static java.lang.String.format;

import com.google.common.collect.ImmutableSet;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.NoEnvironmentFactory.NoEnvInternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.NamespaceResolutionContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.provision.InternalEnvironmentProvisioner;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.NamespaceNameValidator;

/** @author Sergii Leshchenko */
@Singleton
public class KubernetesInfrastructure extends RuntimeInfrastructure {

  public static final String NAME = "kubernetes";

  private final KubernetesRuntimeContextFactory runtimeContextFactory;
  private final KubernetesRuntimeStateCache runtimeStatusesCache;
  private final KubernetesNamespaceFactory namespaceFactory;
  private final KubernetesClientFactory kubernetesClientFactory;

  @Inject
  public KubernetesInfrastructure(
      EventService eventService,
      KubernetesRuntimeContextFactory runtimeContextFactory,
      Set<InternalEnvironmentProvisioner> internalEnvProvisioners,
      KubernetesRuntimeStateCache runtimeStatusesCache,
      KubernetesNamespaceFactory namespaceFactory,
      KubernetesClientFactory kubernetesClientFactory) {
    super(
        NAME,
        ImmutableSet.of(KubernetesEnvironment.TYPE, Constants.NO_ENVIRONMENT_RECIPE_TYPE),
        eventService,
        internalEnvProvisioners);
    this.runtimeContextFactory = runtimeContextFactory;
    this.runtimeStatusesCache = runtimeStatusesCache;
    this.namespaceFactory = namespaceFactory;
    this.kubernetesClientFactory = kubernetesClientFactory;
  }

  @Override
  public Set<RuntimeIdentity> getIdentities() throws InfrastructureException {
    return runtimeStatusesCache.getIdentities();
  }

  @Override
  public String evaluateInfraNamespace(NamespaceResolutionContext resolutionCtx)
      throws InfrastructureException {
    return namespaceFactory.evaluateNamespaceName(resolutionCtx);
  }

  @Override
  public String evaluateLegacyInfraNamespace(NamespaceResolutionContext resolutionContext)
      throws InfrastructureException {
    return namespaceFactory.evaluateLegacyNamespaceName(resolutionContext);
  }

  @Override
  public boolean isNamespaceValid(String name) {
    return NamespaceNameValidator.isValid(name);
  }

  @Override
  public Response sendDirectInfrastructureRequest(
      String httpMethod, URI relativeUri, @Nullable HttpHeaders headers, @Nullable InputStream body)
      throws InfrastructureException {
    return DirectKubernetesAPIAccessHelper.call(
        kubernetesClientFactory.getDefaultConfig().getMasterUrl(),
        kubernetesClientFactory.getAuthenticatedHttpClient(headers),
        httpMethod,
        relativeUri,
        headers,
        body);
  }

  @Override
  protected KubernetesRuntimeContext internalPrepare(
      RuntimeIdentity id, InternalEnvironment environment) throws InfrastructureException {
    return runtimeContextFactory.create(asKubernetesEnv(environment), id, this);
  }

  private KubernetesEnvironment asKubernetesEnv(InternalEnvironment source)
      throws InfrastructureException {
    if (source instanceof NoEnvInternalEnvironment) {
      return KubernetesEnvironment.builder(source).build();
    } else if (source instanceof KubernetesEnvironment) {
      return (KubernetesEnvironment) source;
    }

    throw new InternalInfrastructureException(
        format(
            "Environment type '%s' is not supported. Supported environment types: %s",
            source.getType(), KubernetesEnvironment.TYPE));
  }
}
