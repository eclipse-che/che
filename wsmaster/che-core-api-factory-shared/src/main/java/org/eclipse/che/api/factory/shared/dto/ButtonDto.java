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
package org.eclipse.che.api.factory.shared.dto;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.factory.Button;
import org.eclipse.che.dto.shared.DTO;

/** @author Alexander Garagatyi */
@DTO
public interface ButtonDto extends Button {

  /** Type of the button */
  @Override
  @FactoryParameter(obligation = OPTIONAL)
  Type getType();

  void setType(Type type);

  ButtonDto withType(Type type);

  /** Button attributes */
  @Override
  @FactoryParameter(obligation = OPTIONAL)
  ButtonAttributesDto getAttributes();

  void setAttributes(ButtonAttributesDto attributes);

  ButtonDto withAttributes(ButtonAttributesDto attributes);
}
