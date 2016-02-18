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
package org.eclipse.che.api.machine.shared;


import java.util.List;
import java.util.Map;

/**
 * Defines permissions, which allows access by certain groups and users to the data objects
 *
 * @author Eugene Voevodin
 */
public interface Permissions {

    /**
     * Returns <i>user identifier -> access control list</i> mapping.
     * <p/>
     * For example two users who have different access to recipe:
     * <pre>
     *     user123 : ['read', 'write']
     *     user234 : ['read']
     * </pre>
     * Always returns updatable map, never {@code null}
     */
    Map<String, List<String>> getUsers();

    /**
     * Returns list of groups which have access to the data object.
     * Always returns updatable list, never {@code null}
     */
    List<? extends Group> getGroups();
}


