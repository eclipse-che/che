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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.entry;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.MachineResources.Css;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class RecipeWidgetTest {
    private final String SOME_TEXT = "text";

    @Mock(answer = RETURNS_DEEP_STUBS)
    private MachineResources resources;
    @Mock
    private RecipeDescriptor recipeDescriptor;
    @Mock
    private Css              css;

    RecipeWidget recipeWidget;

    @Before
    public void setUp() throws Exception {
        when(recipeDescriptor.getName()).thenReturn("name");

        recipeWidget = new RecipeWidget(recipeDescriptor, resources);
    }

    @Test
    public void constructorShouldBePerformed() throws Exception {
        verify(recipeWidget.name).setText("name");
        verify(resources).recipe();
    }

    @Test
    public void descriptorShouldBeReturned() throws Exception {
        assertEquals(recipeDescriptor, recipeWidget.getDescriptor());
    }

    @Test
    public void elementShouldBeSelected() throws Exception {
        when(resources.getCss()).thenReturn(css);
        when(css.selectRecipe()).thenReturn(SOME_TEXT);
        when(css.unSelectRecipe()).thenReturn(SOME_TEXT);

        recipeWidget.select();

        verify(recipeWidget.main).addStyleName(SOME_TEXT);
        verify(recipeWidget.main).removeStyleName(SOME_TEXT);
    }

    @Test
    public void elementShouldBeUnSelected() throws Exception {
        when(resources.getCss()).thenReturn(css);
        when(css.selectRecipe()).thenReturn(SOME_TEXT);
        when(css.unSelectRecipe()).thenReturn(SOME_TEXT);

        recipeWidget.unSelect();

        verify(recipeWidget.main).addStyleName(SOME_TEXT);
        verify(recipeWidget.main).removeStyleName(SOME_TEXT);
    }
}