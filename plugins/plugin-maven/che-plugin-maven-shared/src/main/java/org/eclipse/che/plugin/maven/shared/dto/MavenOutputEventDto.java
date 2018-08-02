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
package org.eclipse.che.plugin.maven.shared.dto;

import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.plugin.maven.shared.event.MavenOutputEvent;

/** DTO object which describes base maven output event. */
@DTO
public interface MavenOutputEventDto extends MavenOutputEvent {

  TYPE getType();

  /** Returns a type of the output event. */
  void setType(TYPE type);

  MavenOutputEventDto withType(TYPE type);
}
