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
import org.eclipse.che.api.core.model.factory.Policies;
import org.eclipse.che.dto.shared.DTO;

/**
 * Describe restrictions of the factory
 *
 * @author andrew00x
 * @author Alexander Garagatyi
 */
@DTO
public interface PoliciesDto extends Policies {
  /** Restrict access if referer header doesn't match this field */
  // Do not change referer to referrer
  @Override
  @FactoryParameter(obligation = OPTIONAL)
  String getReferer();

  void setReferer(String referer);

  PoliciesDto withReferer(String referer);

  /** Restrict access for factories used earlier then author supposes */
  @Override
  @FactoryParameter(obligation = OPTIONAL)
  Long getSince();

  void setSince(Long since);

  PoliciesDto withSince(Long since);

  /** Restrict access for factories used later then author supposes */
  @Override
  @FactoryParameter(obligation = OPTIONAL)
  Long getUntil();

  void setUntil(Long until);

  PoliciesDto withUntil(Long until);

  /** Workspace creation strategy */
  @Override
  @FactoryParameter(obligation = OPTIONAL)
  String getCreate();

  void setCreate(String create);

  PoliciesDto withCreate(String create);
}
