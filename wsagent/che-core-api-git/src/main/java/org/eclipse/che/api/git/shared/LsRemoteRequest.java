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
package org.eclipse.che.api.git.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Request for calling git ls-remote.
 *
 * @author Vladyslav Zhukovskii
 */
@DTO
public interface LsRemoteRequest extends GitRequest {
    /** @return url of remote repository. */
    String getRemoteUrl();

    void setRemoteUrl(String remoteUrl);

    LsRemoteRequest withRemoteUrl(String remoteUrl);
}
