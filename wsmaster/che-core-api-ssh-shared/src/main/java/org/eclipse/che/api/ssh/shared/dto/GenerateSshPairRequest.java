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
package org.eclipse.che.api.ssh.shared.dto;

import org.eclipse.che.dto.shared.DTO;


/**
 * Interface describe a request for generating a SSH key pair.
 *
 * @author Sergii Leschenko
 */
@DTO
public interface GenerateSshPairRequest {
    /**
     * Returns name service that will use generated ssh pair.
     */
    String getService();

    void setService(String service);

    GenerateSshPairRequest withService(String service);

    /**
     * Returns name for generated ssh pair
     */
    String getName();

    void setName(String name);

    GenerateSshPairRequest withName(String name);
}
