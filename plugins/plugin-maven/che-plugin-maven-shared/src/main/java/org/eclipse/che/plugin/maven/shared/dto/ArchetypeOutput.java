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
