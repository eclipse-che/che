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
 *   SAP           - implementation
 */
package org.eclipse.che.git.impl.jgit;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.git.GitConnectionFactory;

/**
 * Guice module to install jgit implementation of git components
 *
 * @author Sergii Kabashnyuk
 */
public class JGitModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(GitConnectionFactory.class).to(JGitConnectionFactory.class);
  }
}
