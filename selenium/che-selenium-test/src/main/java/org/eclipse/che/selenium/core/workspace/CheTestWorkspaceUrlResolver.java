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
package org.eclipse.che.selenium.core.workspace;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import org.eclipse.che.selenium.core.provider.TestIdeUrlProvider;

/** @author Anatolii Bazko */
@Singleton
public class CheTestWorkspaceUrlResolver implements TestWorkspaceUrlResolver {

  @Inject private TestIdeUrlProvider testIdeUrlProvider;

  @Override
  public URL resolve(TestWorkspace testWorkspace) throws MalformedURLException {
    try {
      return new URL(
          testIdeUrlProvider.get()
              + testWorkspace.getOwner().getName()
              + "/"
              + testWorkspace.getName());
    } catch (ExecutionException | InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }
}
