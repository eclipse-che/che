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

import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.factory.Ide;
import org.eclipse.che.dto.shared.DTO;

/**
 * Describe IDE interface Look and Feel
 *
 * @author Sergii Kabashniuk
 */
@DTO
public interface IdeDto extends Ide {

  /** @return configuration of IDE on application loaded event. */
  @Override
  @FactoryParameter(obligation = OPTIONAL)
  OnAppLoadedDto getOnAppLoaded();

  void setOnAppLoaded(OnAppLoadedDto onAppLoaded);

  IdeDto withOnAppLoaded(OnAppLoadedDto onAppLoaded);

  /** @return configuration of IDE on application closed event. */
  @Override
  @FactoryParameter(obligation = OPTIONAL)
  OnAppClosedDto getOnAppClosed();

  void setOnAppClosed(OnAppClosedDto onAppClosed);

  IdeDto withOnAppClosed(OnAppClosedDto onAppClosed);

  /** @return configuration of IDE on projects loaded event. */
  @Override
  @FactoryParameter(obligation = OPTIONAL)
  OnProjectsLoadedDto getOnProjectsLoaded();

  void setOnProjectsLoaded(OnProjectsLoadedDto onProjectsLoaded);

  IdeDto withOnProjectsLoaded(OnProjectsLoadedDto onProjectsLoaded);
}
