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
package org.eclipse.che.multiuser.resource.shared.dto;

import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.multiuser.resource.model.Resource;

/** @author Sergii Leschenko */
@DTO
public interface ResourceDto extends Resource {
  @Override
  String getType();

  void setType(String type);

  ResourceDto withType(String type);

  @Override
  long getAmount();

  void setAmount(long amount);

  ResourceDto withAmount(long amount);

  @Override
  String getUnit();

  void setUnit(String unit);

  ResourceDto withUnit(String unit);
}
