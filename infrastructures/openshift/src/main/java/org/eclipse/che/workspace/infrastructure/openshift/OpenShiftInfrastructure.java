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
package org.eclipse.che.workspace.infrastructure.openshift;

import static java.lang.String.format;

import com.google.common.base.Joiner;
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
import org.eclipse.che.workspace.infrastructure.kubernetes.DirectKubernetesAPIAccessHelper;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.NamespaceNameValidator;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProjectFactory;

/** @author Sergii Leshchenko */
@Singleton
public class OpenShiftInfrastructure extends RuntimeInfrastructure {

  public static final String NAME = "openshift";

  private final OpenShiftRuntimeContextFactory runtimeContextFactory;
  private final KubernetesRuntimeStateCache runtimeStatusesCache;
  private final OpenShiftProjectFactory projectFactory;
  private final OpenShiftClientFactory openShiftClientFactory;

  @Inject
  public OpenShiftInfrastructure(
      EventService eventService,
      OpenShiftRuntimeContextFactory runtimeContextFactory,
      Set<InternalEnvironmentProvisioner> internalEnvProvisioners,
      KubernetesRuntimeStateCache runtimeStatusesCache,
      OpenShiftProjectFactory projectFactory,
      OpenShiftClientFactory openShiftClientFactory) {
    super(
        NAME,
        ImmutableSet.of(
            OpenShiftEnvironment.TYPE,
            KubernetesEnvironment.TYPE,
            Constants.NO_ENVIRONMENT_RECIPE_TYPE),
        eventService,
        internalEnvProvisioners);
    this.runtimeContextFactory = runtimeContextFactory;
    this.runtimeStatusesCache = runtimeStatusesCache;
    this.projectFactory = projectFactory;
    this.openShiftClientFactory = openShiftClientFactory;
  }

  @Override
  public Set<RuntimeIdentity> getIdentities() throws InfrastructureException {
    return runtimeStatusesCache.getIdentities();
  }

  @Override
  public String evaluateInfraNamespace(NamespaceResolutionContext resolutionCtx)
      throws InfrastructureException {
    return projectFactory.evaluateNamespaceName(resolutionCtx);
  }

  @Override
  public boolean isNamespaceValid(String namespaceName) {
    return NamespaceNameValidator.isValid(namespaceName);
  }

  @Override
  protected OpenShiftRuntimeContext internalPrepare(
      RuntimeIdentity identity, InternalEnvironment environment) throws InfrastructureException {
    return runtimeContextFactory.create(asOpenShiftEnv(environment), identity, this);
  }

  @Override
  public Response sendDirectInfrastructureRequest(
      String httpMethod, URI relativeUri, @Nullable HttpHeaders headers, @Nullable InputStream body)
      throws InfrastructureException {
    return DirectKubernetesAPIAccessHelper.call(
        openShiftClientFactory.getDefaultConfig().getMasterUrl(),
        openShiftClientFactory.getAuthenticatedHttpClient(headers),
        httpMethod,
        relativeUri,
        headers,
        body);
  }

  private OpenShiftEnvironment asOpenShiftEnv(InternalEnvironment source)
      throws InfrastructureException {
    if (source instanceof NoEnvInternalEnvironment) {
      return OpenShiftEnvironment.builder(source).build();
    } else if (source instanceof OpenShiftEnvironment) {
      return (OpenShiftEnvironment) source;
    } else if (source instanceof KubernetesEnvironment) {
      return new OpenShiftEnvironment((KubernetesEnvironment) source);
    }

    throw new InternalInfrastructureException(
        format(
            "Environment type '%s' is not supported. Supported environment types: %s",
            source.getType(),
            Joiner.on(",").join(OpenShiftEnvironment.TYPE, KubernetesEnvironment.TYPE)));
  }
}
