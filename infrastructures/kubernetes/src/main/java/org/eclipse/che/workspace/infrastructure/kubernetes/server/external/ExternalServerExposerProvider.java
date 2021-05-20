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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import javax.inject.Provider;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Provides {@link ExternalServerExposer} implementations based on configuration.
 *
 * @param <T> type of environment
 */
public interface ExternalServerExposerProvider<T extends KubernetesEnvironment>
    extends Provider<ExternalServerExposer<T>> {}
