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
package org.eclipse.che.workspace.infrastructure.openshift;

import com.google.inject.assistedinject.Assisted;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.Route;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.workspace.server.URLRewriter.NoOpURLRewriter;
import org.eclipse.che.api.workspace.server.hc.ServersCheckerFactory;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeScheduler;
import org.eclipse.che.api.workspace.server.hc.probe.WorkspaceProbesFactory;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInternalRuntime;
import org.eclipse.che.workspace.infrastructure.kubernetes.bootstrapper.KubernetesBootstrapperFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSharedPool;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProject;
import org.eclipse.che.workspace.infrastructure.openshift.server.OpenShiftServerResolver;

/**
 * @author Sergii Leshchenko
 * @author Anton Korneta
 */
public class OpenShiftInternalRuntime extends KubernetesInternalRuntime<OpenShiftRuntimeContext> {

  private final OpenShiftProject project;

  @Inject
  public OpenShiftInternalRuntime(
      @Named("che.infra.kubernetes.workspace_start_timeout_min") int workspaceStartTimeout,
      @Named("che.infra.kubernetes.ingress_start_timeout_min") int ingressStartTimeout,
      NoOpURLRewriter urlRewriter,
      KubernetesBootstrapperFactory bootstrapperFactory,
      ServersCheckerFactory serverCheckerFactory,
      WorkspaceVolumesStrategy volumesStrategy,
      ProbeScheduler probeScheduler,
      WorkspaceProbesFactory probesFactory,
      RuntimeEventsPublisher eventPublisher,
      KubernetesSharedPool sharedPool,
      @Assisted OpenShiftRuntimeContext context,
      @Assisted OpenShiftProject project,
      @Assisted List<Warning> warnings) {
    super(
        workspaceStartTimeout,
        ingressStartTimeout,
        urlRewriter,
        bootstrapperFactory,
        serverCheckerFactory,
        volumesStrategy,
        probeScheduler,
        probesFactory,
        eventPublisher,
        sharedPool,
        context,
        project,
        warnings);
    this.project = project;
  }

  @Override
  protected void startMachines() throws InfrastructureException {
    OpenShiftEnvironment osEnv = getContext().getEnvironment();
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
    // project.pods().watchContainers(new MachineLogsPublisher());

    doStartMachine(new OpenShiftServerResolver(createdServices, createdRoutes));
  }
}
