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
package org.eclipse.che.workspace.infrastructure.openshift.provision.restartpolicy;

import static java.lang.String.format;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.WarningImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.provision.ConfigurationProvisioner;

/**
 * Rewrites restart policy to supported one - 'Never'.
 *
 * @author Alexander Garagatyi
 */
public class RestartPolicyRewriter implements ConfigurationProvisioner {
  static final String DEFAULT_RESTART_POLICY = "Never";

  @Override
  public void provision(OpenShiftEnvironment osEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    for (Pod podConfig : osEnv.getPods().values()) {
      final String podName = podConfig.getMetadata().getName();
      final PodSpec podSpec = podConfig.getSpec();
      rewriteRestartPolicy(podSpec, podName, osEnv);
    }
  }

  private void rewriteRestartPolicy(PodSpec podSpec, String podName, OpenShiftEnvironment env) {
    final String restartPolicy = podSpec.getRestartPolicy();

    if (restartPolicy != null && !DEFAULT_RESTART_POLICY.equalsIgnoreCase(restartPolicy)) {
      final String warnMsg =
          format(
              "Restart policy '%s' for pod '%s' is rewritten with %s",
              restartPolicy, podName, DEFAULT_RESTART_POLICY);
      env.addWarning(new WarningImpl(101, warnMsg));
    }
    podSpec.setRestartPolicy(DEFAULT_RESTART_POLICY);
  }
}
