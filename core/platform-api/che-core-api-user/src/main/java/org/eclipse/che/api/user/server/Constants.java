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
package org.eclipse.che.api.user.server;

/**
 * Constants for User API and UserProfile API
 *
 * @author Eugene Voevodin
 * @author Max Shaposhnik
 */
public final class Constants {

    public static final String LINK_REL_GET_CURRENT_USER_PROFILE    = "current user profile";
    public static final String LINK_REL_UPDATE_CURRENT_USER_PROFILE = "update current user profile";
    public static final String LINK_REL_GET_USER_PROFILE_BY_ID      = "user profile by id";
    public static final String LINK_REL_UPDATE_USER_PROFILE_BY_ID   = "update user profile by id";
    public static final String LINK_REL_INROLE                      = "in role";
    public static final String LINK_REL_CREATE_USER                 = "create user";
    public static final String LINK_REL_GET_CURRENT_USER            = "get current";
    public static final String LINK_REL_UPDATE_PASSWORD             = "update password";
    public static final String LINK_REL_REMOVE_PREFERENCES          = "remove preferences";
    public static final String LINK_REL_REMOVE_ATTRIBUTES           = "remove attributes";
    public static final String LINK_REL_GET_USER_BY_ID              = "get user by id";
    public static final String LINK_REL_GET_USER_BY_EMAIL           = "get user by email";
    public static final String LINK_REL_REMOVE_USER_BY_ID           = "remove user by id";
    public static final String LINK_REL_UPDATE_PREFERENCES          = "update prefs";
    public static final int    ID_LENGTH                            = 16;
    public static final int    PASSWORD_LENGTH                      = 10;

    private Constants() {
    }
}
