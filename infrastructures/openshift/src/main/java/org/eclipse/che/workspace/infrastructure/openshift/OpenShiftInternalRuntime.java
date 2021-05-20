/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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

import com.google.inject.assistedinject.Assisted;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.Route;
import io.opentracing.Tracer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.URLRewriter.NoOpURLRewriter;
import org.eclipse.che.api.workspace.server.hc.ServersCheckerFactory;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeScheduler;
import org.eclipse.che.api.workspace.server.hc.probe.WorkspaceProbesFactory;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.provision.InternalEnvironmentProvisioner;
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.tracing.TracingTags;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInternalRuntime;
import org.eclipse.che.workspace.infrastructure.kubernetes.RuntimeCleaner;
import org.eclipse.che.workspace.infrastructure.kubernetes.RuntimeHangingDetector;
import org.eclipse.che.workspace.infrastructure.kubernetes.StartSynchronizerFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.CheNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.SecretAsContainerResourceProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSharedPool;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.UnrecoverablePodEventListenerFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.SidecarToolingProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProject;
import org.eclipse.che.workspace.infrastructure.openshift.provision.OpenShiftPreviewUrlCommandProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.server.OpenShiftServerResolverFactory;

/**
 * @author Sergii Leshchenko
 * @author Anton Korneta
 */
public class OpenShiftInternalRuntime extends KubernetesInternalRuntime<OpenShiftEnvironment> {

  private final OpenShiftProject project;
  private final OpenShiftServerResolverFactory serverResolverFactory;

  @Inject
  public OpenShiftInternalRuntime(
      @Named("che.infra.kubernetes.workspace_start_timeout_min") int workspaceStartTimeout,
      @Named("che.infra.kubernetes.ingress_start_timeout_min") int ingressStartTimeout,
      NoOpURLRewriter urlRewriter,
      UnrecoverablePodEventListenerFactory unrecoverablePodEventListenerFactory,
      ServersCheckerFactory serverCheckerFactory,
      WorkspaceVolumesStrategy volumesStrategy,
      ProbeScheduler probeScheduler,
      WorkspaceProbesFactory probesFactory,
      RuntimeEventsPublisher eventPublisher,
      KubernetesSharedPool sharedPool,
      KubernetesRuntimeStateCache runtimesStatusesCache,
      KubernetesMachineCache machinesCache,
      StartSynchronizerFactory startSynchronizerFactory,
      Set<InternalEnvironmentProvisioner> internalEnvironmentProvisioners,
      OpenShiftEnvironmentProvisioner kubernetesEnvironmentProvisioner,
      SidecarToolingProvisioner<OpenShiftEnvironment> toolingProvisioner,
      RuntimeHangingDetector runtimeHangingDetector,
      OpenShiftPreviewUrlCommandProvisioner previewUrlCommandProvisioner,
      SecretAsContainerResourceProvisioner<OpenShiftEnvironment>
          secretAsContainerResourceProvisioner,
      OpenShiftServerResolverFactory serverResolverFactory,
      RuntimeCleaner runtimeCleaner,
      CheNamespace cheNamespace,
      Tracer tracer,
      @Assisted OpenShiftRuntimeContext context,
      @Assisted OpenShiftProject project) {
    super(
        workspaceStartTimeout,
        ingressStartTimeout,
        urlRewriter,
        unrecoverablePodEventListenerFactory,
        serverCheckerFactory,
        volumesStrategy,
        probeScheduler,
        probesFactory,
        eventPublisher,
        sharedPool,
        runtimesStatusesCache,
        machinesCache,
        startSynchronizerFactory,
        internalEnvironmentProvisioners,
        kubernetesEnvironmentProvisioner,
        toolingProvisioner,
        runtimeHangingDetector,
        previewUrlCommandProvisioner,
        secretAsContainerResourceProvisioner,
        null,
        runtimeCleaner,
        cheNamespace,
        tracer,
        context,
        project);
    this.project = project;
    this.serverResolverFactory = serverResolverFactory;
  }

  @Override
  @Traced
  protected void startMachines() throws InfrastructureException {
    OpenShiftEnvironment osEnv = getContext().getEnvironment();
    String workspaceId = getContext().getIdentity().getWorkspaceId();

    createSecrets(osEnv, workspaceId);
    List<ConfigMap> createdConfigMaps = createConfigMaps(osEnv, getContext().getIdentity());
    List<Service> createdServices = createServices(osEnv, workspaceId);
    List<Route> createdRoutes = createRoutes(osEnv, workspaceId);

    listenEvents();

    doStartMachine(serverResolverFactory.create(createdServices, createdRoutes, createdConfigMaps));
  }

  @Traced
  @SuppressWarnings("WeakerAccess") // package-private so that interception is possible
  void createSecrets(OpenShiftEnvironment env, String workspaceId) throws InfrastructureException {
    TracingTags.WORKSPACE_ID.set(workspaceId);
    for (Secret secret : env.getSecrets().values()) {
      project.secrets().create(secret);
    }
  }

  @Traced
  @SuppressWarnings("WeakerAccess") // package-private so that interception is possible
  List<Service> createServices(OpenShiftEnvironment env, String workspaceId)
      throws InfrastructureException {
    TracingTags.WORKSPACE_ID.set(workspaceId);
    Collection<Service> servicesToCreate = env.getServices().values();
    List<Service> createdServices = new ArrayList<>(servicesToCreate.size());
    for (Service service : servicesToCreate) {
      createdServices.add(project.services().create(service));
    }

    return createdServices;
  }

  @Traced
  @SuppressWarnings("WeakerAccess") // package-private so that interception is possible
  List<Route> createRoutes(OpenShiftEnvironment env, String workspaceId)
      throws InfrastructureException {
    TracingTags.WORKSPACE_ID.set(workspaceId);
    Collection<Route> routesToCreate = env.getRoutes().values();
    List<Route> createdRoutes = new ArrayList<>(routesToCreate.size());
    for (Route route : routesToCreate) {
      createdRoutes.add(project.routes().create(route));
    }

    return createdRoutes;
  }
}
