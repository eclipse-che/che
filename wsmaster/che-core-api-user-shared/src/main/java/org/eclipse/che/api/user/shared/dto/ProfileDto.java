/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.user.shared.dto;

import org.eclipse.che.api.core.model.user.Profile;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.dto.shared.DTO;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

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
