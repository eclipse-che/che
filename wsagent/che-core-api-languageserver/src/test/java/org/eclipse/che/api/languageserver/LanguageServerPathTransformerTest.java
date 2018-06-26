/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver;

import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import java.net.URI;
import org.eclipse.che.api.languageserver.RegistryContainer.Registry;
import org.eclipse.che.api.project.server.impl.RootDirPathProvider;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/*
 * Tests for {@link LanguageServerPathTransformer}
 */
@Listeners(MockitoTestNGListener.class)
public class LanguageServerPathTransformerTest {

  private static final String DEFAULT_PROJECTS_ROOT = "/projects";
  private static final String CUSTOM_PROJECTS_ROOT = "/home/user/projects";
  private static final String CORRECT_ID = "correct-id";
  private static final String INCORRECT_ID = "incorrect-id";
  private static final String PROJECT_NAME = "project";

  @Mock RootDirPathProvider rootDirPathProvider;

  @Spy RegistryContainer registryContainer = new RegistryContainer();

  @InjectMocks private LanguageServerPathTransformer languageServerPathTransformer;

  Registry<String> projectsRootRegistry = registryContainer.projectsRootRegistry;

  @BeforeMethod
  public void setUp() {
    when(rootDirPathProvider.get()).thenReturn(DEFAULT_PROJECTS_ROOT);

    projectsRootRegistry.add(CORRECT_ID, CUSTOM_PROJECTS_ROOT);
  }

  @Test
  public void shouldResolveCustomUri() throws Exception {
    URI actualUri = languageServerPathTransformer.resolve(PROJECT_NAME, CORRECT_ID);

    URI expectedURI = new URI("file:" + CUSTOM_PROJECTS_ROOT + "/" + PROJECT_NAME);

    assertEquals(actualUri, expectedURI);
  }

  @Test
  public void shouldResolveDefaultUri() throws Exception {
    URI actualUri = languageServerPathTransformer.resolve("project", INCORRECT_ID);

    URI expectedURI = new URI("file:" + DEFAULT_PROJECTS_ROOT + "/" + PROJECT_NAME);

    assertEquals(actualUri, expectedURI);
  }

  @Test
  public void shouldTransformUriToWsPathForCustomProjectsRoot() throws Exception {
    URI uri = new URI("file:" + CUSTOM_PROJECTS_ROOT + "/" + PROJECT_NAME);

    String actualPath = languageServerPathTransformer.toWsPath(uri, CORRECT_ID);

    String expectedPath = "/" + PROJECT_NAME;

    assertEquals(actualPath, expectedPath);
  }

  @Test
  public void shouldTransformUriToWsPathForDefaultProjectsRoot() throws Exception {
    URI uri = new URI("file:" + DEFAULT_PROJECTS_ROOT + "/" + PROJECT_NAME);

    String actualPath = languageServerPathTransformer.toWsPath(uri, INCORRECT_ID);

    String expectedPath = "/" + PROJECT_NAME;

    assertEquals(actualPath, expectedPath);
  }

  @Test
  public void shouldReturnUriPath() throws Exception {
    URI uri = new URI("file:" + DEFAULT_PROJECTS_ROOT + "/" + PROJECT_NAME);

    String actualPath = languageServerPathTransformer.toPath(uri);

    String expectedPath = DEFAULT_PROJECTS_ROOT + "/" + PROJECT_NAME;

    assertEquals(actualPath, expectedPath);
  }

  @Test
  public void shouldBeAbsoluteForAbsoluteStringPathsWithCustomRoot() {
    String path = CUSTOM_PROJECTS_ROOT + "/" + PROJECT_NAME;
    boolean isAbsolute = languageServerPathTransformer.isAbsolute(path, CORRECT_ID);
    assertTrue(isAbsolute);
  }

  @Test
  public void shouldBeAbsoluteForAbsoluteUriPathsWithCustomRoot() throws Exception {
    URI uri = new URI("file:" + CUSTOM_PROJECTS_ROOT + "/" + PROJECT_NAME);
    boolean isAbsolute = languageServerPathTransformer.isAbsolute(uri, CORRECT_ID);
    assertTrue(isAbsolute);
  }

  @Test
  public void shouldBeAbsoluteForAbsoluteStringPathsWithDefaultRoot() {
    String path = DEFAULT_PROJECTS_ROOT + "/" + PROJECT_NAME;
    boolean isAbsolute = languageServerPathTransformer.isAbsolute(path, INCORRECT_ID);
    assertTrue(isAbsolute);
  }

  @Test
  public void shouldBeAbsoluteForAbsoluteUriPathsWithDefaultRoot() throws Exception {
    URI uri = new URI("file:" + DEFAULT_PROJECTS_ROOT + "/" + PROJECT_NAME);
    boolean isAbsolute = languageServerPathTransformer.isAbsolute(uri, INCORRECT_ID);
    assertTrue(isAbsolute);
  }

  @Test
  public void shouldNotBeAbsoluteForAbsoluteStringPathsWithCustomRoot() {
    String path = "/user/projects/project";
    boolean isAbsolute = languageServerPathTransformer.isAbsolute(path, CORRECT_ID);
    assertFalse(isAbsolute);
  }

  @Test
  public void shouldNotBeAbsoluteForAbsoluteUriPathsWithCustomRoot() throws Exception {
    URI uri = new URI("file:/user/projects/project");
    boolean isAbsolute = languageServerPathTransformer.isAbsolute(uri, CORRECT_ID);
    assertFalse(isAbsolute);
  }

  @Test
  public void shouldNotBeAbsoluteForAbsoluteStringPathsWithDefaultRoot() {
    String path = "/project";
    boolean isAbsolute = languageServerPathTransformer.isAbsolute(path, INCORRECT_ID);
    assertFalse(isAbsolute);
  }

  @Test
  public void shouldNotBeAbsoluteForAbsoluteUriPathsWithDefaultRoot() throws Exception {
    URI uri = new URI("file:/project");
    boolean isAbsolute = languageServerPathTransformer.isAbsolute(uri, INCORRECT_ID);
    assertFalse(isAbsolute);
  }
}
