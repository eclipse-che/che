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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.resolver;

import io.fabric8.kubernetes.api.model.Service;
import java.util.Map;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposer;

/**
 * Helps to resolve {@link ServerImpl servers} by machine name according to {@link Service services}
 * and implementation specific k8s objects.
 *
 * <p>Objects annotations are used to check if they exposes the specified machine servers.
 *
 * @see ExternalServerExposer
 * @see Annotations
 */
public interface ServerResolver {

  /**
   * Resolves servers by the specified machine name.
   *
   * @param machineName machine to resolve servers
   * @return resolved servers
   */
  Map<String, ServerImpl> resolve(String machineName);

  /**
   * Resolve external servers from implementation specific k8s object and it's annotations.
   *
   * @param machineName machine to resolve servers
   * @return resolved servers
   */
  Map<String, ServerImpl> resolveExternalServers(String machineName);
}
