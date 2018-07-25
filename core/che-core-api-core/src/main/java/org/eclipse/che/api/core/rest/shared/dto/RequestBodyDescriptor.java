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
package org.eclipse.che.api.core.rest.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * Describes body of the request.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 */
@DTO
public interface RequestBodyDescriptor {
  /**
   * Get optional description of request body.
   *
   * @return optional description of request body
   */
  String getDescription();

  RequestBodyDescriptor withDescription(String description);

  /**
   * Set optional description of request body.
   *
   * @param description optional description of request body
   */
  void setDescription(String description);
}
