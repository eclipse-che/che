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

/**
 * Permissions group, allows to manage group access to data object
 * For example: permission for access to {@link org.eclipse.che.api.core.model.machine.Recipe}
 *
 * @author Eugene Voevodin
 */
public interface Group {

    /**
     * Returns group name (i.e. 'workspace/admin').
     * <p/>
     * Does not require existing organization group, may be any kind of string.
     *
     * For example group which allows read and write access for workspace/admin:
     * <pre>
     *      group: 'workspace/admin'
     *      unit: 'workspace'
     *      acl: 'read', 'write'
     * </pre>
     *
     * For public recipes following group data may be used:
     * <pre>
     *     group: 'public'
     *     unit: null
     *     acl: 'read'
     * </pre>
     */
    String getName();

    /**
     * Returns group unit (i.e. 'workspace', 'account') or {@code null}
     * when group name is not related to any unit
     */
    String getUnit();

    /**
     * Returns access control list for current group (i.e. 'read', 'write', 'search').
     * Returned instance is always updatable list, never {@code null}
     */
    List<String> getAcl();
}
