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
package org.eclipse.che.plugin.svn.shared.credentials;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface RepositoryCredentials {

    String getUsername();

    void setUsername(String username);

    RepositoryCredentials withUsername(String username);

    String getEncryptedPassword();

    void setEncryptedPassword(String password);

    RepositoryCredentials withEncryptedPassword(String password);

    String getInitVector();

    void setInitVector(String initVector);

    RepositoryCredentials withInitVector(String initVector);

    /**
     * Version of the encryption scheme used for the password.<br>
     * Add new versions when the scheme changes. Should allow to keep decrypting password stored before a change,<br>
     * or better, to migrate them between versions.<br>
     * Currently defined as:
     * <ul>
     * <li>0: no encryption
     * <li>1:
     * </ul>
     * 
     * @return
     */
    int getEncryptionSchemeVersion();

    void setEncryptionSchemeVersion(int version);

    RepositoryCredentials withEncryptionSchemeVersion(int version);
}
