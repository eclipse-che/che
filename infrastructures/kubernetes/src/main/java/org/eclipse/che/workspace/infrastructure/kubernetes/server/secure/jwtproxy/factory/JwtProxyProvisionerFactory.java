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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.factory;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.ProxyProvisionerFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.JwtProxyProvisioner;

/**
 * Helps to create {@link JwtProxyProvisioner} with fields injected from DI container.
 *
 * @author Sergii Leshchenko
 */
public interface JwtProxyProvisionerFactory extends ProxyProvisionerFactory {
  JwtProxyProvisioner create(RuntimeIdentity runtimeId);
}
