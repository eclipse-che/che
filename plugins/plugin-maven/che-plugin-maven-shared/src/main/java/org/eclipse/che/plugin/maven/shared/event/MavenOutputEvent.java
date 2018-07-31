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

/** Base maven output event. */
public interface MavenOutputEvent {
  /** Returns typ of the event. */
  TYPE getType();

  enum TYPE {
    START_STOP,
    PERCENT,
    PERCENT_UNDEFINED,
    UPDATE,
    TEXT
  }
}
