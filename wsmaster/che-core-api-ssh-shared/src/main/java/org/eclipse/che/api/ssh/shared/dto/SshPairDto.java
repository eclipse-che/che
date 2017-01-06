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

import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.ssh.shared.model.SshPair;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Sergii Leschenko
 */
@DTO
public interface SshPairDto extends SshPair, Hyperlinks {
    @Override
    String getService();

    void setService(String service);

    SshPairDto withService(String service);

    @Override
    String getName();

    void setName(String name);

    SshPairDto withName(String name);

    @Override
    String getPublicKey();

    void setPublicKey(String publicKey);

    SshPairDto withPublicKey(String publicKey);

    @Override
    String getPrivateKey();

    void setPrivateKey(String privateKey);

    SshPairDto withPrivateKey(String privateKey);

    @Override
    SshPairDto withLinks(List<Link> links);
}
