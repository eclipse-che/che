/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.resources.impl;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.resources.Folder;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.resource.Path;

/**
 * Default implementation of the {@code Folder}.
 *
 * @author Vlad Zhukovskyi
 * @see ContainerImpl
 * @see Folder
 * @since 4.4.0
 */
@Beta
class FolderImpl extends ContainerImpl implements Folder {

  @Inject
  protected FolderImpl(
      @Assisted Path path,
      @Assisted ResourceManager resourceManager,
      PromiseProvider promiseProvider) {
    super(path, resourceManager, promiseProvider);
  }

  /** {@inheritDoc} */
  @Override
  public final int getResourceType() {
    return FOLDER;
  }

  /** {@inheritDoc} */
  @Override
  public Project.ProjectRequest toProject() {
    return new Project.ProjectRequest() {
      private ProjectConfig config;

      /** {@inheritDoc} */
      @Override
      public Request<Project, ProjectConfig> withBody(ProjectConfig object) {
        this.config = object;
        return this;
      }

      /** {@inheritDoc} */
      @Override
      public ProjectConfig getBody() {
        return config;
      }

      /** {@inheritDoc} */
      @Override
      public Promise<Project> send() {
        return resourceManager.update(getLocation(), this);
      }
    };
  }
}
