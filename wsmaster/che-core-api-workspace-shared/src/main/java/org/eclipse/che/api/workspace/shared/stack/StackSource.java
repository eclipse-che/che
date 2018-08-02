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
package org.eclipse.che.api.workspace.shared.stack;

/**
 * Defines the interface that describes the stack source. It is a part of the {@link Stack}
 *
 * <p>Example: "source" : { "type":"image", "origin":"codenvy/ubuntu_jdk8" }
 *
 * @author Alexander Andrienko
 */
public interface StackSource {

  /**
   * Returns type for the StackSource.
   *
   * <p>There are three available types of the StackSource:
   *
   * <ul>
   *   <li>"recipe"
   *   <li>"image"
   *   <li>"location"
   * </ul>
   */
  String getType();

  /**
   * Returns origin data for the Stack Source. Origin data can be:
   *
   * <ul>
   *   <li>text/plain - when the type is "recipe" (e.g. "FROM codenvy/ubuntu_jdk8"})
   *   <li>image tag - when the type is "image" (e.g. "codenvy/ubuntu_jdk8")
   *   <li>url - when the type is "location" (e.g.
   *       "https://raw.githubusercontent.com/codenvy/dockerfiles/master/php/debian/Dockerfile")
   * </ul>
   */
  String getOrigin();
}
