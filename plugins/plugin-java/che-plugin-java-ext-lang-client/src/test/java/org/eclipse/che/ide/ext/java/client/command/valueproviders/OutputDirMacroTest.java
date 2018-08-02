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
import static org.eclipse.che.ide.ext.java.shared.Constants.LANGUAGE;
import static org.eclipse.che.ide.ext.java.shared.Constants.OUTPUT_FOLDER;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.resource.Path;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** @author Valeriy Svydenko */
@RunWith(GwtMockitoTestRunner.class)
public class OutputDirMacroTest {
  @InjectMocks private OutputDirMacro provider;

  @Mock private AppContext appContext;
  @Mock private PromiseProvider promises;

  @Mock private Resource resource1;
  @Mock private Resource resource2;
  @Mock private Optional<Project> projectOptional;
  @Mock private Project relatedProject;

  @Captor private ArgumentCaptor<Promise<String>> valuePromiseCaptor;

  private Resource[] resources;
  private Map<String, List<String>> attributes = new HashMap<>();

  @Before
  public void setUp() throws Exception {
    resources = new Resource[] {resource1};
    attributes.put(LANGUAGE, singletonList("java"));

    when(appContext.getResources()).thenReturn(resources);
    when(resource1.getRelatedProject()).thenReturn(projectOptional);
    when(projectOptional.isPresent()).thenReturn(true);
    when(projectOptional.get()).thenReturn(relatedProject);
    when(appContext.getProjectsRoot()).thenReturn(new Path("/projects"));
    when(relatedProject.getLocation()).thenReturn(new Path("projectParent/project"));
    when(relatedProject.getAttributes()).thenReturn(attributes);
  }

  @Test
  public void keyShouldBeReturned() throws Exception {
    assertEquals("${project.java.output.dir}", provider.getName());
  }

  @Test
  public void valueShouldBeEmptyIfSelectedResourcesIsNull() throws Exception {
    resources = null;
    when(appContext.getResources()).thenReturn(resources);

    provider.expand();

    verify(promises).resolve(eq(""));
  }

  @Test
  public void valueShouldBeEmptyIfSelectedManyResources() throws Exception {
    resources = new Resource[] {resource1, resource2};
    when(appContext.getResources()).thenReturn(resources);

    provider.expand();

    verify(promises).resolve(eq(""));
  }

  @Test
  public void valueShouldBeEmptyIfRelatedProjectOfSelectedResourceIsNull() throws Exception {
    when(projectOptional.isPresent()).thenReturn(false);

    provider.expand();

    verify(promises).resolve(eq(""));
  }

  @Test
  public void valueShouldBeEmptyIfRelatedProjectIsNotJavaProject() throws Exception {
    attributes.put(LANGUAGE, singletonList("cpp"));
    when(relatedProject.getAttributes()).thenReturn(attributes);

    provider.expand();

    verify(promises).resolve(eq(""));
  }

  @Test
  public void outputFolderShouldBeRootOfProjectIfAttributeDoesNotExist() throws Exception {
    provider.expand();

    verify(promises).resolve(eq("/projects/projectParent/project"));
  }

  @Test
  public void outputFolderShouldBeSetAsValueOfAttribute() throws Exception {
    attributes.put(OUTPUT_FOLDER, singletonList("bin"));

    provider.expand();

    verify(promises).resolve(eq("/projects/projectParent/project/bin"));
  }
}
