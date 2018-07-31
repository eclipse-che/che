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
package org.eclipse.che.ide.part.perspectives.general;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.api.parts.Perspective;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.api.parts.PerspectiveManager.PerspectiveTypeListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** @author Dmitry Shnurenko */
@RunWith(MockitoJUnitRunner.class)
public class PerspectiveManagerTest {

  @Mock private PerspectiveTypeListener typeListener;
  @Mock private Perspective projectPerspective;
  @Mock private Perspective machinePerspective;

  private PerspectiveManager manager;

  @Before
  public void setUp() {
    Map<String, Perspective> perspectives = new HashMap<>();

    perspectives.put("Machine Perspective", machinePerspective);
    perspectives.put("Project Perspective", projectPerspective);

    manager = new PerspectiveManager(perspectives, "Project Perspective");
  }

  @Test
  public void defaultPerspectiveShouldBeReturned() {
    Perspective perspective = manager.getActivePerspective();

    assertThat(perspective, sameInstance(projectPerspective));
  }

  @Test
  public void perspectiveIdShouldBeSet() {
    manager.addListener(typeListener);

    manager.setPerspectiveId("Machine Perspective");

    verify(projectPerspective).storeState();

    verify(typeListener).onPerspectiveChanged();

    assertThat(manager.getActivePerspective(), equalTo(machinePerspective));
    assertThat(manager.getPerspectiveId(), equalTo("Machine Perspective"));
  }
}
