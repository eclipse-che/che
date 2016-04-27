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
package org.eclipse.che.commons.user;

/** @author andrew00x */
public interface User {
    /** Unidentified user */
    User ANONYMOUS = new User() {
        @Override
        public String getName() {
            return "Anonymous";
        }

        @Override
        public boolean isMemberOf(String role) {
            return false;
        }

        @Override
        public boolean hasPermission(String domain, String instance, String action) {
            return false;
        }

        @Override
        public String getToken() {
            return null;
        }

        @Override
        public String getId() {
            return "0000-00-0000";
        }

        @Override
        public boolean isTemporary() {
            return false;
        }
    };

    /** Get user name. */
    String getName();

    /**
     * Check is user in specified {@code role}.
     *
     * @param role
     *         role name to check
     * @return {@code true} if user in role and {@code false} otherwise
     */
    boolean isMemberOf(String role);

    /**
     * Check does user have specified permission.
     *
     * @return {@code true} if user has permission to perform given action and {@code false} otherwise
     */
    boolean hasPermission(String domain, String instance, String action);

    /**
     * Get user auth token to be able to execute request as user
     *
     * @return - user token
     */
    String getToken();

    /**
     * Get user unique identifier of user.
     * In comparison with name id never changes for the given user.
     *
     * @return - unique identifier of user.
     */
    String getId();

    /**
     * @return - true if user is temporary, false if this is a real persistent user.
     */
    boolean isTemporary();
}
