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
package org.eclipse.che.api.factory.shared.dto;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

import java.util.List;
import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.factory.OnAppLoaded;
import org.eclipse.che.dto.shared.DTO;

/**
 * Describe IDE look and feel on application loaded event.
 *
 * @author Sergii Kabashniuk
 */
@DTO
public interface OnAppLoadedDto extends OnAppLoaded {

  /** @return actions for current event. */
  @Override
  @FactoryParameter(obligation = OPTIONAL)
  List<IdeActionDto> getActions();

  void setActions(List<IdeActionDto> actions);

  OnAppLoadedDto withActions(List<IdeActionDto> actions);
}
