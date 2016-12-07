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
package org.eclipse.che.api.git;

import com.google.common.annotations.Beta;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jsonrpc.RequestTransmitter;
import org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto;
import org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto.Type;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;
import org.eclipse.che.api.vfs.impl.file.event.EventTreeNode;
import org.eclipse.che.api.vfs.impl.file.event.HiEvent;
import org.eclipse.che.api.vfs.impl.file.event.HiEventDetector;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Optional.empty;
import static java.util.regex.Pattern.compile;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.MODIFIED;
import static org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto.Type.BRANCH;
import static org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto.Type.REVISION;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Detects if there was a .git/HEAD file modification, which is a sign of git branch
 * checkout operation, though in some rare cases it simply shows that the head of
 * current branch is changed.
 * <p>
 * By the moment of this class creation  those situations are considered rare
 * enough to ignore false detections.
 * </p>
 * <p>
 * It is designed to detect only HEAD file MODIFICATION, which means that it will
 * not trigger if those files are CREATED, DELETED, etc.
 * </p>
 * <p>
 * This very implementation works only with git repositories initialized in
 * the project root folder.
 * </p>
 *
 * @author Dmitry Kuleshov
 * @since 4.5
 */
@Beta
public class GitCheckoutHiEventDetector implements HiEventDetector<GitCheckoutEventDto> {
    private static final Logger LOG = getLogger(GitCheckoutHiEventDetector.class);

    private static final String  GIT_DIR   = ".git";
    private static final String  HEAD_FILE = "HEAD";
    private static final Pattern PATTERN   = compile("ref: refs/heads/");

    private final VirtualFileSystemProvider virtualFileSystemProvider;
    private final RequestTransmitter        transmitter;

    @Inject
    public GitCheckoutHiEventDetector(VirtualFileSystemProvider virtualFileSystemProvider, RequestTransmitter transmitter) {
        this.virtualFileSystemProvider = virtualFileSystemProvider;
        this.transmitter = transmitter;
    }

    @Override
    public Optional<HiEvent<GitCheckoutEventDto>> detect(EventTreeNode eventTreeNode) {
        if (!eventTreeNode.isRoot() || eventTreeNode.getChildren().isEmpty()) {
            return empty();
        }

        final Optional<EventTreeNode> headFile = eventTreeNode.getFirstChild()
                                                              .flatMap(o -> o.getChild(GIT_DIR))
                                                              .flatMap(o -> o.getChild(HEAD_FILE));
        if (headFile.isPresent()) {
            final EventTreeNode file = headFile.get();

            if (file.modificationOccurred() && MODIFIED == file.getLastEventType()) {
                final String fileContent = getFileContent(file);
                final Type type = getType(fileContent);
                final String name = getName(fileContent, type);

                transmitter.broadcast("event:git-checkout", newDto(GitCheckoutEventDto.class).withName(name).withType(type));

            }
        }
        return Optional.empty();
    }

    private Type getType(String content) {
        return content.contains("ref:") ? BRANCH : REVISION;
    }

    private String getName(String content, Type type) {
        return type == REVISION ? content : PATTERN.split(content)[1];
    }

    private String getFileContent(EventTreeNode file) {
        try {
            return virtualFileSystemProvider.getVirtualFileSystem()
                                            .getRoot()
                                            .getChild(Path.of(file.getPath()))
                                            .getContentAsString();
        } catch (ServerException | ForbiddenException e) {
            LOG.error("Error trying to read {} file and broadcast it", file.getPath(), e);
        }
        return null;
    }
}
