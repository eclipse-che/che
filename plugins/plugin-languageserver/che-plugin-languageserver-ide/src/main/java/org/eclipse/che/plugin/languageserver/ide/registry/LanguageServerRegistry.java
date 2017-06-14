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
package org.eclipse.che.plugin.languageserver.ide.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.eclipse.che.plugin.languageserver.ide.service.LanguageServerRegistryJsonRpcClient;
import org.eclipse.che.plugin.languageserver.ide.service.LanguageServerRegistryServiceClient;
import org.eclipse.lsp4j.ServerCapabilities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class LanguageServerRegistry {
    private final LanguageServerRegistryJsonRpcClient                                     jsonRpcClient;
    private       LoaderFactory                                                           loaderFactory;
    private       NotificationManager                                                     notificationManager;

    private final Map<FileType, LanguageDescription>                                      registeredFileTypes = new ConcurrentHashMap<>();
    private final FileTypeRegistry                                                        fileTypeRegistry;

    @Inject
    public LanguageServerRegistry(EventBus eventBus,
                                  LoaderFactory loaderFactory,
                                  NotificationManager notificationManager,
                                  LanguageServerRegistryJsonRpcClient jsonRpcClient,
                                  LanguageServerRegistryServiceClient client, 
                                  FileTypeRegistry fileTypeRegistry) {


        this.loaderFactory = loaderFactory;
        this.notificationManager = notificationManager;
        this.jsonRpcClient = jsonRpcClient;
        this.fileTypeRegistry= fileTypeRegistry;
    }

    public Promise<ServerCapabilities> getOrInitializeServer(String projectPath, VirtualFile file) {
        // call initialize service
        final MessageLoader loader = loaderFactory.newLoader("Initializing Language Server for " + file.getName());
        loader.show();
        return jsonRpcClient.initializeServer(file.getLocation().toString()).then((ServerCapabilities arg) -> {
            loader.hide();
            return arg;
        }).catchError(arg -> {
            notificationManager.notify("Initializing Language Server for " + file.getName(), arg.getMessage(), FAIL, EMERGE_MODE);
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
    public void registerFileType(FileType type, LanguageDescription description) {
        fileTypeRegistry.registerFileType(type);
        registeredFileTypes.put(type, description);
    }

    /**
     * Get the language that is registered for this file. May return null if
     * none is found.
     * 
     * @param file
     * @return
     */
    public LanguageDescription getLanguageDescription(VirtualFile file) {
        FileType fileType = fileTypeRegistry.getFileTypeByFile(file);
        if (fileType == null) {
            return null;
        }
        return registeredFileTypes.get(fileType);
    }

}
