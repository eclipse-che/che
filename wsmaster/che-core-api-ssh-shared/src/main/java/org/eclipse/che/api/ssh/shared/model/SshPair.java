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
package org.eclipse.che.api.ssh.shared.model;

import org.eclipse.che.commons.annotation.Nullable;

/**
 * Defines ssh pair
 *
 * @author Sergii Leschenko
 */
public interface SshPair {
    /**
     * Returns name service that use current ssh pair. It is mandatory.
     */
    String getService();

    /**
     * Returns name of ssh pair. It is mandatory.
     */
    String getName();

    /**
     * Returns content of public key. It is optional
     */
    @Nullable
    String getPublicKey();

    /**
     * Returns content of private key. It is optional
     */
    @Nullable
    String getPrivateKey();
}
