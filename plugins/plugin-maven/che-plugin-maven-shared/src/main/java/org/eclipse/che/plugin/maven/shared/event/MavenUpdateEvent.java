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
package org.eclipse.che.plugin.maven.shared.event;

import java.util.List;

/** Event that describes Maven notification output. */
public interface MavenUpdateEvent extends MavenOutputEvent {
  /** Returns list of projects which were modified. */
  List<String> getUpdatedProjects();

  /** Returns list of projects which were removed. */
  List<String> getRemovedProjects();
}
