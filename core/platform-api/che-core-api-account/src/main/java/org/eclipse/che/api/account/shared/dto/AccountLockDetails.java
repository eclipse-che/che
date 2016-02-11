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
package org.eclipse.che.api.account.shared.dto;

import org.eclipse.che.dto.shared.DTO;


/**
 * @author Oleksii Orel
 */
@DTO
public interface AccountLockDetails {
    String getAccountId();

    void setAccountId(String accountId);

    AccountLockDetails withAccountId(String accountId);

    Boolean isLocked();

    void setLocked(Boolean status);

    AccountLockDetails withLocked(Boolean status);
}
