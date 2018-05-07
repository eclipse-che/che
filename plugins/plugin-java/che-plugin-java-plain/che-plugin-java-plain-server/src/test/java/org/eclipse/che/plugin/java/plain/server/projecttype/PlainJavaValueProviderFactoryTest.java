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
package org.eclipse.che.plugin.java.plain.server.projecttype;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.impl.RegisteredProject;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Valeriy Svydenko */
@Listeners(value = {MockitoTestNGListener.class})
public class PlainJavaValueProviderFactoryTest {

  private static final String PROJECT_PATH = "ws/path";

  @InjectMocks private PlainJavaValueProviderFactory plainJavaValueProviderFactory;
  @Mock private Map<String, List<String>> attributes;
  @Mock private ProjectManager projectManager;
  @Mock private RegisteredProject registeredProject;
  @Captor private ArgumentCaptor<List<String>> captor;

  @Test
  public void attributeShouldBeSet() throws Exception {
    when(projectManager.get(PROJECT_PATH)).thenReturn(Optional.of(registeredProject));
    when(registeredProject.getAttributes()).thenReturn(attributes);
    when(registeredProject.getPath()).thenReturn(PROJECT_PATH);

    registeredProject.getAttributes().put(SOURCE_FOLDER, Arrays.asList("src"));

    verify(attributes).put(SOURCE_FOLDER, singletonList("src"));
  }
}
