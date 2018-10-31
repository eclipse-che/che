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

import com.google.inject.assistedinject.Assisted;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.Route;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.workspace.server.URLRewriter.NoOpURLRewriter;
import org.eclipse.che.api.workspace.server.hc.ServersCheckerFactory;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeScheduler;
import org.eclipse.che.api.workspace.server.hc.probe.WorkspaceProbesFactory;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.provision.InternalEnvironmentProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInternalRuntime;
import org.eclipse.che.workspace.infrastructure.kubernetes.RuntimeHangingDetector;
import org.eclipse.che.workspace.infrastructure.kubernetes.StartSynchronizerFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.bootstrapper.KubernetesBootstrapperFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSharedPool;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.UnrecoverablePodEventListener;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.UnrecoverablePodEventListenerFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.SidecarToolingProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProject;
import org.eclipse.che.workspace.infrastructure.openshift.server.OpenShiftServerResolver;

/**
 * @author Sergii Leshchenko
 * @author Anton Korneta
 */
public class OpenShiftInternalRuntime extends KubernetesInternalRuntime<OpenShiftEnvironment> {

  private final OpenShiftProject project;
  private final UnrecoverablePodEventListenerFactory unrecoverablePodEventListenerFactory;

  @Inject
  public OpenShiftInternalRuntime(
      @Named("che.infra.kubernetes.workspace_start_timeout_min") int workspaceStartTimeout,
      @Named("che.infra.kubernetes.ingress_start_timeout_min") int ingressStartTimeout,
      NoOpURLRewriter urlRewriter,
      UnrecoverablePodEventListenerFactory unrecoverablePodEventListenerFactory,
      KubernetesBootstrapperFactory bootstrapperFactory,
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
      @Assisted OpenShiftRuntimeContext context,
      @Assisted OpenShiftProject project,
      @Assisted List<Warning> warnings) {
    super(
        workspaceStartTimeout,
        ingressStartTimeout,
        urlRewriter,
        unrecoverablePodEventListenerFactory,
        bootstrapperFactory,
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
        context,
        project,
        warnings);
    this.project = project;
    this.unrecoverablePodEventListenerFactory = unrecoverablePodEventListenerFactory;
  }

  @Override
  protected void startMachines() throws InfrastructureException {
    OpenShiftEnvironment osEnv = getContext().getEnvironment();

    for (Secret secret : osEnv.getSecrets().values()) {
      project.secrets().create(secret);
    }

    for (ConfigMap configMap : osEnv.getConfigMaps().values()) {
      project.configMaps().create(configMap);
    }

    List<Service> createdServices = new ArrayList<>();
    for (Service service : osEnv.getServices().values()) {
      createdServices.add(project.services().create(service));
    }

    List<Route> createdRoutes = new ArrayList<>();
    for (Route route : osEnv.getRoutes().values()) {
      createdRoutes.add(project.routes().create(route));
    }
    // TODO https://github.com/eclipse/che/issues/7653
    // project.pods().watch(new AbnormalStopHandler());

    project.deployments().watchEvents(new MachineLogsPublisher());
    if (unrecoverablePodEventListenerFactory.isConfigured()) {
      Map<String, Pod> pods = getContext().getEnvironment().getPods();
      UnrecoverablePodEventListener handler =
          unrecoverablePodEventListenerFactory.create(
              pods.keySet(), this::handleUnrecoverableEvent);
      project.deployments().watchEvents(handler);
    }

    doStartMachine(new OpenShiftServerResolver(createdServices, createdRoutes));
  }
}
