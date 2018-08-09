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
import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.provision.InternalEnvironmentProvisioner;
import org.eclipse.che.api.workspace.server.wsnext.WorkspaceNextApplier;
import org.eclipse.che.api.workspace.server.wsnext.WorkspaceNextObjectsRetriever;
import org.eclipse.che.api.workspace.server.wsnext.model.PluginMeta;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage.DockerImageEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.convert.DockerImageEnvironmentConverter;
import org.slf4j.Logger;

/** @author Sergii Leshchenko */
@Singleton
public class KubernetesInfrastructure extends RuntimeInfrastructure {

  public static final String NAME = "kubernetes";

  private static final Logger LOG = getLogger(KubernetesInfrastructure.class);

  private final DockerImageEnvironmentConverter dockerImageEnvConverter;
  private final KubernetesRuntimeContextFactory runtimeContextFactory;
  private final KubernetesEnvironmentProvisioner k8sEnvProvisioner;
  private final KubernetesRuntimeStateCache runtimeStatusesCache;
  private final Map<String, WorkspaceNextApplier> workspaceNextAppliers;
  private final WorkspaceNextObjectsRetriever workspaceNextObjectsRetriever;

  @Inject
  public KubernetesInfrastructure(
      EventService eventService,
      KubernetesRuntimeContextFactory runtimeContextFactory,
      KubernetesEnvironmentProvisioner k8sEnvProvisioner,
      Set<InternalEnvironmentProvisioner> internalEnvProvisioners,
      DockerImageEnvironmentConverter dockerImageEnvConverter,
      KubernetesRuntimeStateCache runtimeStatusesCache,
      Map<String, WorkspaceNextApplier> workspaceNextAppliers,
      WorkspaceNextObjectsRetriever workspaceNextObjectsRetriever) {
    super(
        NAME,
        ImmutableSet.of(KubernetesEnvironment.TYPE, DockerImageEnvironment.TYPE),
        eventService,
        internalEnvProvisioners);
    this.runtimeContextFactory = runtimeContextFactory;
    this.k8sEnvProvisioner = k8sEnvProvisioner;
    this.dockerImageEnvConverter = dockerImageEnvConverter;
    this.runtimeStatusesCache = runtimeStatusesCache;
    this.workspaceNextAppliers = ImmutableMap.copyOf(workspaceNextAppliers);
    this.workspaceNextObjectsRetriever = workspaceNextObjectsRetriever;
  }

  @Override
  public Set<RuntimeIdentity> getIdentities() throws InfrastructureException {
    return runtimeStatusesCache.getIdentities();
  }

  @Override
  public RuntimeContext prepare(RuntimeIdentity id, InternalEnvironment environment)
      throws ValidationException, InfrastructureException {

    applyWorkspaceNext(environment);

    return super.prepare(id, environment);
  }

  @Override
  protected KubernetesRuntimeContext internalPrepare(
      RuntimeIdentity id, InternalEnvironment environment) throws InfrastructureException {
    final KubernetesEnvironment kubernetesEnvironment = asKubernetesEnv(environment);

    k8sEnvProvisioner.provision(kubernetesEnvironment, id);

    return runtimeContextFactory.create(kubernetesEnvironment, id, this);
  }

  private KubernetesEnvironment asKubernetesEnv(InternalEnvironment source)
      throws InfrastructureException {
    if (source instanceof KubernetesEnvironment) {
      return (KubernetesEnvironment) source;
    }
    if (source instanceof DockerImageEnvironment) {
      return dockerImageEnvConverter.convert((DockerImageEnvironment) source);
    }
    throw new InternalInfrastructureException(
        format(
            "Environment type '%s' is not supported. Supported environment types: %s",
            source.getRecipe().getType(), KubernetesEnvironment.TYPE));
  }

  private void applyWorkspaceNext(InternalEnvironment environment) throws InfrastructureException {
    String recipeType = environment.getRecipe().getType();
    Collection<PluginMeta> pluginsMeta =
        workspaceNextObjectsRetriever.get(environment.getAttributes());
    if (pluginsMeta.isEmpty()) {
      return;
    }

    // TODO
    // Start container with a broker
    // Pass Metas to it
    // Retrieve actual state of Plugin broker
    // Once finished successfully fetch workspace tooling config
    // Consider pushing logs from Broker to workspace logs

    // TODO remove this. Added for the Walking skeleton development purposes
    LOG.error("Walking skeleton attributes: {}", environment.getAttributes());
    LOG.error("Walking skeleton metadata: {}", pluginsMeta);


    WorkspaceNextApplier wsNext = workspaceNextAppliers.get(recipeType);
    if (wsNext == null) {
      throw new InfrastructureException(
          "Workspace.Next configuration is not supported with recipe type " + recipeType);
    }
    // TODO Apply tooling config to InternalEnvironment
    // wsNext.apply(environment, plugins);
  }
}
