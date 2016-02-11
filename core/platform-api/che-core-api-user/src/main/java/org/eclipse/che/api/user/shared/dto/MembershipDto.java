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

import org.eclipse.che.api.user.shared.model.Membership;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author andrew00x
 */
@DTO
public interface MembershipDto extends Membership {

    @Override
    String getUserId();

    void setUserId(String id);

    MembershipDto withUserId(String id);

    @Override
    String getScope();

    @Override
    String getUserName();

    @Override
    String getSubjectId();

    @Override
    Map<String, String> getSubjectProperties();

    @Override
    List<String> getRoles();

    void setRoles(List<String> roles);

    MembershipDto withRoles(List<String> roles);
}
