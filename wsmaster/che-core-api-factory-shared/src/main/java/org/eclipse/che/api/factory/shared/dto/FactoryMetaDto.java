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
import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

import java.util.List;
import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;

/** Ancestor for Factory DTOs that does not know about devfile version it will hold. */
public interface FactoryMetaDto extends Hyperlinks {

  /**
   * Gives an option to update the factory based on devfile version. See {@link FactoryVisitor}.
   *
   * @param visitor visitor that should update the factory
   * @return updated factory
   */
  FactoryMetaDto acceptVisitor(FactoryVisitor visitor);

  @FactoryParameter(obligation = MANDATORY)
  String getV();

  void setV(String v);

  FactoryMetaDto withV(String v);

  /**
   * Indicates filename in repository from which the factory was created (for example, .devfile) or
   * just contains 'repo' value if factory was created from bare GitHub repository. For custom raw
   * URL's (pastebin, gist etc) value is {@code null}
   */
  @FactoryParameter(obligation = OPTIONAL, setByServer = true)
  String getSource();

  void setSource(String source);

  FactoryMetaDto withSource(String source);

  @FactoryParameter(obligation = OPTIONAL)
  String getName();

  void setName(String name);

  FactoryMetaDto withName(String name);

  @FactoryParameter(obligation = OPTIONAL, setByServer = true)
  String getId();

  void setId(String id);

  FactoryMetaDto withId(String id);

  @FactoryParameter(obligation = OPTIONAL)
  AuthorDto getCreator();

  void setCreator(AuthorDto creator);

  FactoryMetaDto withCreator(AuthorDto creator);

  @Override
  FactoryMetaDto withLinks(List<Link> links);

  @FactoryParameter(obligation = OPTIONAL, trackedOnly = true)
  PoliciesDto getPolicies();

  void setPolicies(PoliciesDto policies);

  FactoryMetaDto withPolicies(PoliciesDto policies);

  @FactoryParameter(obligation = OPTIONAL)
  IdeDto getIde();

  void setIde(IdeDto ide);

  FactoryMetaDto withIde(IdeDto ide);
}
