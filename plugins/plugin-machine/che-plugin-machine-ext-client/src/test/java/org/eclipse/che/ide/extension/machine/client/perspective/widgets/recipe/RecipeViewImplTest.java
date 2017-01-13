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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.verify;

/**
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class RecipeViewImplTest {
    @Mock(answer = RETURNS_DEEP_STUBS)
    private org.eclipse.che.ide.Resources resources;
    @Mock
    private Widget                        recipe;

    RecipesPartViewImpl view;

    @Before
    public void setUp() throws Exception {
        view = new RecipesPartViewImpl(resources);
    }

    @Test
    public void recipeShouldBeAdded() throws Exception {
        view.addRecipe(recipe);

        verify(view.widgets).add(recipe);
    }

    @Test
    public void recipeShouldBeRemoved() throws Exception {
        view.removeRecipe(recipe);

        verify(view.widgets).remove(recipe);
    }

    @Test
    public void panelShouldBeCleared() throws Exception {
        view.clear();

        verify(view.widgets).clear();
    }
}
