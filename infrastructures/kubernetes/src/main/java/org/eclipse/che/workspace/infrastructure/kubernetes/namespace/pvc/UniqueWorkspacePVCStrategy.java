/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc;

import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.tracing.TracingTags;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesPersistentVolumeClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a unique PVC for each volume of a workspace.
 *
 * <p>Names for PVCs are evaluated as: '{configured_prefix}' + '-' +'{generated_8_chars}' to avoid
 * naming collisions inside of one Kubernetes namespace.
 *
 * <p>Note that for in this strategy number of simultaneously used volumes by workspaces can not be
 * greater than the number of available PVCs in Kubernetes namespace.
 *
 * <p><b>Used subpaths:</b>
 *
 * <p>This strategy uses subpaths to do the same as {@link CommonPVCStrategy} does and make easier
 * data migration if it will be needed.<br>
 * Subpaths have the following format: '{workspaceId}/{volume/PVC name}'.<br>
 * Note that logs volume has the special format: '{workspaceId}/{volumeName}/{machineName}'. It is
 * done in this way to avoid conflicts e.g. two identical agents inside different machines produce
 * the same log file.
 *
 * <p><b>How user-defined PVCs are processed:</b>
 *
 * <p>User-defined PVCs are provisioned with generated unique names. Pods volumes that reference
 * PVCs are updated accordingly. Subpaths of the corresponding containers volume mounts are prefixed
 * with `'{workspaceId}/{originalPVCName}'`.
 *
 * <p>User-defined PVC name is used as Che Volume name. It means that if Machine is configured to
 * use Che Volume with the same name as user-defined PVC has then Che Volume will reuse user-defined
 * PVC.
 *
 * <p>Note that quantity and access mode of user-defined PVCs are not overridden with Che Server
 * configured.
 *
 * <p><b>Clean up:</b>
 *
 * <p>Cleanup of backed up data is performed by removing of PVCs related to the workspace but when
 * the volume or machine name is changed then related PVC would not be removed.
 *
 * @author Anton Korneta
 * @author Alexander Garagatyi
 */
public class UniqueWorkspacePVCStrategy implements WorkspaceVolumesStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(UniqueWorkspacePVCStrategy.class);

  public static final String UNIQUE_STRATEGY = "unique";

  private final KubernetesNamespaceFactory factory;
  private final EphemeralWorkspaceAdapter ephemeralWorkspaceAdapter;
  private final PVCProvisioner pvcProvisioner;
  private final SubPathPrefixes subpathPrefixes;
  private final boolean waitBound;

  @Inject
  public UniqueWorkspacePVCStrategy(
      @Named("che.infra.kubernetes.pvc.wait_bound") boolean waitBound,
      KubernetesNamespaceFactory factory,
      EphemeralWorkspaceAdapter ephemeralWorkspaceAdapter,
      PVCProvisioner pvcProvisioner,
      SubPathPrefixes subpathPrefixes) {
    this.waitBound = waitBound;
    this.factory = factory;
    this.ephemeralWorkspaceAdapter = ephemeralWorkspaceAdapter;
    this.pvcProvisioner = pvcProvisioner;
    this.subpathPrefixes = subpathPrefixes;
  }

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    final String workspaceId = identity.getWorkspaceId();
    if (EphemeralWorkspaceUtility.isEphemeral(k8sEnv.getAttributes())) {
      ephemeralWorkspaceAdapter.provision(k8sEnv, identity);
      return;
    }

    LOG.debug("Provisioning PVC strategy for workspace '{}'", workspaceId);

    Map<String, PersistentVolumeClaim> userDefinedPVCs =
        new HashMap<>(k8sEnv.getPersistentVolumeClaims());

    k8sEnv.getPersistentVolumeClaims().clear();
    fillInExistingPVCs(k8sEnv, identity);

    pvcProvisioner.provision(k8sEnv, userDefinedPVCs);

    pvcProvisioner.convertCheVolumes(k8sEnv, workspaceId);

    subpathPrefixes.prefixVolumeMountsSubpaths(k8sEnv, identity.getWorkspaceId());

    provisionWorkspaceIdLabel(k8sEnv.getPersistentVolumeClaims(), identity.getWorkspaceId());

    LOG.debug("PVC strategy provisioning done for workspace '{}'", workspaceId);
  }

  @Traced
  @Override
  public void prepare(
      KubernetesEnvironment k8sEnv,
      RuntimeIdentity identity,
      long timeoutMillis,
      Map<String, String> startOptions)
      throws InfrastructureException {
    String workspaceId = identity.getWorkspaceId();

    TracingTags.WORKSPACE_ID.set(workspaceId);

    if (EphemeralWorkspaceUtility.isEphemeral(k8sEnv.getAttributes())) {
      return;
    }

    if (k8sEnv.getPersistentVolumeClaims().isEmpty()) {
      // no PVCs to prepare
      return;
    }

    final KubernetesPersistentVolumeClaims k8sClaims =
        factory.getOrCreate(identity).persistentVolumeClaims();
    LOG.debug("Creating PVCs for workspace '{}'", workspaceId);
    k8sClaims.createIfNotExist(k8sEnv.getPersistentVolumeClaims().values());

    if (waitBound) {
      LOG.debug("Waiting for PVC(s) of workspace '{}' to be bound", workspaceId);
      for (PersistentVolumeClaim pvc : k8sEnv.getPersistentVolumeClaims().values()) {
        k8sClaims.waitBound(pvc.getMetadata().getName(), timeoutMillis);
      }
    }
    LOG.debug("Preparing PVCs done for workspace '{}'", workspaceId);
  }

  @Override
  public void cleanup(Workspace workspace) throws InfrastructureException {
    if (EphemeralWorkspaceUtility.isEphemeral(workspace)) {
      return;
    }
    factory
        .get(workspace)
        .persistentVolumeClaims()
        .delete(ImmutableMap.of(CHE_WORKSPACE_ID_LABEL, workspace.getId()));
  }

  private void fillInExistingPVCs(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    Map<String, PersistentVolumeClaim> existingPVCs =
        factory
            .getOrCreate(identity)
            .persistentVolumeClaims()
            .getByLabel(CHE_WORKSPACE_ID_LABEL, identity.getWorkspaceId())
            .stream()
            .collect(toMap(pvc -> pvc.getMetadata().getName(), Function.identity()));

    k8sEnv.getPersistentVolumeClaims().putAll(existingPVCs);
  }

  private void provisionWorkspaceIdLabel(
      Map<String, PersistentVolumeClaim> pvcs, String workspaceId) {
    pvcs.values()
        .forEach(pvc -> pvc.getMetadata().getLabels().put(CHE_WORKSPACE_ID_LABEL, workspaceId));
  }
}
