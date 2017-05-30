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
package org.eclipse.che.plugin.ssh.key.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Uploads public key part. Must be registered in {@link SshKeyUploaderRegistry}:
 *
 * @author Ann Shumilova
 */
public interface SshKeyUploader {
    /**
     * @param userId
     *         user's id, for whom to generate key
     * @param callback
     *         callback
     */
    void uploadKey(String userId, AsyncCallback<Void> callback);
}
