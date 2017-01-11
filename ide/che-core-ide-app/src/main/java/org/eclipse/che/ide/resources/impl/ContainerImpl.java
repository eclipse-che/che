/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.resources.impl;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;

import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Folder;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.resource.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Default implementation of the {@code Container}.
 *
 * @author Vlad Zhukovskyi
 * @see ResourceImpl
 * @see Container
 * @since 4.4.0
 */
@Beta
abstract class ContainerImpl extends ResourceImpl implements Container {

    protected PromiseProvider promiseProvider;

    protected ContainerImpl(Path path, ResourceManager resourceManager, PromiseProvider promiseProvider) {
        super(path, resourceManager);

        this.promiseProvider = checkNotNull(promiseProvider);
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Optional<File>> getFile(final Path relativePath) {
        return resourceManager.getFile(getLocation().append(relativePath));
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Optional<File>> getFile(String relativePath) {
        return resourceManager.getFile(getLocation().append(relativePath));
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Optional<Container>> getContainer(Path relativePath) {
        return resourceManager.getContainer(getLocation().append(relativePath));
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Optional<Container>> getContainer(String relativePath) {
        return resourceManager.getContainer(getLocation().append(relativePath));
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Resource[]> getChildren(final boolean forceUpdate) {
        return resourceManager.childrenOf(this, forceUpdate).thenPromise(new Function<Resource[], Promise<Resource[]>>() {
            /** {@inheritDoc} */
            @Override
            public Promise<Resource[]> apply(Resource[] children) throws FunctionException {
                if (children.length == 0 && !forceUpdate) {
                    return getChildren(true);
                }

                return promiseProvider.resolve(children);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Resource[]> getChildren() {
        return getChildren(false);
    }

    /** {@inheritDoc} */
    @Override
    public Project.ProjectRequest importProject() {
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
                return resourceManager.importProject(this);
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public Project.ProjectRequest newProject() {
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
                return resourceManager.createProject(this);
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Folder> newFolder(String name) {
        return resourceManager.createFolder(this, name);
    }

    /** {@inheritDoc} */
    @Override
    public Promise<File> newFile(String name, String content) {
        return resourceManager.createFile(this, name, content);
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Resource[]> synchronize() {
        return resourceManager.synchronize(this);
    }

    /** {@inheritDoc} */
    @Override
    public Promise<ResourceDelta[]> synchronize(ResourceDelta...deltas) {
        checkState(getLocation().isRoot(), "External deltas should be applied on the workspace root");

        return resourceManager.synchronize(deltas);
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Resource[]> search(String fileMask, String contentMask) {
        return resourceManager.search(this, fileMask, contentMask);
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Resource[]> getTree(int depth) {
        return resourceManager.getRemoteResources(this, depth, true);
    }

    @Override
    public Promise<SourceEstimation> estimate(String projectType) {
        return resourceManager.estimate(this, projectType);
    }
}
