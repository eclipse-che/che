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
package org.eclipse.che.api.account.shared.dto;

import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.shared.DTO;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author andrew00x
 */
@DTO
public interface MemberDescriptor {
    @ApiModelProperty(value = "User roles", allowableValues = "account/owner,account/member")
    List<String> getRoles();

    void setRoles(List<String> roles);

    MemberDescriptor withRoles(List<String> roles);

    @ApiModelProperty(value = "User ID")
    String getUserId();

    void setUserId(String id);

    MemberDescriptor withUserId(String id);

    AccountReference getAccountReference();

    void setAccountReference(AccountReference accountReference);

    MemberDescriptor withAccountReference(AccountReference accountReference);

    List<Link> getLinks();

    void setLinks(List<Link> links);

    MemberDescriptor withLinks(List<Link> links);
}
