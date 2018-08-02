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
package org.eclipse.che.ide.ext.git.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.editor.gutter.Gutter;
import org.eclipse.che.ide.api.vcs.VcsChangeMarkerRender;
import org.eclipse.che.ide.api.vcs.VcsChangeMarkerRenderFactory;
import org.eclipse.che.ide.ext.git.client.plugins.GitChangeMarkerRender;

/**
 * Git implementation of {@link VcsChangeMarkerRenderFactory}
 *
 * @author Igor Vinokur
 */
@Singleton
public class GitChangeMarkerRendererFactory implements VcsChangeMarkerRenderFactory {

  private final GitResources gitResources;

  @Inject
  public GitChangeMarkerRendererFactory(GitResources gitResources) {
    this.gitResources = gitResources;
  }

  @Override
  public VcsChangeMarkerRender create(Gutter hasGutter) {
    return new GitChangeMarkerRender(gitResources, hasGutter);
  }
}
