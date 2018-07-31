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
package org.eclipse.che.plugin.github.factory.resolver;

import com.google.inject.AbstractModule;
import org.eclipse.che.inject.DynaModule;

/** @author Max Shaposhnik (mshaposhnik@codenvy.com) */
@DynaModule
public class GitHubFactoryModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(GithubURLParser.class).to(LegacyGithubURLParser.class);
  }
}
