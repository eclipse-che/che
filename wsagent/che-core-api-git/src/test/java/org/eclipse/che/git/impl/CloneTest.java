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
package org.eclipse.che.git.impl;

import com.google.common.io.Files;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.shared.CloneRequest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToGitRepositoryWithContent;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Igor Vinokur
 */
public class CloneTest {

    private File localRepo;
    private File remoteRepo;

    @BeforeMethod
    public void setUp() {
        localRepo = Files.createTempDir();
        remoteRepo = Files.createTempDir();
    }

    @AfterMethod
    public void cleanUp() {
        cleanupTestRepo(localRepo);
        cleanupTestRepo(remoteRepo);
    }

    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = GitConnectionFactoryProvider.class)
    public void testSimpleClone(GitConnectionFactory connectionFactory)
            throws ServerException, IOException, UnauthorizedException, URISyntaxException {
        //given
        GitConnection remoteConnection = connectToGitRepositoryWithContent(connectionFactory, remoteRepo);
        GitConnection localConnection = connectionFactory.getConnection(localRepo.getAbsolutePath());
        int filesBefore = localRepo.listFiles().length;

        //when
        localConnection.clone(newDto(CloneRequest.class).withRemoteUri(remoteConnection.getWorkingDir().getAbsolutePath()));

        //then
        int filesAfter = localRepo.listFiles().length;
        assertEquals(filesAfter, filesBefore + 2);
    }


    @Test(dataProvider = "GitConnectionFactory", dataProviderClass = GitConnectionFactoryProvider.class)
    public void testLineConsumerOutputWhenCloning(GitConnectionFactory connectionFactory)
            throws ServerException, IOException, UnauthorizedException, URISyntaxException {
        //given
        GitConnection remoteConnection = connectToGitRepositoryWithContent(connectionFactory, remoteRepo);
        GitConnection localConnection = connectionFactory.getConnection(localRepo.getAbsolutePath());

        LineConsumerFactory lineConsumerFactory = mock(LineConsumerFactory.class);
        LineConsumer lineConsumer = mock(LineConsumer.class);
        when(lineConsumerFactory.newLineConsumer()).thenReturn(lineConsumer);
        localConnection.setOutputLineConsumerFactory(lineConsumerFactory);

        //when
        localConnection.clone(newDto(CloneRequest.class).withRemoteUri(remoteConnection.getWorkingDir().getAbsolutePath()));

        //then
        verify(lineConsumer, atLeastOnce()).writeLine(anyString());
    }
}
