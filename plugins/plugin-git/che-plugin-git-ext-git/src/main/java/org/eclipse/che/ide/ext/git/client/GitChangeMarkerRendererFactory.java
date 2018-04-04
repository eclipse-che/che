/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
