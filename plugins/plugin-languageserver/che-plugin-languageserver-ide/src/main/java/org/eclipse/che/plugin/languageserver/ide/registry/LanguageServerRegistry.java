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
package org.eclipse.che.plugin.languageserver.ide.registry;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.che.api.languageserver.shared.model.LanguageRegex;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.eclipse.che.plugin.languageserver.ide.service.LanguageServerServiceClient;
import org.eclipse.lsp4j.ServerCapabilities;

/** @author Anatoliy Bazko */
@Singleton
public class LanguageServerRegistry {
  private LoaderFactory loaderFactory;
  private NotificationManager notificationManager;

  private final Map<FileType, LanguageRegex> registeredFileTypes = new ConcurrentHashMap<>();
  private final FileTypeRegistry fileTypeRegistry;
  private final LanguageServerServiceClient languageServerServiceClient;
  private final PromiseProvider promiseProvider;

  @Inject
  public LanguageServerRegistry(
      LoaderFactory loaderFactory,
      NotificationManager notificationManager,
      FileTypeRegistry fileTypeRegistry,
      LanguageServerServiceClient languageServerServiceClient,
      PromiseProvider promiseProvider) {

    this.loaderFactory = loaderFactory;
    this.notificationManager = notificationManager;
    this.fileTypeRegistry = fileTypeRegistry;
    this.languageServerServiceClient = languageServerServiceClient;
    this.promiseProvider = promiseProvider;
  }

  public Promise<ServerCapabilities> getOrInitializeServer(VirtualFile file) {
    // call initialize service
    final MessageLoader loader =
        loaderFactory.newLoader("Initializing Language Server for " + file.getName());
    loader.show();
    String wsPath = file.getLocation().toString();
    return languageServerServiceClient
        .initialize(wsPath)
        .thenPromise(
            serverCapabilities -> {
              loader.hide();
              return promiseProvider.resolve(serverCapabilities);
            })
        .catchError(
            promiseError -> {
              notificationManager.notify(
                  "Initializing Language Server for " + file.getName(),
                  promiseError.getMessage(),
                  FAIL,
                  EMERGE_MODE);
              loader.hide();
              return null;
            });
  }

  /**
   * Register file type for a language description
   *
   * @param type
   * @param description
   */
  public void registerFileType(FileType type, LanguageRegex description) {
    registeredFileTypes.put(type, description);
  }

  /**
   * Remove given File Type from registry.
   *
   * @param fileType the file type for removing
   */
  public void unRegister(FileType fileType) {
    if (fileType != null) {
      registeredFileTypes.remove(fileType);
    }
  }

  /** Returns the set of all registered file types. */
  public Set<FileType> getRegisteredFileTypes() {
    return new HashSet<>(registeredFileTypes.keySet());
  }

  /**
   * Get the language that is registered for this file. May return null if none is found.
   *
   * @param file
   * @return
   */
  public LanguageRegex getLanguageFilter(VirtualFile file) {
    FileType fileType = fileTypeRegistry.getFileTypeByFileName(file.getName());
    return registeredFileTypes.get(fileType);
  }

  /**
   * Provide ability to check if language is registered for given resource
   *
   * @param resource resource to check
   * @return {@code true} if given resource is file and language is registered for given resource,
   *     {@code false} otherwise
   */
  public boolean isLsRegistered(Resource resource) {
    return resource.isFile() && getLanguageFilter(resource.asFile()) != null;
  }
}
