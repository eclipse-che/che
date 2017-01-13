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
package org.eclipse.che.api.core.model.user;

import java.util.Map;

/**
 * Defines the user's profile model.
 *
 * <p>User's profile describes an additional user information such as his
 * job title or company name which is not related to application business logic.
 * If it is necessary to manage business logic related attributes then
 * user's preferences should be used instead.
 *
 * @author Yevhenii Voevodin
 * @see User
 */
public interface Profile {

    /**
     * Returns the identifier of the user {@link User#getId()}
     * whom this profile belongs to.
     */
    String getUserId();

    /**
     * Returns the user profile attributes (e.g. job title).
     */
    Map<String, String> getAttributes();
}
