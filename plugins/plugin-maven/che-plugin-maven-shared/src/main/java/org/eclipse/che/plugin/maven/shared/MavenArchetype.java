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
package org.eclipse.che.plugin.maven.shared;

import java.util.Map;

/** @author Vitalii Parfonov */
public interface MavenArchetype {

  /** Returns the archetype's groupId. */
  String getGroupId();

  /** Returns the archetype's artifactId. */
  String getArtifactId();

  /** Returns the archetype's version. */
  String getVersion();

  /** Returns the repository where to find the archetype. */
  String getRepository();

  /** Returns the additional properties for the archetype. */
  Map<String, String> getProperties();
}
