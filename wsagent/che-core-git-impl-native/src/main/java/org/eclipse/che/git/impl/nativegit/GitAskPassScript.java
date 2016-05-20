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

import com.google.common.io.Files;

import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.git.UserCredential;
import org.eclipse.che.commons.lang.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Sergii Kabashniuk
 */
public class GitAskPassScript {

    private static final String GIT_ASK_PASS_SCRIPT_TEMPLATE = "META-INF/NativeGitAskPassTemplate";
    private static final String GIT_ASK_PASS_SCRIPT          = "ask_pass";
    private String gitAskPassTemplate;
    private File askScriptDirectory;

    private static final Logger LOG = LoggerFactory.getLogger(GitAskPassScript.class);

    public GitAskPassScript() {
        init();
    }

    /**
     * Searches "GIT_ASKPASS" script.
     *
     * @param credentials
     *         user credentials
     * @return stored script
     */
    public File build(UserCredential credentials) throws GitException {
        askScriptDirectory = Files.createTempDir();

        if (!askScriptDirectory.exists()) {
            askScriptDirectory.mkdirs();
        }
        File gitAskPassScript = new File(askScriptDirectory, GIT_ASK_PASS_SCRIPT);
        try (FileOutputStream fos = new FileOutputStream(gitAskPassScript)) {
            String actualGitAskPassTemplate = gitAskPassTemplate.replace("$self", gitAskPassScript.getAbsolutePath())
                                                                .replace("$password", credentials.getPassword())
                                                                .replace("$username", credentials.getUserName());
            fos.write(actualGitAskPassTemplate.getBytes());
        } catch (IOException e) {
            LOG.error("It is not possible to store " + gitAskPassScript + " credentials", e);
            throw new GitException("Can't store credentials");
        }
        if (!gitAskPassScript.setExecutable(true)) {
            LOG.error("Can't make " + gitAskPassScript + " executable");
            throw new GitException("Can't set permissions to credentials");
        }
        return gitAskPassScript;
    }

    public void remove() {

        if (askScriptDirectory != null && askScriptDirectory.exists()) {
            if (!IoUtil.deleteRecursive(askScriptDirectory))
                LOG.warn("Ask-pass script deletion failed.");
        }
    }

    public void init() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(GIT_ASK_PASS_SCRIPT_TEMPLATE)))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            gitAskPassTemplate = sb.toString();
        } catch (Exception e) {
            LOG.error("Can't load template " + GIT_ASK_PASS_SCRIPT_TEMPLATE);
        }
    }
}
