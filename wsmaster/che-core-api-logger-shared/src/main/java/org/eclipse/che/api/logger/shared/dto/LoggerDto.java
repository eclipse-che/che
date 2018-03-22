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
