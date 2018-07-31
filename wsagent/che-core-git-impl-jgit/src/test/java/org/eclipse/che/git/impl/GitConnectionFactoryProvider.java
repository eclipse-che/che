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
 *   SAP           - implementation
 */
package org.eclipse.che.git.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.git.CredentialsLoader;
import org.eclipse.che.api.git.GitUserResolver;
import org.eclipse.che.git.impl.jgit.JGitConnectionFactory;
import org.eclipse.che.plugin.ssh.key.script.SshKeyProvider;
import org.testng.annotations.DataProvider;

/** @author Sergii Kabashniuk */
public class GitConnectionFactoryProvider {

  @DataProvider(name = "GitConnectionFactory")
  public static Object[][] createConnection() throws ServerException, NotFoundException {
    GitUserResolver resolver = mock(GitUserResolver.class);
    when(resolver.getUser()).thenReturn(GitTestUtil.getTestGitUser());
    return new Object[][] {
      new Object[] {
        new JGitConnectionFactory(
            mock(CredentialsLoader.class),
            mock(SshKeyProvider.class),
            mock(EventService.class),
            resolver)
      }
    };
  }
}
