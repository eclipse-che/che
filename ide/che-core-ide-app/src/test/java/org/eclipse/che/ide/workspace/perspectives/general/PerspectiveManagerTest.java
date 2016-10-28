/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.workspace.perspectives.general;

import org.eclipse.che.ide.api.parts.Perspective;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.api.parts.PerspectiveManager.PerspectiveTypeListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class PerspectiveManagerTest {

    @Mock
    private PerspectiveTypeListener typeListener;
    @Mock
    private Perspective             projectPerspective;
    @Mock
    private Perspective             machinePerspective;

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