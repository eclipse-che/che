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
package org.eclipse.che.security.oauth;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.inject.DynaModule;

/**
 * Setup GitHubOAuthAuthenticator in guice container.
 *
 * @author Sergii Kabashniuk
 */
@DynaModule
public class GithubModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder<OAuthAuthenticator> oAuthAuthenticators =
        Multibinder.newSetBinder(binder(), OAuthAuthenticator.class);
    oAuthAuthenticators.addBinding().to(GitHubOAuthAuthenticator.class);
  }
}
