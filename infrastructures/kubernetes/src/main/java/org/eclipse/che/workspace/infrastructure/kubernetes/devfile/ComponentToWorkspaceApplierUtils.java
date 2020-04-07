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
package org.eclipse.che.workspace.infrastructure.kubernetes.devfile;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PUBLIC_ENDPOINT_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.devfile.DockerimageComponentToWorkspaceApplier.CHE_COMPONENT_NAME_LABEL;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import java.util.HashMap;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.devfile.Endpoint;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;

public final class ComponentToWorkspaceApplierUtils {
  private ComponentToWorkspaceApplierUtils() {}

  static ServerConfigImpl toServerConfig(Endpoint endpoint) {
    HashMap<String, String> attributes = new HashMap<>(endpoint.getAttributes());

    String protocol = attributes.remove("protocol");
    if (isNullOrEmpty(protocol)) {
      protocol = "http";
    }

    String path = attributes.remove("path");

    String isPublic = attributes.remove(PUBLIC_ENDPOINT_ATTRIBUTE);
    if ("false".equals(isPublic)) {
      ServerConfig.setInternal(attributes, true);
    }

    return new ServerConfigImpl(Integer.toString(endpoint.getPort()), protocol, path, attributes);
  }

  static Service createService(String componentName, Endpoint endpoint) {
    ServicePort servicePort =
        new ServicePortBuilder()
            .withPort(endpoint.getPort())
            .withProtocol("TCP")
            .withNewTargetPort(endpoint.getPort())
            .build();
    return new ServiceBuilder()
        .withNewMetadata()
        .withName(endpoint.getName())
        .endMetadata()
        .withNewSpec()
        .withSelector(ImmutableMap.of(CHE_COMPONENT_NAME_LABEL, componentName))
        .withPorts(singletonList(servicePort))
        .endSpec()
        .build();
  }
}
