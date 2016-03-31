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
 * Service that is able to encrypt/decrypt text based on an encryption algorithm
 */
public interface EncryptTextService {

    EncryptResult encryptText(final String textToEncrypt) throws EncryptException;

    String decryptText(final EncryptResult toDecrypt) throws EncryptException;

    int getSchemeVersion();

    boolean isActive();
}

