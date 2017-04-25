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
package org.eclipse.che.api.auth.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * OAuth token.
 *
 * @author Sergii Kabashniuk
 */
@DTO
public interface OAuthToken {
    /** Get OAuth token */
    String getToken();

    /** Set OAuth token */
    void setToken(String token);

    OAuthToken withToken(String token);

    /** Get OAuth scope */
    String getScope();

    /** Set OAuth scope */
    void setScope(String scope);

    OAuthToken withScope(String scope);
}
