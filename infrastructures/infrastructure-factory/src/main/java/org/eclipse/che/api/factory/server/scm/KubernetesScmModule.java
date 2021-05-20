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
package org.eclipse.che.api.factory.server.scm;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.factory.server.scm.kubernetes.KubernetesGitCredentialManager;
import org.eclipse.che.api.factory.server.scm.kubernetes.KubernetesPersonalAccessTokenManager;

public class KubernetesScmModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(GitCredentialManager.class).to(KubernetesGitCredentialManager.class);
    bind(PersonalAccessTokenManager.class).to(KubernetesPersonalAccessTokenManager.class);
  }
}
