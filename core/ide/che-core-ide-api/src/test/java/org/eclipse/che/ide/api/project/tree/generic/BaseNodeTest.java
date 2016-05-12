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
package org.eclipse.che.ide.api.project.tree.generic;

import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.ide.api.project.tree.TreeSettings;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import com.google.web.bindery.event.shared.EventBus;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public abstract class BaseNodeTest {
    @Mock
    protected EventBus               eventBus;
    @Mock
    protected ProjectServiceClient   projectServiceClient;
    @Mock
    protected DtoUnmarshallerFactory dtoUnmarshallerFactory;
    @Mock
    protected GenericTreeStructure   treeStructure;
    @Mock
    protected TreeSettings           treeSettings;

    @Before
    public void setUp() {
        when(treeStructure.getSettings()).thenReturn(treeSettings);
    }
}
