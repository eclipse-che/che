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

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/**
 * Test result trace DTO.
 *
 * @author Bartlomiej Laczkowski
 */
@DTO
public interface TestResultTraceDto {

  /**
   * Returns related message (i.e. error/exception description).
   *
   * @return related message
   */
  String getMessage();

  /**
   * Sets related message (i.e. error/exception description).
   *
   * @param message
   */
  void setMessage(String message);

  /**
   * Returns list of stack frames for this trace.
   *
   * @return list of stack frames
   */
  List<TestResultTraceFrameDto> getTraceFrames();

  /**
   * Sets list of stack frames for this trace.
   *
   * @param traceFrames
   */
  void setTraceFrames(List<TestResultTraceFrameDto> traceFrames);
}
