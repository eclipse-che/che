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
package org.eclipse.che.api.core.rest.shared.dto;

import java.util.List;
import org.eclipse.che.api.core.rest.shared.Links;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.dto.shared.DelegateRule;
import org.eclipse.che.dto.shared.DelegateTo;

/** @author andrew00x */
@DTO
public interface Hyperlinks {
  List<Link> getLinks();

  Hyperlinks withLinks(List<Link> links);

  void setLinks(List<Link> links);

  @DelegateTo(
    client = @DelegateRule(type = Links.class, method = "getLinks"),
    server = @DelegateRule(type = Links.class, method = "getLinks")
  )
  List<Link> getLinks(String rel);

  @DelegateTo(
    client = @DelegateRule(type = Links.class, method = "getLink"),
    server = @DelegateRule(type = Links.class, method = "getLink")
  )
  Link getLink(String rel);
}
