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
package org.eclipse.che.ide.ext.java.client.command;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.web.bindery.event.shared.EventBus;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.ext.java.client.project.classpath.ProjectClasspathChangedEvent;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.jdt.ls.extension.api.dto.ClasspathEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** @author Valeriy Svydenko */
@RunWith(MockitoJUnitRunner.class)
public class ClasspathContainerTest {
  private static final String PROJECT_PATH = "/project1";

  @Mock private JavaLanguageExtensionServiceClient classpathServiceClient;
  @Mock private EventBus eventBus;

  @Mock private Promise<List<ClasspathEntry>> classpathEntries;

  @InjectMocks private ClasspathContainer classpathContainer;

  @Before
  public void setUp() throws Exception {
    when(classpathServiceClient.classpathTree(anyString())).thenReturn(classpathEntries);
  }

  @Test
  public void changedClasspathHandlerShouldBeAdded() throws Exception {
    verify(eventBus).addHandler(ProjectClasspathChangedEvent.TYPE, classpathContainer);
  }

  @Test
  public void classpathShouldBeAdded() throws Exception {
    Promise<List<ClasspathEntry>> entries = classpathContainer.getClasspathEntries(PROJECT_PATH);

    verify(classpathServiceClient).classpathTree(PROJECT_PATH);
    assertEquals(classpathEntries, entries);
  }

  @Test
  public void classpathAlreadyIncludes() throws Exception {
    classpathContainer.getClasspathEntries(PROJECT_PATH);

    reset(classpathServiceClient);

    Promise<List<ClasspathEntry>> entries = classpathContainer.getClasspathEntries(PROJECT_PATH);

    verify(classpathServiceClient, never()).classpathTree(PROJECT_PATH);
    assertEquals(classpathEntries, entries);
  }
}
