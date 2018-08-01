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
package org.eclipse.che.security.oauth.shared.dto;

import java.util.List;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.shared.DTO;

/** @author Max Shaposhnik (mshaposhnik@codenvy.com) */
@DTO
public interface OAuthAuthenticatorDescriptor {

  String getName();

  void setName(String name);

  OAuthAuthenticatorDescriptor withName(String name);

  List<Link> getLinks();

  void setLinks(List<Link> links);

  OAuthAuthenticatorDescriptor withLinks(List<Link> links);
}
