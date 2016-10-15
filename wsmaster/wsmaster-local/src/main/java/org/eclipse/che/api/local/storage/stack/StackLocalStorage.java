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
package org.eclipse.che.api.local.storage.stack;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.local.StackDeserializer;
import org.eclipse.che.api.local.WorkspaceConfigDeserializer;
import org.eclipse.che.api.local.storage.LocalStorage;
import org.eclipse.che.api.workspace.server.WorkspaceConfigJsonAdapter;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.stack.StackJsonAdapter;
import org.eclipse.che.api.workspace.server.stack.image.StackIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.lang.String.format;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.Collections.singletonMap;
import static org.apache.commons.io.FileUtils.deleteDirectory;

/**
 * Local {@link StackImpl} storage for storing stacks and their {@link StackIcon}
 *
 * @author Alexander Andrienko
 */
@Singleton
public class StackLocalStorage {

    private static final Logger LOG = LoggerFactory.getLogger(StackLocalStorage.class);

    public static final String STACK_STORAGE_FILE = "stacks.json";
    private static final String ICON_FOLDER_NAME   = "images";

    private final LocalStorage localStorage;
    private final Path         iconFolderPath;

    @Inject
    public StackLocalStorage(@Named("che.conf.storage") String pathToStorage,
                             StackJsonAdapter stackJsonAdapter) throws IOException {
        this.localStorage = new LocalStorage(pathToStorage,
                                             STACK_STORAGE_FILE,
                                             singletonMap(StackImpl.class, new StackDeserializer(stackJsonAdapter)));
        this.iconFolderPath = Paths.get(pathToStorage, ICON_FOLDER_NAME);
    }

    /**
     * Store map {@code stacks} to the local stack storage
     *
     * @param stacks
     *         map, where key is id of the {@link StackImpl} and value is {@link StackImpl})
     * @throws IOException
     */
    public void store(Map<String, StackImpl> stacks) throws IOException {
        localStorage.store(stacks);
        deleteDirectory(iconFolderPath.toFile());
        stacks.values().forEach(this::saveIcon);
    }

    /**
     * Load map(where key is id of the {@link StackImpl} and value is {@link StackImpl}) from local stack storage.
     */
    public Map<String, StackImpl> loadMap() {
        Map<String, StackImpl> stackMap = localStorage.loadMap(new TypeToken<Map<String, StackImpl>>() {
        });
        for (StackImpl stack : stackMap.values()) {
            setIconData(stack, iconFolderPath);
        }
        return stackMap;
    }

    /**
     * Set binary data to {@link StackIcon} of the {@code stack}. Stack icon files store in the by {@code stackIconFolderPath}
     * Icon data stores in the local storage by path:
     * {@code stackIconFolderPath}/stackId/IconName.
     *
     * @param stack
     *         stack to update stack icon data
     * @param stackIconFolderPath
     *         path to the folder with stack icons
     * @see StackImpl
     * @see StackIcon
     */
    private void setIconData(StackImpl stack, Path stackIconFolderPath) {
        StackIcon stackIcon = stack.getStackIcon();
        if (stackIcon == null) {
            return;
        }
        try {
            Path stackIconPath = stackIconFolderPath.resolve(stack.getId()).resolve(stackIcon.getName());
            if (Files.exists(stackIconPath) && Files.isRegularFile(stackIconPath)) {
                stackIcon = new StackIcon(stackIcon.getName(), stackIcon.getMediaType(), Files.readAllBytes(stackIconPath));
                stack.setStackIcon(stackIcon);
            } else {
                throw new IOException("Stack icon is not a file or doesn't exist by path: " + stackIconPath);
            }
        } catch (IOException e) {
            stack.setStackIcon(null);
            LOG.error(format("Failed to load stack icon data for the stack with id '%s'", stack.getId()), e);
        }
    }

    /**
     * Save {@link StackIcon} of the {@code stack} to the local storage
     *
     * @param stack
     *         {@link StackImpl} which contains {@link StackIcon} to store
     */
    private void saveIcon(StackImpl stack) {
        try {
            StackIcon stackIcon = stack.getStackIcon();
            if (stackIcon != null && stackIcon.getData() != null) {
                Path iconParentDirectory = iconFolderPath.resolve(stack.getId());
                Files.createDirectories(iconParentDirectory);
                Path iconPath = iconParentDirectory.resolve(stackIcon.getName());
                Files.write(iconPath, stackIcon.getData(), CREATE, TRUNCATE_EXISTING);
            }
        } catch (IOException ex) {
            LOG.error(format("Failed to save icon for stack with id '%s'", stack.getId()), ex);
        }
    }
}
