/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.ssh.key.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Uploads public key part. Must be registered in {@link SshKeyUploaderRegistry}:
 *
 * @author Ann Shumilova
 */
public interface SshKeyUploader {
  /**
   * @param userId user's id, for whom to generate key
   * @param callback callback
   */
  void uploadKey(String userId, AsyncCallback<Void> callback);
}
