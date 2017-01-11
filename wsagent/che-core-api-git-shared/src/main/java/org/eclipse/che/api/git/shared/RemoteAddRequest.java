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

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Request to add remote configuration {@link #name} for repository at {@link #url}.
 *
 * @author andrew00x
 */
@DTO
public interface RemoteAddRequest {
    /** @return remote name */
    String getName();
    
    void setName(String name);
    
    RemoteAddRequest withName(String name);

    /** @return repository url */
    String getUrl();
    
    void setUrl(String url);
    
    RemoteAddRequest withUrl(String url);

    /** @return list of tracked branches in remote repository */
    List<String> getBranches();
    
    void setBranches(List<String> branches);
}