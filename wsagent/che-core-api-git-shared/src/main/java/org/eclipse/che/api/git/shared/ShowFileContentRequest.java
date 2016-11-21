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
 * Request to show content of the file from specified revision or branch
 *
 * @author Igor Vinokur
 */
@DTO
public interface ShowFileContentRequest {
    /**
     * File path to show
     */
    String getFile();

    void setFile(String file);

    ShowFileContentRequest withFile(String file);

    /**
     * hash of revision or branch
     */
    String getVersion();

    void setVersion(String version);

    ShowFileContentRequest withVersion(String version);
}