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
package org.eclipse.che.api.user.shared.dto;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.user.Profile;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.dto.shared.DTO;

/**
 * This object used for transporting profile data to/from client.
 *
 * @author Yevhenii Voevodin
 * @see Profile
 * @see DtoFactory
 */
@DTO
public interface ProfileDto extends Profile {

  void setUserId(String id);

  @ApiModelProperty("Profile ID")
  String getUserId();

  ProfileDto withUserId(String id);

  @ApiModelProperty("Profile attributes")
  Map<String, String> getAttributes();

  void setAttributes(Map<String, String> attributes);

  ProfileDto withAttributes(Map<String, String> attributes);

  List<Link> getLinks();

  void setLinks(List<Link> links);

  ProfileDto withLinks(List<Link> links);

  String getEmail();

  ProfileDto withEmail(String email);
}
