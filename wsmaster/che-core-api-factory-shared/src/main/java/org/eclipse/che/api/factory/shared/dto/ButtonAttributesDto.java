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
package org.eclipse.che.api.factory.shared.dto;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.factory.ButtonAttributes;
import org.eclipse.che.dto.shared.DTO;

/** @author Alexander Garagatyi */
@DTO
public interface ButtonAttributesDto extends ButtonAttributes {

  @Override
  @FactoryParameter(obligation = OPTIONAL)
  String getColor();

  void setColor(String color);

  ButtonAttributesDto withColor(String color);

  @Override
  @FactoryParameter(obligation = OPTIONAL)
  Boolean getCounter();

  void setCounter(Boolean counter);

  ButtonAttributesDto withCounter(Boolean counter);

  @Override
  @FactoryParameter(obligation = OPTIONAL)
  String getLogo();

  void setLogo(String logo);

  ButtonAttributesDto withLogo(String logo);

  @Override
  @FactoryParameter(obligation = OPTIONAL)
  String getStyle();

  void setStyle(String style);

  ButtonAttributesDto withStyle(String style);
}
