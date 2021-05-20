/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.MANDATORY;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.shared.DTO;

/**
 * Factory DTO for Devfile v2. As che-server don't know the structure of Devfile v2, we're using
 * just generic {@code Map<String, Object>} here.
 */
@DTO
public interface FactoryDevfileV2Dto extends FactoryMetaDto, Hyperlinks {

  @Override
  default FactoryMetaDto acceptVisitor(FactoryVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  FactoryDevfileV2Dto withV(String v);

  @FactoryParameter(obligation = MANDATORY)
  Map<String, Object> getDevfile();

  void setDevfile(Map<String, Object> devfile);

  FactoryDevfileV2Dto withDevfile(Map<String, Object> devfile);

  @Override
  FactoryDevfileV2Dto withSource(String source);

  @Override
  FactoryDevfileV2Dto withLinks(List<Link> links);
}
