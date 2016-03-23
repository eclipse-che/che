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
package org.eclipse.che.git.impl.nativegit;

import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.git.CredentialsLoader;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.git.GitUserResolver;
import org.eclipse.che.git.impl.nativegit.ssh.GitSshScriptProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

/**
 * Native implementation for GitConnectionFactory
 *
 * @author Eugene Voevodin
 * @author Valeriy Svydenko
 */
@Singleton
public class NativeGitConnectionFactory extends GitConnectionFactory {

    private final CredentialsLoader    credentialsLoader;
    private final GitSshScriptProvider gitSshScriptProvider;
    private final GitUserResolver      userResolver;

    @Inject
    public NativeGitConnectionFactory(CredentialsLoader credentialsLoader, GitSshScriptProvider gitSshScriptProvider, GitUserResolver userResolver) {
        this.credentialsLoader = credentialsLoader;
        this.gitSshScriptProvider = gitSshScriptProvider;
        this.userResolver = userResolver;
    }


    @Override
    public GitConnection getConnection(File workDir, LineConsumerFactory outputPublisherFactory) throws GitException {
        final GitConnection gitConnection = new NativeGitConnection(workDir, gitSshScriptProvider, credentialsLoader, userResolver);
        gitConnection.setOutputLineConsumerFactory(outputPublisherFactory);
        return gitConnection;
    }

    @Override
    public CredentialsLoader getCredentialsLoader() {
        return credentialsLoader;
    }

}