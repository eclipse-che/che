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
package org.eclipse.che.api.vfs.shared.dto;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Representation of Access Control Entry used to interaction with client via JSON.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 */
@DTO
public interface AccessControlEntry {
    /** @return principal's permissions */
    List<String> getPermissions();

    AccessControlEntry withPermissions(List<String> permissions);

    void setPermissions(List<String> permissions);

    /** @return principal */
    Principal getPrincipal();

    AccessControlEntry withPrincipal(Principal principal);

    void setPrincipal(Principal principal);
}
