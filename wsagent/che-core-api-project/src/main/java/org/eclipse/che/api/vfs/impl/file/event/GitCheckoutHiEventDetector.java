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
package org.eclipse.che.api.vfs.impl.file.event;

import com.google.common.annotations.Beta;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.shared.dto.event.GitBranchCheckoutEventDto;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.io.File.separator;
import static java.util.Optional.empty;
import static java.util.regex.Pattern.compile;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.MODIFIED;
import static org.eclipse.che.api.vfs.impl.file.event.HiEvent.Category.UNDEFINED;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Detects if there was a .git/HEAD file modification, which is a sign of git branch
 * checkout operation, though in some rare cases it simply shows that the head of
 * current branch is changed.
 * <p>
 *     By the moment of this class creation  those situations are considered rare
 *     enough to ignore false detections.
 * </p>
 * <p>
 *     It is designed to detect only HEAD file MODIFICATION, which means that it will
 *     not trigger if those files are CREATED, DELETED, etc.
 * </p>
 * <p>
 *     This very implementation works only with git repositories initialized in
 *     the project root folder.
 * </p>
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
public class GitCheckoutHiEventDetector implements HiEventDetector<GitBranchCheckoutEventDto> {
    private static final Logger LOG = getLogger(GitCheckoutHiEventDetector.class);

    private static final String  GIT_DIR                  = ".git";
    private static final String  HEAD_FILE                = "HEAD";
    private static final String  GIT_OPERATION_WS_CHANNEL = "git-operations-channel";
    private static final int     PRIORITY                 = 50;
    private static final Pattern PATTERN                  = compile("ref: refs" + separator + "heads" + separator);

    private final VirtualFileSystemProvider virtualFileSystemProvider;
    private final HiEventBroadcaster        broadcaster;

    @Inject
    public GitCheckoutHiEventDetector(VirtualFileSystemProvider virtualFileSystemProvider,
                                      HiEventClientBroadcaster highLevelVfsEventClientBroadcaster) {
        this.virtualFileSystemProvider = virtualFileSystemProvider;
        this.broadcaster = highLevelVfsEventClientBroadcaster;
    }

    @Override
    public Optional<HiEvent<GitBranchCheckoutEventDto>> detect(EventTreeNode eventTreeNode) {
        if (!eventTreeNode.isRoot() || eventTreeNode.getChildren().isEmpty()) {
            return empty();
        }

        final Optional<EventTreeNode> headFile = eventTreeNode.getFirstChild()
                                                              .flatMap(o -> o.getChild(GIT_DIR))
                                                              .flatMap(o -> o.getChild(HEAD_FILE));

        if (headFile.isPresent()
            && headFile.get().modificationOccurred()
            && MODIFIED.equals(headFile.get().getLastEventType())) {

            final GitBranchCheckoutEventDto dto = newDto(GitBranchCheckoutEventDto.class).withBranchName(getBranchName(headFile.get()));

            return Optional.of(HiEvent.newInstance(GitBranchCheckoutEventDto.class)
                                      .withCategory(UNDEFINED.withPriority(PRIORITY))
                                      .withBroadcaster(broadcaster)
                                      .withChannel(GIT_OPERATION_WS_CHANNEL)
                                      .withDto(dto));
        } else {
            return empty();
        }
    }

    private String getBranchName(EventTreeNode file) {
        try {
            final String result = virtualFileSystemProvider.getVirtualFileSystem()
                                                     .getRoot()
                                                     .getChild(Path.of(file.getPath()))
                                                     .getContentAsString();
            return PATTERN.split(result)[1];
        } catch (ServerException | ForbiddenException e) {
            LOG.error("Error trying to read {} file and broadcast it", file.getPath(), e);
        }

        return "";
    }
}
