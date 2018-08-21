/*
 * Copyright (c) 2016-2017 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.composer.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * Provide output of Composer dependencies installation.
 *
 * @author Kaloyan Raev
 */
@DTO
public interface ComposerOutput {

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
