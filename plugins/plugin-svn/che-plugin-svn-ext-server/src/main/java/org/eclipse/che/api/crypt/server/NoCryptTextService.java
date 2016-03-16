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
package org.eclipse.che.api.crypt.server;

import org.eclipse.che.api.crypt.shared.EncryptResult;

/**
 * Dummy encryption service that returns plain text.
 */
public class NoCryptTextService implements EncryptTextService {

    public static final int SCHEME_VERSION = 0;

    @Override
    public EncryptResult encryptText(String textToEncrypt) {
        return new EncryptResult(textToEncrypt, "");
    }

    @Override
    public String decryptText(final EncryptResult toDecrypt) {
        return toDecrypt.getCipherText();
    }

    @Override
    public int getSchemeVersion() {
        return SCHEME_VERSION;
    }

    @Override
    public boolean isActive() {
        return true;
    }

}
