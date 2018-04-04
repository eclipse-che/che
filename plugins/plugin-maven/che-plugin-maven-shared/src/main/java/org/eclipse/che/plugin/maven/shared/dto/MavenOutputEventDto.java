/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
