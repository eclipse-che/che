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
package org.eclipse.che.git.impl;

import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.params.AddParams;
import org.eclipse.che.api.git.params.CommitParams;
import org.eclipse.che.api.git.shared.GitUser;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.subject.SubjectImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.write;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.core.util.LineConsumerFactory.NULL;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * @author Sergii Kabashniuk
 */

public class GitTestUtil {

    public static final String CONTENT = "git repository content\n";

    public static GitConnection connectToGitRepositoryWithContent(GitConnectionFactory connectionFactory, File repository)
            throws GitException, IOException {

        GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
        addFile(connection, "README.txt", CONTENT);
        connection.add(AddParams.create(singletonList("README.txt")));
        connection.commit(CommitParams.create("Initial commit"));
        return connection;
    }

    public static GitConnection connectToInitializedGitRepository(GitConnectionFactory connectionFactory, File repository)
            throws GitException, IOException {

        GitConnection connection = getTestUserConnection(connectionFactory, repository);
        connection.init(false);
        return connection;
    }

    public static void cleanupTestRepo(File testRepo) {
        IoUtil.deleteRecursive(testRepo);
    }

    public static GitConnection getTestUserConnection(GitConnectionFactory connectionFactory, File repository) throws GitException {
        EnvironmentContext.getCurrent().setSubject(new SubjectImpl("codenvy", "codenvy", null, false));
        return connectionFactory.getConnection(repository, NULL);
    }

    public static GitUser getTestGitUser() {
        return newDto(GitUser.class).withName("test_name").withEmail("test@email");
    }

    public static File addFile(GitConnection connection, String name, String content) throws IOException {
        return addFile(connection.getWorkingDir().toPath(), name, content);
    }

    public static File addFile(Path parent, String name, String content) throws IOException {
        if (!exists(parent)) {
            createDirectories(parent);
        }
        return write(parent.resolve(name), content.getBytes()).toFile();
    }

    public static void deleteFile(GitConnection connection, String name) throws IOException {
        delete(connection.getWorkingDir().toPath().resolve(name));
    }
}
