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
package org.eclipse.che.git.impl.nativegit.ssh;

import org.eclipse.che.api.git.GitException;

/**
 * @author Sergii Kabashniuk
 */
public interface SshKeyProvider {

    /**
     * Get private ssh key.
     *
     * @param url
     *         url to git repository
     * @return byte array that contains private ssh key
     * @throws GitException
     *         if an error occurs while fetching keys
     */
    byte[] getPrivateKey(String url) throws GitException;
}
