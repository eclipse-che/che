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

import static org.slf4j.LoggerFactory.getLogger;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEventHandler;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.slf4j.Logger;

/** Listens pod events and publish them as machine logs. */
public class PodLogsPublisher implements PodEventHandler {

  private static final Logger LOG = getLogger(PodLogsPublisher.class);

  private final RuntimeEventsPublisher eventPublisher;
  private final RuntimeIdentity runtimeIdentity;
  //  private final KubernetesMachineCache machines;

  public PodLogsPublisher(RuntimeEventsPublisher eventPublisher, RuntimeIdentity runtimeIdentity) {
    this.eventPublisher = eventPublisher;
    this.runtimeIdentity = runtimeIdentity;
  }

  @Override
  public void handle(PodEvent event) {
    final String podName = event.getPodName();
    eventPublisher.sendMachineLogEvent(
        "[[[" + podName + "]]]", event.getMessage(), event.getCreationTimeStamp(), runtimeIdentity);
  }

  //  private void watchContainerLogs(String podName, ObjectReference involvedObject) {
  //    try {
  //
  //      //        "spec.containers{che-machine-execnlu}"
  //      String c = involvedObject.getFieldPath();
  //      if (c.startsWith("spec.containers{")) {
  //        c = c.replace("spec.containers{", "");
  //        c = c.replace("}", "");
  //
  //        KubernetesClient client = clientFactory.create(workspaceId);
  //
  //        LOG.info("watch [{}:{}] logs !!!", podName, c);
  //
  //        PrintStream print =
  //            new PrintStream(
  //                "/home/mvala/tmp/eh_logs/"
  //                    + Instant.now().toEpochMilli()
  //                    + "_"
  //                    + podName
  //                    + ".log");
  //        try (LogWatch watch =
  //            client
  //                .pods()
  //                .inNamespace(namespace)
  //                .withName(podName)
  //                .inContainer(c)
  //                .withPrettyOutput()
  //                .watchLog(print)) {
  //          if (watch.getOutput() != null) {
  //            String message = IOUtils.toString(watch.getOutput(), UTF_8);
  //            LOG.info("podddd[{}] ->> {}", podName, message);
  //          } else {
  //            LOG.info("hmmmmmmm null again");
  //          }
  //        }
  //      }
  //    } catch (InfrastructureException | IOException e) {
  //      e.printStackTrace();
  //    }
  //  }
}
