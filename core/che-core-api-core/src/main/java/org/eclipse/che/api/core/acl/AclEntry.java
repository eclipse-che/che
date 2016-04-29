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
package org.eclipse.che.api.core.acl;

import java.util.List;

/**
 * This is the interface used for representing one entry in an Access Control List (ACL).
 *
 * @author Sergii Leschenko
 */
public interface AclEntry {
    /**
     * Returns user id or '*' for all users
     */
    String getUser();

    /**
     * Returns list of actions which are allowed to perform for specified user
     */
    List<String> getActions();
}
