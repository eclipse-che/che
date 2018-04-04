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

/**
 * Provide output of maven archetype project generation
 *
 * @author Vitalii Parfonov
 */
@DTO
public interface ArchetypeOutput {

  enum State {
    START,
    IN_PROGRESS,
    DONE,
    ERROR
  }

  /**
   * Output line
   *
   * @return
   */
  String getOutput();

  /**
   * Before start will be State.START During generation - State.IN_PROGRESS After generation will be
   * State.DONE In case error - State.ERROR
   *
   * @return
   */
  State getState();

  void setOutput(String output);

  void setState(State state);
}
