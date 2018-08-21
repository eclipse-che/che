/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.api.testing.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * Single trace frame DTO.
 *
 * @author Bartlomiej Laczkowski
 */
@DTO
public interface TestResultTraceFrameDto {

  /**
   * Returns trace frame description/name.
   *
   * @return trace frame description/name
   */
  String getTraceFrame();

  /**
   * Sets trace frame description/name.
   *
   * @param traceFrame
   */
  void setTraceFrame(String traceFrame);

  /**
   * Returns trace frame simple location DTO (i.e. file with related method/function definition).
   *
   * @return trace frame simple location DTO
   */
  SimpleLocationDto getLocation();

  /**
   * Sets trace frame simple location DTO.
   *
   * @param location
   */
  void setLocation(SimpleLocationDto location);
}
