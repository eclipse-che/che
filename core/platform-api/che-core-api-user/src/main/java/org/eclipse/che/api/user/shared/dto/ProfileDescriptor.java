/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.user.shared.dto;

import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.shared.DTO;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

/**
 * @author andrew00x
 */
@DTO
public interface ProfileDescriptor {

    void setId(String id);

    @ApiModelProperty("Profile ID")
    String getId();

    ProfileDescriptor withId(String id);

    @ApiModelProperty("User ID")
    String getUserId();

    void setUserId(String id);

    ProfileDescriptor withUserId(String id);

    @ApiModelProperty("Profile attributes")
    Map<String, String> getAttributes();

    void setAttributes(Map<String, String> attributes);

    ProfileDescriptor withAttributes(Map<String, String> attributes);

    List<Link> getLinks();

    void setLinks(List<Link> links);

    ProfileDescriptor withLinks(List<Link> links);
}
