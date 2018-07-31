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
import org.eclipse.che.ide.api.resources.Project.ProjectRequest;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.NameUtils;

/**
 * Folders may be leaf or non-leaf resources and may contain files and/or other folders. A folder
 * resource is stored as a directory in the local file system.
 *
 * <p>Folder instance can be obtained by calling {@link Container#getContainer(Path)} or by {@link
 * Container#getChildren(boolean)}.
 *
 * <p>Note. This interface is not intended to be implemented by clients.
 *
 * @author Vlad Zhukovskyi
 * @since 4.4.0
 */
@Beta
public interface Folder extends Container {

  /**
   * Transforms current folder into {@link Project}.
   *
   * <p>Calling current method doesn't create configuration immediately. To complete configuration
   * creating method {@link ProjectRequest#send()} should be called. This is immutable operation
   * which produce new {@link Project}.
   *
   * <p>Example of usage:
   *
   * <pre>
   *     Folder folder = ... ;
   *     ProjectConfig configuration = ... ;
   *
   *     Promise<Project> projectPromise = folder.toProject().withBody(configuration).send();
   *
   *     projectPromise.then(new Operation<Project>() {
   *         public void apply(Project newProject) throws OperationException {
   *              //do something with new project
   *         }
   *     });
   * </pre>
   *
   * <p>Fires {@link ResourceChangedEvent} with the following {@link ResourceDelta}: Delta kind:
   * {@link ResourceDelta#UPDATED}. Updated resource (instance of {@link Project}) provided by
   * {@link ResourceDelta#getResource()}
   *
   * @return the create project request
   * @throws IllegalArgumentException if arguments is not a valid. Reasons include:
   *     <ul>
   *       <li>Invalid project name
   *       <li>Invalid project type
   *     </ul>
   *
   * @throws IllegalStateException if creation was failed. Reasons include:
   *     <ul>
   *       <li>Resource already exists
   *     </ul>
   *
   * @see NameUtils#checkProjectName(String)
   * @see ProjectRequest
   * @see ProjectRequest#send()
   * @since @since 4.4.0
   */
  ProjectRequest toProject();
}
