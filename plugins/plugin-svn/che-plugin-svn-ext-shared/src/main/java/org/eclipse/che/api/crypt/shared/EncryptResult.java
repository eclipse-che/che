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
package org.eclipse.che.api.crypt.shared;

public class EncryptResult {

    private String cipherText;
    private String initVector;

    public EncryptResult() {
    }

    public EncryptResult(final String cipherText, final String initVector) {
        super();
        this.cipherText = cipherText;
        this.initVector = initVector;
    }

    public String getCipherText() {
        return cipherText;
    }

    public void setCipherText(final String cipherText) {
        this.cipherText = cipherText;
    }

    public String getInitVector() {
        return initVector;
    }

    public void setInitVector(final String initVector) {
        this.initVector = initVector;
    }
}
