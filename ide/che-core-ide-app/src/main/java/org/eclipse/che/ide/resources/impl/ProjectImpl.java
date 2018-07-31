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
package org.eclipse.che.ide.resources.impl;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.model.project.ProjectProblem;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.config.SourceStorage;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.workspace.shared.ProjectProblemImpl;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.marker.Marker;
import org.eclipse.che.ide.resource.Path;

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

  private final ProjectConfig reference;
  private final List<ProjectProblem> problems;

  @Inject
  protected ProjectImpl(
      @Assisted ProjectConfig reference,
      @Assisted ResourceManager resourceManager,
      PromiseProvider promiseProvider) {
    super(Path.valueOf(reference.getPath()), resourceManager, promiseProvider);

    this.reference = reference;
    if (reference.getProblems() != null) {
      problems =
          reference
              .getProblems()
              .stream()
              .map(problem -> new ProjectProblemImpl(problem.getCode(), problem.getMessage()))
              .collect(Collectors.toList());
    } else {
      problems = Collections.emptyList();
    }
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
    return reference.getDescription();
  }

  /** {@inheritDoc} */
  @Override
  public String getType() {
    return reference.getType();
  }

  /** {@inheritDoc} */
  @Override
  public List<String> getMixins() {
    return unmodifiableList(reference.getMixins());
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, List<String>> getAttributes() {
    return unmodifiableMap(reference.getAttributes());
  }

  /** {@inheritDoc} */
  @Override
  public SourceStorage getSource() {
    return reference.getSource();
  }

  @Override
  public List<ProjectProblem> getProblems() {
    return problems;
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
    return reference.getProblems() != null && !reference.getProblems().isEmpty();
  }

  /** {@inheritDoc} */
  @Override
  public boolean exists() {
    final Optional<Marker> problemMarker = getMarker(ProblemProjectMarker.PROBLEM_PROJECT);

    return !problemMarker.isPresent()
        || !((ProblemProjectMarker) problemMarker.get())
            .getProblems()
            .containsKey(FOLDER_NOT_EXISTS_ON_FS);
  }

  /** {@inheritDoc} */
  @Override
  public Promise<List<SourceEstimation>> resolve() {
    return resourceManager.resolve(this);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isTypeOf(String type) {
    return getType().equals(type); // TODO implement better mechanism for type detection
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
        .add("type", reference.getType())
        .add("description", reference.getDescription())
        .add("mixins", reference.getMixins())
        .add("attributes", reference.getAttributes())
        .toString();
  }
}
