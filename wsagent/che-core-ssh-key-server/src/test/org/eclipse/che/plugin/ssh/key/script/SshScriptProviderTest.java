/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.ssh.key.script;

import org.eclipse.che.api.git.GitException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class SshScriptProviderTest {
    private static final String SSH_KEY             = "key";
    private static final String SCRIPT_FILE         = "ssh_script";
    private static final String SCRIPT_FILE_WINDOWS = "ssh_script.bat";
    private static final String URL                 = "git@github.com:codenvy/test.git";

    @Mock
    SshKeyProvider keyProvider;

    @InjectMocks
    private GitSshScriptProvider scriptProvider;

    @BeforeMethod
    public void setUp() throws FileNotFoundException, GitException {
        when(keyProvider.getPrivateKey(URL)).thenReturn(SSH_KEY.getBytes());
    }

    @Test
    public void checkExistenceScriptFileTest() throws GitException, IOException {
        boolean b = false;
        GitSshScript gitSshScript = scriptProvider.gitSshScript(URL);
        File scriptDirectory = gitSshScript.getSshScriptFile().getParentFile();
        if (scriptDirectory.exists() && scriptDirectory.isDirectory() && scriptDirectory.listFiles() != null) {
            for (File file : scriptDirectory.listFiles()) {
                String fileName = file.getName();
                b = file.isFile()
                    && (fileName.equalsIgnoreCase(SCRIPT_FILE) || fileName.equalsIgnoreCase(SCRIPT_FILE_WINDOWS))
                    || b;
            }
        }
        gitSshScript.delete();
        assertTrue(b);
    }
}
