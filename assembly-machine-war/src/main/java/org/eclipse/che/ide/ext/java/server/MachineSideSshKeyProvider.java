/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.server;

import org.eclipse.che.api.git.GitException;
import org.eclipse.che.git.impl.nativegit.ssh.SshKeyProvider;

/**
 * //
 *
 * @author Vitalii Parfonov
 */
public class MachineSideSshKeyProvider implements SshKeyProvider {
    @Override
    public byte[] getPrivateKey(String url) throws GitException {
        return new byte[0];
    }
}
