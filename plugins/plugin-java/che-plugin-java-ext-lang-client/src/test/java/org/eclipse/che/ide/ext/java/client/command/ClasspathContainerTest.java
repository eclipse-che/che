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
import org.eclipse.che.ide.ext.java.client.project.classpath.ClasspathChangedEvent;
import org.eclipse.che.ide.ext.java.client.project.classpath.service.ClasspathServiceClient;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDto;
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

  @Mock private ClasspathServiceClient classpathServiceClient;
  @Mock private EventBus eventBus;

  @Mock private Promise<List<ClasspathEntryDto>> classpathEntries;

  @InjectMocks private ClasspathContainer classpathContainer;

  @Before
  public void setUp() throws Exception {
    when(classpathServiceClient.getClasspath(anyString())).thenReturn(classpathEntries);
  }

  @Test
  public void changedClasspathHandlerShouldBeAdded() throws Exception {
    verify(eventBus).addHandler(ClasspathChangedEvent.TYPE, classpathContainer);
  }

  @Test
  public void classpathShouldBeAdded() throws Exception {
    Promise<List<ClasspathEntryDto>> entries = classpathContainer.getClasspathEntries(PROJECT_PATH);

    verify(classpathServiceClient).getClasspath(PROJECT_PATH);
    assertEquals(classpathEntries, entries);
  }

  @Test
  public void classpathAlreadyIncludes() throws Exception {
    classpathContainer.getClasspathEntries(PROJECT_PATH);

    reset(classpathServiceClient);

    Promise<List<ClasspathEntryDto>> entries = classpathContainer.getClasspathEntries(PROJECT_PATH);

    verify(classpathServiceClient, never()).getClasspath(PROJECT_PATH);
    assertEquals(classpathEntries, entries);
  }
}
