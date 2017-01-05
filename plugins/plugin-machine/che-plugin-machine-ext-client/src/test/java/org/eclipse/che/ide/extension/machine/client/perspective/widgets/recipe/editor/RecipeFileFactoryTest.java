/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor;

import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.RecipeFileFactory.NAME;
import static org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.RecipeFileFactory.PATH;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(MockitoJUnitRunner.class)
public class RecipeFileFactoryTest {

    private static final String SOME_TEXT = "someText";

    @Captor
    private ArgumentCaptor<List<Link>> linkListCaptor;

    //constructor mocks
    @Mock
    private EventBus               eventBus;
    @Mock
    private DtoUnmarshallerFactory dtoUnmarshallerFactory;
    @Mock
    private DtoFactory             dtoFactory;
    @Mock
    private AppContext             appContext;
    @Mock
    Provider<EditorAgent> editorAgentProvider;

    //additional mocks
    @Mock
    private ItemReference  itemReference;

    @InjectMocks
    private RecipeFileFactory factory;

    @Before
    public void setUp() throws Exception {
        when(dtoFactory.createDto(ItemReference.class)).thenReturn(itemReference);
        when(itemReference.withName(NAME)).thenReturn(itemReference);
        when(itemReference.withPath(PATH)).thenReturn(itemReference);
    }

    @Test
    public void dockerFileShouldBeCreated() throws Exception {
        VirtualFile fileNode = factory.newInstance(SOME_TEXT);

        assertThat(fileNode, notNullValue());
        assertThat(fileNode, instanceOf(VirtualFile.class));
    }
}
