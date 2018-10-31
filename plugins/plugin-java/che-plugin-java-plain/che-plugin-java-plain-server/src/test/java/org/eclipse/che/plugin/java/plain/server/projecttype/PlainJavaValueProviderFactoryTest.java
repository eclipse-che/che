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
package org.eclipse.che.plugin.java.plain.server.projecttype;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.shared.RegisteredProject;
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
  @Mock private EventService eventService;
  @Captor private ArgumentCaptor<List<String>> captor;

  @Test
  public void attributeShouldBeSet() throws Exception {
    when(registeredProject.getAttributes()).thenReturn(attributes);

    registeredProject.getAttributes().put(SOURCE_FOLDER, Arrays.asList("src"));

    verify(attributes).put(SOURCE_FOLDER, singletonList("src"));
  }
}
