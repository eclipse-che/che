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
package org.eclipse.che.api.git.shared;

import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.dto.shared.DTO;

/**
 * @author Anton Korneta.
 */
@EventOrigin("gitcheckout")
@DTO
public interface GitCheckoutEvent {

    boolean isCheckoutOnly();

    void setCheckoutOnly(boolean checkoutOnly);

    GitCheckoutEvent withCheckoutOnly(boolean checkoutOnly);

    String getBranchRef();

    void setBranchRef(String branchRef);

    GitCheckoutEvent withBranchRef(String branchRef);

    String getWorkspaceId();

    void setWorkspaceId(String workspaceId);

    GitCheckoutEvent withWorkspaceId(String workspaceId);

    String getProjectName();

    void setProjectName(String projectName);

    GitCheckoutEvent withProjectName(String projectName);
}
