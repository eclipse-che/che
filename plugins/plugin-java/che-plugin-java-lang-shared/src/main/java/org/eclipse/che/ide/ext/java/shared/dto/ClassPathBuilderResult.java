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
package org.eclipse.che.ide.ext.java.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * DTO represents the information about building classpath of the project.
 *
 * @author Valeriy Svydenko
 */
@DTO
public interface ClassPathBuilderResult {
    /** @return status of maven operation. */
    Status getStatus();

    void setStatus(Status name);

    /** @return output logs of maven operation. */
    String getLogs();

    void setLogs(String logs);

    String getChannel();

    void setChannel(String channel);

    enum Status {
        SUCCESS,
        ERROR,
    }
}
