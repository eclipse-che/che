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
package org.eclipse.che.plugin.java.server.projecttype;

import static java.nio.file.Files.createFile;
import static java.nio.file.Files.createTempDirectory;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.ext.java.shared.Constants.CONTAINS_JAVA_FILES;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for the Project Type provider
 *
 * @author Florent Benoit
 */
@Listeners(value = {MockitoTestNGListener.class})
public class JavaValueProviderFactoryTest {

  private static final String SIMPLE_NAME = JavaValueProviderFactoryTest.class.getSimpleName();

  private static final String HELLOWORLD_JAVA = "helloworld.java";
  private static final String PROJECT_PATH = "/project/path";
  private static final String HELLOWORLD_JS = "helloworld.js";
  @Mock private PathTransformer pathTransformer;
  @InjectMocks private JavaValueProviderFactory javaValueProviderFactory;

  private File projectDirectory;
  private File subDirectory;
  private File file;

  @BeforeMethod
  public void createTemporaryDirectory() throws Exception {
    projectDirectory = createTempDirectory(SIMPLE_NAME).toFile();
  }

  @BeforeMethod
  public void setUp() throws Exception {
    when(pathTransformer.transform(PROJECT_PATH)).thenReturn(projectDirectory.toPath());
  }

  @AfterMethod
  public void cleanCreateFilesAndDirectories() throws Exception {
    projectDirectory.deleteOnExit();

    if (subDirectory != null) {
      subDirectory.deleteOnExit();
    }

    if (file != null) {
      file.deleteOnExit();
    }
  }

  /** In this case we have a folder with a java file, so it should find a java file */
  @Test
  public void checkFoundJavaFilesInCurrentFolder() throws Exception {
    file = createFile(projectDirectory.toPath().resolve(HELLOWORLD_JAVA)).toFile();

    List<String> hasJavaFiles =
        javaValueProviderFactory.newInstance(PROJECT_PATH).getValues(CONTAINS_JAVA_FILES);

    assertNotNull(hasJavaFiles);
    assertEquals(hasJavaFiles, singletonList("true"));
  }

  /** In this case we have a folder with a javascript file, so it shouldn't find any java files */
  @Test
  public void checkNotFoundJavaFilesInCurrentFolder() throws Exception {
    file = createFile(projectDirectory.toPath().resolve(HELLOWORLD_JS)).toFile();

    List<String> hasJavaFiles =
        javaValueProviderFactory.newInstance(PROJECT_PATH).getValues(CONTAINS_JAVA_FILES);

    assertNotNull(hasJavaFiles);
    assertEquals(hasJavaFiles, singletonList("false"));
  }

  /**
   * In this case we have a folder with a javascript file, but some sub folders contains java files
   */
  @Test
  public void checkFoundJavaButNotInRootFolder() throws Throwable {
    Path projectDirectoryPath = projectDirectory.toPath();
    Path subDirectoryPath = projectDirectoryPath.resolve("subDirectory");

    subDirectory = Files.createDirectory(subDirectoryPath).toFile();

    file = createFile(subDirectory.toPath().resolve(HELLOWORLD_JAVA)).toFile();

    List<String> hasJavaFiles =
        javaValueProviderFactory.newInstance(PROJECT_PATH).getValues(CONTAINS_JAVA_FILES);

    assertNotNull(hasJavaFiles);
    assertEquals(hasJavaFiles, singletonList("true"));
  }
}
