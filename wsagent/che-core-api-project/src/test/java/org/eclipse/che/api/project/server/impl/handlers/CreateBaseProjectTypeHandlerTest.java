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
package org.eclipse.che.api.project.server.impl.handlers;

import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.fs.server.WsPathUtils.resolve;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import com.google.common.io.Files;
import java.io.File;
import java.io.InputStream;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.impl.CreateBaseProjectTypeHandler;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Vitalii Parfonov */
@Listeners(MockitoTestNGListener.class)
public class CreateBaseProjectTypeHandlerTest {

  @Mock private FsManager fsManager;
  @InjectMocks private CreateBaseProjectTypeHandler createBaseProjectTypeHandler;

  private File root;

  @BeforeMethod
  public void createTemporaryRootDirectory() throws Exception {
    root = Files.createTempDir();
  }

  @AfterMethod
  public void deleteOnExitTemporaryRootDirectory() throws Exception {
    root.deleteOnExit();
  }

  @Test
  public void testCreateProject() throws Exception {
    String projectWsPath = root.toPath().toAbsolutePath().toString();
    String readmeWsPath = resolve(projectWsPath, "README");
    createBaseProjectTypeHandler.onCreateProject(projectWsPath, emptyMap(), emptyMap());

    verify(fsManager).createDir(projectWsPath, true, true);
    verify(fsManager).createFile(eq(readmeWsPath), any(InputStream.class));
  }
}
