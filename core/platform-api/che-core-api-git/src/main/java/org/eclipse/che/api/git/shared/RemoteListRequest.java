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
 * Request to get list of remotes. If {@link #remote} is specified then info about this remote only given.
 *
 * @author andrew00x
 */
@DTO
public interface RemoteListRequest extends GitRequest {
    /** @return if <code>true</code> show remote url and name otherwise show remote name only */
    boolean isVerbose();
    
    void setVerbose(boolean isVerbose);
    
    RemoteListRequest withVerbose(boolean verbose);

    /** @return remote name */
    String getRemote();
    
    RemoteListRequest withRemote(String remote);
    
    void setRemote(String remote);
}