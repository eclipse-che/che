/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.ssh.key.client;

import static org.eclipse.che.plugin.ssh.key.client.manage.SshKeyManagerPresenter.VCS_SSH_SERVICE;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.ssh.SshServiceClient;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Provides business logic for Ssh related operations.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class SshKeyManager {

  private PromiseProvider promiseProvider;
  private SshServiceClient sshServiceClient;
  private SshKeyUploaderRegistry sshKeyUploaderRegistry;

  @Inject
  public SshKeyManager(
      PromiseProvider promiseProvider,
      SshKeyUploaderRegistry sshKeyUploaderRegistry,
      SshServiceClient sshServiceClient) {
    this.promiseProvider = promiseProvider;
    this.sshServiceClient = sshServiceClient;
    this.sshKeyUploaderRegistry = sshKeyUploaderRegistry;
  }

  /**
   * Generates and uploads new public key for specified host. Removes the key when an exception is
   * happen at uploading the key
   *
   * @param userId the id of the user who will be the owner of the ssh pair
   * @param host host to generate the key
   */
  public Promise<Void> generateSshKey(String userId, String host) {
    SshKeyUploader sshKeyUploader = sshKeyUploaderRegistry.getUploader(host);
    if (sshKeyUploader == null) {
      return promiseProvider.reject("Can not find ssh keys uploader for " + host);
    }

    return promiseProvider.create(
        callback ->
            sshKeyUploader.uploadKey(
                userId,
                new AsyncCallback<Void>() {
                  @Override
                  public void onSuccess(Void result) {
                    callback.onSuccess(result);
                  }

                  @Override
                  public void onFailure(Throwable exception) {
                    callback.onFailure(exception);

                    removeSshKey(host);
                  }
                }));
  }

  /**
   * Removes the key for the specified host.
   *
   * @param host host to remove the key
   */
  public void removeSshKey(String host) {
    sshServiceClient
        .getPair(VCS_SSH_SERVICE, host)
        .thenPromise(pair -> sshServiceClient.deletePair(pair.getService(), pair.getName()))
        .catchError(
            arg -> {
              Log.error(getClass(), arg.getCause());
            });
  }

  /**
   * Checks if current user has ssh keys for the specified host.
   *
   * @return {@link Promise} with {@code true} when ssh key is available for the host, {@code false}
   *     otherwise
   */
  public Promise<Boolean> isSshKeyAvailable(String host) {
    return promiseProvider.create(
        callback ->
            sshServiceClient
                .getPair(VCS_SSH_SERVICE, host)
                .then(
                    arg -> {
                      callback.onSuccess(true);
                    })
                .catchError(
                    arg -> {
                      callback.onSuccess(false);

                      Log.info(getClass(), "Ssh key is not available for " + host);
                    }));
  }
}
