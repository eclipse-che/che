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
package org.eclipse.che.api.project.server.watcher;

import org.eclipse.che.api.project.server.FilesBuffer;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;

import javax.validation.constraints.NotNull;
import javax.websocket.EncodeException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static org.eclipse.che.api.project.server.Constants.CODENVY_DIR;

/**
 * The class contains business logic which allows describe on some folder on file system and handle all changes related to
 * described folder.
 *
 * @author Dmitry Shnurenko
 */
class Watcher {

    private static final String WATCHER_WS_CHANEL = "watcher:chanel:%s";

    private final WatchService        watcher;
    private final Map<WatchKey, Path> keys;
    private final FilesBuffer         filesBuffer;
    private final boolean             recursive;

    private final String pathToRoot;

    /**
     * Creates entity of {@link Watcher} to listen changes on file system.
     *
     * @param dir
     *         directory which will be registered via watcher
     * @param filesBuffer
     *         buffer which need to separate actions IDE and file systems
     * @param recursive
     *         <code>true</code> watcher will be registered on current directory and all inner directories
     *         <code>false</code>  watcher will be registered only on current directory
     * @throws IOException
     */
    Watcher(Path dir, FilesBuffer filesBuffer, boolean recursive) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        this.recursive = recursive;
        this.filesBuffer = filesBuffer;
        this.pathToRoot = dir.toString();

        if (recursive) {
            registerAll(dir);
        } else {
            register(dir);
        }
    }

    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }

    private void registerAll(final Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path directoryToRegister, BasicFileAttributes attrs) throws IOException {
                String relativePath = directoryToRegister.toString().substring(pathToRoot.length());

                if (absentServiceDirectory(relativePath)) {
                    register(directoryToRegister);
                }

                return FileVisitResult.CONTINUE;
            }
        });
    }

    private boolean absentServiceDirectory(@NotNull String path) {
        boolean isTargetFolder = path.contains("target");
        boolean isCheFolder = path.contains(".che");
        //noinspection SpellCheckingInspection
        boolean isCodenvyFolder = path.contains(CODENVY_DIR);
        boolean isVfsFolder = path.contains(".vfs");
        boolean isGitFolder = path.contains(".git");
        boolean isMvnFolder = path.contains(".mvn");

        return !isCheFolder && !isCodenvyFolder && !isTargetFolder && !isVfsFolder && !isGitFolder && !isMvnFolder;
    }

    /**
     * Starts process events to catch all actions related to file system and sends special event on client through web socket
     * when something changed on file system. This method is started in separated thread.
     */
    void processEvents() {
        while (true) {

            try {
                WatchKey key = watcher.take();

                Path changedDirectory = keys.get(key);

                if (changedDirectory == null) {
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind kind = event.kind();

                    if (kind == OVERFLOW) {
                        continue;
                    }

                    //noinspection unchecked
                    WatchEvent<Path> watchEvent = (WatchEvent<Path>)event;
                    Path name = watchEvent.context();
                    Path child = changedDirectory.resolve(name);

                    registerChild(kind, child);

                    sendEvents(kind, changedDirectory.toString() + '/' + name.toString());
                }

                boolean valid = key.reset();
                if (!valid) {
                    keys.remove(key);

                    if (keys.isEmpty()) {
                        break;
                    }
                }
            } catch (InterruptedException x) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void registerChild(@NotNull WatchEvent.Kind kind, @NotNull Path pathToChild) {
        if (recursive && (kind == ENTRY_CREATE)) {
            try {
                if (Files.isDirectory(pathToChild, NOFOLLOW_LINKS)) {
                    registerAll(pathToChild);
                }
            } catch (IOException x) {
                //to do nothing
            }
        }
    }

    private void sendEvents(@NotNull WatchEvent.Kind eventKind, @NotNull String pathToNewFile) {
        String relativePath = pathToNewFile.substring(pathToRoot.length() + 1);

        if (eventKind == ENTRY_CREATE || eventKind == ENTRY_DELETE) {
            ChannelBroadcastMessage broadcastMessage = new ChannelBroadcastMessage();

            broadcastMessage.setChannel(String.format(WATCHER_WS_CHANEL, "1"));
            broadcastMessage.setBody(String.format(relativePath));

            boolean bufferContainsPath = filesBuffer.isContainsPath(relativePath);

            try {
                if (!bufferContainsPath && absentServiceDirectory(relativePath) && !correctNodeName(relativePath)) {
                    WSConnectionContext.sendMessage(broadcastMessage);
                }
            } catch (EncodeException | IOException exception) {
                Thread.currentThread().interrupt();
            }
        }
    }

    //need for cloud
    private boolean correctNodeName(@NotNull String relativePath) {
        int startName = relativePath.lastIndexOf("/") + 1;
        String name = relativePath.substring(startName);

        return name.startsWith(".");
    }
}