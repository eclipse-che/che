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
import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.resource.Path;

import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

/**
 * Default implementation of the {@code Project}.
 *
 * @author Vlad Zhukovskyi
 * @see ContainerImpl
 * @see Project
 * @since 4.4.0
 */
@Beta
class ProjectImpl extends ContainerImpl implements Project {

    private static final int FOLDER_NOT_EXISTS_ON_FS = 10;

    @Inject
    protected ProjectImpl(@Assisted ProjectConfig reference,
                          @Assisted ResourceManager resourceManager,
                          PromiseProvider promiseProvider) {
        super(Path.valueOf(reference.getPath()), resourceManager, promiseProvider);
    }

    /** {@inheritDoc} */
    @Override
    public final int getResourceType() {
        return PROJECT;
    }

    /** {@inheritDoc} */
    @Override
    public String getPath() {
        return getLocation().toString();
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return getProjectConfig().getDescription();
    }

    /** {@inheritDoc} */
    @Override
    public String getType() {
        return getProjectConfig().getType();
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getMixins() {
        return unmodifiableList(getProjectConfig().getMixins());
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, List<String>> getAttributes() {
        return unmodifiableMap(getProjectConfig().getAttributes());
    }

    /** {@inheritDoc} */
    @Override
    public SourceStorage getSource() {
        return getProjectConfig().getSource();
    }

    /** {@inheritDoc} */
    @Override
    public ProjectRequest update() {
        return new ProjectRequest() {
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

    /** {@inheritDoc} */
    @Override
    public boolean isProblem() {
        return resourceManager.calculateProblemMarker(this) != null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean exists() {
        final ProblemProjectMarker problemProjectMarker = resourceManager.calculateProblemMarker(this);

        return problemProjectMarker == null || !problemProjectMarker.getProblems().containsKey(FOLDER_NOT_EXISTS_ON_FS);
    }

    /** {@inheritDoc} */
    @Override
    public Promise<List<SourceEstimation>> resolve() {
        return resourceManager.resolve(this);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isTypeOf(String type) {
        return getType().equals(type); //TODO implement better mechanism for type detection
    }

    /** {@inheritDoc} */
    @Override
    public String getAttribute(String key) {
        final Map<String, List<String>> attributes = getAttributes();

        if (attributes.containsKey(key)) {
            final List<String> values = attributes.get(key);

            return values.get(0);
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getAttributes(String key) {
        final Map<String, List<String>> attributes = getAttributes();

        if (attributes.containsKey(key)) {
            return attributes.get(key);
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("path", getLocation())
                          .add("resource", getResourceType())
                          .add("type", getProjectConfig().getType())
                          .add("description", getProjectConfig().getDescription())
                          .add("mixins", getProjectConfig().getMixins())
                          .add("attributes", getProjectConfig().getAttributes())
                          .toString();
    }

    private ProjectConfig getProjectConfig() {
        return resourceManager.getProjectConfig(path);
    }
}
