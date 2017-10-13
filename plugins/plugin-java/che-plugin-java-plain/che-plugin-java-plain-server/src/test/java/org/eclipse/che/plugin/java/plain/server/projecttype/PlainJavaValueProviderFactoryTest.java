/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.plain.server.projecttype;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.inject.Provider;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.project.server.impl.ProjectConfigRegistry;
import org.eclipse.che.api.project.server.impl.RegisteredProject;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * @author Valeriy Svydenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class PlainJavaValueProviderFactoryTest {

  public static final String PROJECT_PATH = "/project/path";
  @InjectMocks
  PlainJavaValueProviderFactory plainJavaValueProviderFactory;
  @Mock
  Map<String, List<String>> attributes;
  @Mock
  private Provider<ProjectConfigRegistry> projectRegistryProvider;
  @Mock
  private ProjectConfigRegistry projectConfigRegistry;
  @Mock
  private RegisteredProject registeredProject;
  @Mock
  private ProjectConfig projectConfig;
  @Captor
  private ArgumentCaptor<List<String>> captor;

  @BeforeMethod
  public void setUp() throws Exception {
  }

  @Test
  public void attributeShouldBeSet() throws Exception {
    when(projectConfig.getAttributes()).thenReturn(attributes);

    plainJavaValueProviderFactory
        .newInstance(projectConfig)
        .setValues(SOURCE_FOLDER, singletonList("src"));

    verify(attributes).put(SOURCE_FOLDER, singletonList("src"));
  }

  @Test
  public void newValueOfAttributeShouldBeAdded() throws Exception {
    when(projectConfig.getAttributes()).thenReturn(attributes);
    when(attributes.containsKey(SOURCE_FOLDER)).thenReturn(true);
    when(attributes.get(SOURCE_FOLDER)).thenReturn(Arrays.asList("src1", "src2"));

    plainJavaValueProviderFactory
        .newInstance(projectConfig)
        .setValues(SOURCE_FOLDER, singletonList("src3"));

    verify(attributes).put(eq(SOURCE_FOLDER), captor.capture());

    assertTrue(captor.getValue().size() == 3);
    assertTrue(captor.getValue().containsAll(Arrays.asList("src1", "src2", "src3")));
  }
}
