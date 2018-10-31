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
package org.eclipse.che.ide.ext.java.client.command.valueproviders;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.command.ClasspathContainer;
import org.eclipse.che.ide.ext.java.client.project.classpath.ClasspathResolver;
import org.eclipse.che.ide.ext.java.shared.Constants;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.jdt.ls.extension.api.dto.ClasspathEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** @author Valeriy Svydenko */
@RunWith(MockitoJUnitRunner.class)
public class SourcepathMacroTest {
  @Mock private ClasspathContainer classpathContainer;
  @Mock private ClasspathResolver classpathResolver;
  @Mock private AppContext appContext;
  @Mock private PromiseProvider promises;

  @Mock private Resource resource;
  @Mock private Optional<Project> projectOptional;
  @Mock private Project project;
  @Mock private Promise<List<ClasspathEntry>> classpathEntriesPromise;

  @Captor private ArgumentCaptor<Function<List<ClasspathEntry>, String>> classpathEntriesCapture;

  @InjectMocks private SourcepathMacro sourcepathMacro;

  private Resource[] resources = new Resource[1];

  @Before
  public void setUp() throws Exception {
    resources[0] = resource;

    when(appContext.getResources()).thenReturn(resources);
    when(resource.getRelatedProject()).thenReturn(projectOptional);
    when(projectOptional.get()).thenReturn(project);

    Map<String, List<String>> attributes = new HashMap<>();
    attributes.put(Constants.LANGUAGE, singletonList("java"));

    when(project.getAttributes()).thenReturn(attributes);

    Path projectPath = Path.valueOf("/name");
    when(project.getLocation()).thenReturn(projectPath);
  }

  @Test
  public void sourcepathShouldBeBuiltWith2Libraries() throws Exception {
    String source1 = "/name/source1";
    String source2 = "/name/source2";

    List<ClasspathEntry> entries = new ArrayList<>();

    Set<String> sources = new HashSet<>();
    sources.add(source1);
    sources.add(source2);

    when(classpathContainer.getClasspathEntries(anyString())).thenReturn(classpathEntriesPromise);
    when(classpathResolver.getSources()).thenReturn(sources);

    sourcepathMacro.expand();

    verify(classpathEntriesPromise).then(classpathEntriesCapture.capture());
    String classpath = classpathEntriesCapture.getValue().apply(entries);

    verify(classpathResolver).resolveClasspathEntries(entries);
    assertEquals("source2:source1:", classpath);
  }

  @Test
  public void defaultValueOfSourcepathShouldBeBuilt() throws Exception {
    List<ClasspathEntry> entries = new ArrayList<>();
    Path projectsRoot = Path.valueOf("/projects");

    when(appContext.getProjectsRoot()).thenReturn(projectsRoot);
    when(classpathContainer.getClasspathEntries(anyString())).thenReturn(classpathEntriesPromise);

    sourcepathMacro.expand();

    verify(classpathEntriesPromise).then(classpathEntriesCapture.capture());
    String classpath = classpathEntriesCapture.getValue().apply(entries);

    verify(classpathResolver).resolveClasspathEntries(entries);
    assertEquals("/projects/name", classpath);
  }

  @Test
  public void keyOfTheSourcepathShouldBeReturned() throws Exception {
    assertEquals("${project.java.sourcepath}", sourcepathMacro.getName());
  }
}
