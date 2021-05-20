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

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

import java.util.List;
import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.factory.Factory;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;
import org.eclipse.che.dto.shared.DTO;

/**
 * Factory of version 4.0.
 *
 * <p>This 'implementation' of {@link FactoryMetaDto} is used for Devfile v1.
 *
 * @author Max Shaposhnik
 */
@DTO
public interface FactoryDto extends FactoryMetaDto, Factory, Hyperlinks {

  @Override
  default FactoryMetaDto acceptVisitor(FactoryVisitor visitor) {
    return visitor.visit(this);
  }

  @FactoryParameter(obligation = OPTIONAL)
  DevfileDto getDevfile();

  void setDevfile(DevfileDto devfileDto);

  FactoryDto withDevfile(DevfileDto devfileDto);

  FactoryDto withV(String v);

  @Override
  FactoryDto withName(String name);

  @Override
  FactoryDto withPolicies(PoliciesDto policies);

  @Override
  FactoryDto withIde(IdeDto ide);

  @Override
  FactoryDto withId(String id);

  @Override
  FactoryDto withSource(String source);

  @Override
  FactoryDto withCreator(AuthorDto creator);

  @Override
  FactoryDto withLinks(List<Link> links);

  /** because factory DTO may have devfile, in that case, workspace may be optional */
  @Override
  @FactoryParameter(obligation = OPTIONAL)
  WorkspaceConfigDto getWorkspace();

  void setWorkspace(WorkspaceConfigDto workspace);

  FactoryDto withWorkspace(WorkspaceConfigDto workspace);
}
