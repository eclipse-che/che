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
package org.eclipse.che.api.core.rest.shared.dto;

import java.util.Map;
import org.eclipse.che.dto.shared.DTO;

/**
 * Extended error which contains error code and optional attributes.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@DTO
public interface ExtendedError extends ServiceError {

  /**
   * Get error code.
   *
   * @return error code
   */
  int getErrorCode();

  /**
   * Set error code.
   *
   * @param errorCode error code
   */
  void setErrorCode(int errorCode);

  ExtendedError withErrorCode(int errorCode);

  /**
   * Get error attributes.
   *
   * @return attributes of the error if any
   */
  Map<String, String> getAttributes();

  /**
   * Set error attributes.
   *
   * @param attributes error attributes
   */
  void setAttributes(Map<String, String> attributes);

  ExtendedError withAttributes(Map<String, String> attributes);

  @Override
  ExtendedError withMessage(String message);
}
