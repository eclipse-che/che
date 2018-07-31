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
package org.eclipse.che.plugin.maven.shared.event;

/** Event that describes Maven start notification. */
public interface MavenStartStopEvent extends MavenOutputEvent {
  /**
   * Returns {@code true} if the process of resolving is started otherwise returns {@code false}.
   */
  boolean isStart();
}
