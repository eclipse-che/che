/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Helps to create {@link SecureServerExposer} instances.
 *
 * <p>Note that ONE {@link SecureServerExposer} instance should be used for one workspace start.
 *
 * @author Sergii Leshchenko
 */
public interface SecureServerExposerFactory<T extends KubernetesEnvironment> {
  SecureServerExposer<T> create(RuntimeIdentity runtimeId);
}
