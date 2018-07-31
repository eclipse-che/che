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
package org.eclipse.che.ide.api.resources;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.marker.Marker;

/**
 * An object that represents client side project.
 *
 * <p>Features of projects include:
 *
 * <ul>
 *   <li>A project collects together a set of files and folders.
 *   <li>A project's location controls where the project's resources are stored in the local file
 *       system.
 * </ul>
 *
 * Project also extends {@link ProjectConfig} which contains the meta-data required to define a
 * project.
 *
 * <p>To get list of currently of all loaded projects in the IDE, use {@link
 * AppContext#getProjects()}
 *
 * <p>Note. This interface is not intended to be implemented by clients.
 *
 * @author Vlad Zhukovskyi
 * @see AppContext#getProjects()
 * @since 4.4.0
 */
@Beta
public interface Project extends Container, ProjectConfig {

  /**
   * Changes this project resource to match the given configuration provided by the request. Request
   * contains all necessary data for manipulating with the project.
   *
   * <p>Example of usage:
   *
   * <pre>
   *     ProjectConfig config = ... ;
   *     Project project = ... ;
   *
   *     Project.ProjectRequest updateRequest = project.update()
   *                                                   .withBody(config);
   *
   *     Promise<Project> updatedProject = updateRequest.send();
   * </pre>
   *
   * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
   * {@link ResourceDelta#UPDATED}. Updated resource provided by {@link ResourceDelta#getResource()}
   *
   * <p>Note. Calling this method doesn't update the project immediately. To complete request method
   * {@link ProjectRequest#send()} should be called.
   *
   * @return the request to update the project
   * @see ProjectRequest
   * @see ProjectRequest#send()
   * @since 4.4.0
   */
  ProjectRequest update();

  /**
   * Check whether current project has problems. Problem project calculates in a runtime, so it is
   * not affects stored configuration on the server. To find out the reasons why project has
   * problems, following code snippet may be helpful:
   *
   * <p>Example of usage:
   *
   * <pre>
   *     Project project = ... ;
   *     if (project.isProblem()) {
   *         Marker problemMarker = getMarker(ProblemProjectMarker.PROBLEM_PROJECT).get();
   *
   *         String message = String.valueOf(problemMarker.getAttribute(Marker.MESSAGE));
   *     }
   * </pre>
   *
   * @return {@code true} if current project has problems, otherwise {@code false}
   * @see ProblemProjectMarker
   * @since 4.4.0
   */
  boolean isProblem();

  /**
   * Returns the {@code true} if project physically exists on the file system.
   *
   * <p>Project may not be exists on file system, but workspace may has configured in the current
   * workspace.
   *
   * @return {@code true} if project physically exists on the file system, otherwise {@code false}
   * @since 4.4.0
   */
  boolean exists();

  /**
   * Resolve possible project types for current {@link Project}.
   *
   * <p>These source estimations may be useful for automatically project type detection.
   *
   * <p>Source estimation provides possible project type and attributes that this project type can
   * provide. Based on this information, current project may be configured in correct way.
   *
   * @return the {@link Promise} with source estimations
   * @since 4.4.0
   */
  Promise<List<SourceEstimation>> resolve();

  /**
   * Checks whether given project {@code type} is applicable to current project.
   *
   * @param type the project type to check
   * @return true if given project type is applicable to current project
   * @since 4.4.0
   */
  boolean isTypeOf(String type);

  /**
   * Returns the attribute value for given {@code key}. If such attribute doesn't exist, {@code
   * null} is returned. If there is more than one value exists for given {@code key}, than first
   * value is returned.
   *
   * @param key the attribute name
   * @return first value for the given {@code key} or null if such attribute doesn't exist
   * @since 4.4.0
   */
  String getAttribute(String key);

  /**
   * Returns the list of attributes for given {@code key}. If such attribute doesn't exist, {@code
   * null} is returned.
   *
   * @param key the attribute name
   * @return the list with values for the given {@code key} or null if such attribute doesn't exist
   * @since 4.4.0
   */
  List<String> getAttributes(String key);

  /**
   * Marker that describe problematic project.
   *
   * @see #isProblem()
   * @since 4.4.0
   */
  @Beta
  class ProblemProjectMarker implements Marker {

    private Map<Integer, String> problems;

    /**
     * Marker type, which should be used when marker requests.
     *
     * @see Resource#getMarker(String)
     */
    public static final String PROBLEM_PROJECT = "problemProjectMarker";

    public ProblemProjectMarker(Map<Integer, String> problems) {
      this.problems = MoreObjects.firstNonNull(problems, Collections.<Integer, String>emptyMap());
    }

    /** {@inheritDoc} */
    @Override
    public String getType() {
      return PROBLEM_PROJECT;
    }

    public Map<Integer, String> getProblems() {
      return problems;
    }
  }

  /**
   * Base interface for project update operation.
   *
   * @see Project#update()
   * @since 4.4.0
   */
  @Beta
  interface ProjectRequest extends Resource.Request<Project, ProjectConfig> {
    @Override
    Request<Project, ProjectConfig> withBody(ProjectConfig object);

    @Override
    ProjectConfig getBody();
  }
}
