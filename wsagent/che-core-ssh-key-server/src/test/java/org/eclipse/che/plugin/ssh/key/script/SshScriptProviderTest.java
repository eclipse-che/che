/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.ssh.key.script;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.eclipse.che.api.core.ServerException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Anton Korneta */
@Listeners(MockitoTestNGListener.class)
public class SshScriptProviderTest {
  private static final String SSH_KEY = "key";
  private static final String SCRIPT_FILE = "ssh_script";
  private static final String SCRIPT_FILE_WINDOWS = "ssh_script.bat";
  private static final String URL = "git@github.com:codenvy/test.git";

  @Mock SshKeyProvider keyProvider;

  @InjectMocks private SshScriptProvider scriptProvider;

  @BeforeMethod
  public void setUp() throws FileNotFoundException, ServerException {
    when(keyProvider.getPrivateKey(URL)).thenReturn(SSH_KEY.getBytes());
  }

  @Test
  public void checkExistenceScriptFileTest() throws ServerException, IOException {
    boolean b = false;
    SshScript sshScript = scriptProvider.getSshScript(URL);
    File scriptDirectory = sshScript.getSshScriptFile().getParentFile();
    if (scriptDirectory.exists()
        && scriptDirectory.isDirectory()
        && scriptDirectory.listFiles() != null) {
      for (File file : scriptDirectory.listFiles()) {
        String fileName = file.getName();
        b =
            file.isFile()
                    && (fileName.equalsIgnoreCase(SCRIPT_FILE)
                        || fileName.equalsIgnoreCase(SCRIPT_FILE_WINDOWS))
                || b;
      }
    }
    sshScript.delete();
    assertTrue(b);
  }
}
