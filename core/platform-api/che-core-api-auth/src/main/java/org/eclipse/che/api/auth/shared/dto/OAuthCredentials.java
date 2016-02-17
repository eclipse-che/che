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
package org.eclipse.che.api.auth.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * OAuth 1.0 credentials.
 *
 * @author Sergii Kabashniuk
 */
@DTO
public interface OAuthCredentials {
    /** Get OAuth token */
    String getToken();

    /** Set OAuth token */
    void setToken(String token);

    OAuthCredentials withToken(String token);

    /** Get OAuth scope */
    String getTokenSecret();

    /** Set OAuth scope */
    void setTokenSecret(String scope);

    OAuthCredentials withTokenSecret(String scope);

    /** Get OAuth scope */
    boolean isCallbackConfirmed();

    /** Set OAuth scope */
    void setCallbackConfirmed(boolean scope);

    OAuthCredentials withCallbackConfirmed(boolean scope);
}