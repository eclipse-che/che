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
package org.eclipse.che.api.logger.shared.dto;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface LoggerDto {

  public String getName();

  public void setName(String name);

  public LoggerDto withName(String name);

  public String getLevel();

  public void setLevel(String level);

  public LoggerDto withLevel(String level);
}
