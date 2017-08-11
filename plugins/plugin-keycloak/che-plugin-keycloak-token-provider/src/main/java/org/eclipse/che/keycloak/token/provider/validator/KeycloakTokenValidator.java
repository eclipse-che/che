/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.keycloak.token.provider.validator;

import org.eclipse.che.keycloak.token.provider.exception.KeycloakException;

import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;

@Singleton
public class KeycloakTokenValidator {
    private static final String BEARER_PREFIX = "Bearer ";

    public void validate(final String keycloakToken) throws KeycloakException {
        if (!isValid(keycloakToken)) {
            throw new KeycloakException("Keycloak token must have '" + BEARER_PREFIX + "' prefix");
        }
    }

    private boolean isValid(final String keycloakToken) {
        return (StringUtils.isNotBlank(keycloakToken) && keycloakToken.startsWith(BEARER_PREFIX));
    }

}
