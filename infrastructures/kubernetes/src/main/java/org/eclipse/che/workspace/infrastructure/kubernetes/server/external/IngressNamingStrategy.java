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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import io.fabric8.kubernetes.api.model.ServicePort;
import org.eclipse.che.commons.annotation.Nullable;

public interface IngressNamingStrategy {

  @Nullable
  String getIngressHost(String serviceName, ServicePort servicePort);

  String getIngressName(String serviceName, ServicePort servicePort);

  String getIngressPath(String serviceName, ServicePort servicePort);
}
