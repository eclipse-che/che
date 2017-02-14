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
package org.eclipse.che.ide.extension.machine.client.util;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.entry.RecipeWidget;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class NameGeneratorTest {

    private static final String HELLO       = "hello";
    private static final String COPY_HELLO  = "Copy of hello";
    private static final String COPY2_HELLO = "Copy2 of hello";
    private static final String COPY3_HELLO = "Copy3 of hello";
    private static final String COPY4_HELLO = "Copy4 of hello";

    @Mock
    private RecipeWidget recipe1;
    @Mock
    private RecipeDescriptor recipeDescriptor1;
    @Mock
    private RecipeWidget recipe2;
    @Mock
    private RecipeDescriptor recipeDescriptor2;
    @Mock
    private RecipeWidget recipe3;
    @Mock
    private RecipeDescriptor recipeDescriptor3;

    /**
     * First copy is named 'copy of'
     */
    @Test
    public void generateFirstName() {
        when(recipe1.getDescriptor()).thenReturn(recipeDescriptor1);
        when(recipe1.getDescriptor().getName()).thenReturn(HELLO);

        String generated = NameGenerator.generateCopy(HELLO, new HashSet<>(Collections.singletonList(recipe1)));
        String expectedName = "Copy of hello";
        assertEquals(expectedName, generated);
    }

    /**
     * Second copy is named 'copy2 of ...'
     */
    @Test
    public void generateAlreadyExistsFirstName() {
        when(recipe1.getDescriptor()).thenReturn(recipeDescriptor1);
        when(recipe1.getDescriptor().getName()).thenReturn(COPY_HELLO);

        String generated = NameGenerator.generateCopy(HELLO, new HashSet<>(Collections.singletonList(recipe1)));

        assertEquals(COPY2_HELLO, generated);
    }

    /**
     * Third copy is named 'copy of ... rev3'
     */
    @Test
    public void generateAlreadyExistsTwiceName() {
        when(recipe1.getDescriptor()).thenReturn(recipeDescriptor1);
        when(recipe2.getDescriptor()).thenReturn(recipeDescriptor2);
        when(recipe1.getDescriptor().getName()).thenReturn(COPY_HELLO);
        when(recipe2.getDescriptor().getName()).thenReturn(COPY2_HELLO);

        String generated = NameGenerator.generateCopy(HELLO, new HashSet<>(Arrays.asList(recipe1, recipe2)));

        assertEquals(COPY3_HELLO, generated);
    }

    /**
     * Copying a copy should result in a new increment of a copy, not copy of copy
     */
    @Test
    public void generateCopyOfCopy() {
        when(recipe1.getDescriptor()).thenReturn(recipeDescriptor1);
        when(recipe2.getDescriptor()).thenReturn(recipeDescriptor2);
        when(recipe3.getDescriptor()).thenReturn(recipeDescriptor3);
        when(recipe1.getDescriptor().getName()).thenReturn(COPY_HELLO);
        when(recipe2.getDescriptor().getName()).thenReturn(COPY2_HELLO);
        when(recipe3.getDescriptor().getName()).thenReturn(COPY3_HELLO);

        String generated = NameGenerator.generateCopy(COPY3_HELLO, new HashSet<>(Arrays.asList(recipe1, recipe2, recipe3)));

        assertEquals(COPY4_HELLO, generated);
    }

    @Test
    public void generateCustomRecipeName() throws Exception {
        String newName = "RECIPE-3";

        when(recipe1.getDescriptor()).thenReturn(recipeDescriptor1);
        when(recipe2.getDescriptor()).thenReturn(recipeDescriptor2);
        when(recipeDescriptor1.getName()).thenReturn("RECIPE-1");
        when(recipeDescriptor2.getName()).thenReturn("RECIPE-2");
        HashSet<RecipeWidget> recipes = new HashSet<>(Arrays.asList(recipe1, recipe2));

        String generated = NameGenerator.generateCustomRecipeName(recipes);

        assertEquals(newName, generated);
    }

    @Test
    public void generateCustomRecipeName2() throws Exception {
        String newName = "RECIPE-2";

        when(recipe1.getDescriptor()).thenReturn(recipeDescriptor1);
        when(recipe2.getDescriptor()).thenReturn(recipeDescriptor2);
        when(recipe1.getDescriptor().getName()).thenReturn("RECIPE-1");
        when(recipe2.getDescriptor().getName()).thenReturn("RECIPE-3");

        HashSet<RecipeWidget> recipes = new HashSet<>(Arrays.asList(recipe1, recipe2));

        String generated = NameGenerator.generateCustomRecipeName(recipes);

        assertEquals(newName, generated);
    }

    @Test
    public void generateCustomRecipeName3() throws Exception {
        String newName = "RECIPE-11";

        RecipeWidget environment3 = mock(RecipeWidget.class);
        RecipeWidget environment4 = mock(RecipeWidget.class);
        RecipeWidget environment5 = mock(RecipeWidget.class);
        RecipeWidget environment6 = mock(RecipeWidget.class);
        RecipeWidget environment7 = mock(RecipeWidget.class);
        RecipeWidget environment8 = mock(RecipeWidget.class);
        RecipeWidget environment9 = mock(RecipeWidget.class);
        RecipeWidget environment10 = mock(RecipeWidget.class);

        RecipeDescriptor recipeDescriptor3 = mock(RecipeDescriptor.class);
        RecipeDescriptor recipeDescriptor4 = mock(RecipeDescriptor.class);
        RecipeDescriptor recipeDescriptor5 = mock(RecipeDescriptor.class);
        RecipeDescriptor recipeDescriptor6 = mock(RecipeDescriptor.class);
        RecipeDescriptor recipeDescriptor7 = mock(RecipeDescriptor.class);
        RecipeDescriptor recipeDescriptor8 = mock(RecipeDescriptor.class);
        RecipeDescriptor recipeDescriptor9 = mock(RecipeDescriptor.class);
        RecipeDescriptor recipeDescriptor10 = mock(RecipeDescriptor.class);

        when(recipe1.getDescriptor()).thenReturn(recipeDescriptor1);
        when(recipe2.getDescriptor()).thenReturn(recipeDescriptor2);
        when(environment3.getDescriptor()).thenReturn(recipeDescriptor3);
        when(environment4.getDescriptor()).thenReturn(recipeDescriptor4);
        when(environment5.getDescriptor()).thenReturn(recipeDescriptor5);
        when(environment6.getDescriptor()).thenReturn(recipeDescriptor6);
        when(environment7.getDescriptor()).thenReturn(recipeDescriptor7);
        when(environment8.getDescriptor()).thenReturn(recipeDescriptor8);
        when(environment9.getDescriptor()).thenReturn(recipeDescriptor9);
        when(environment10.getDescriptor()).thenReturn(recipeDescriptor10);


        when(recipeDescriptor1.getName()).thenReturn("RECIPE-1");
        when(recipeDescriptor2.getName()).thenReturn("RECIPE-2");
        when(recipeDescriptor3.getName()).thenReturn("RECIPE-3");
        when(recipeDescriptor4.getName()).thenReturn("RECIPE-4");
        when(recipeDescriptor5.getName()).thenReturn("RECIPE-5");
        when(recipeDescriptor6.getName()).thenReturn("RECIPE-6");
        when(recipeDescriptor7.getName()).thenReturn("RECIPE-7");
        when(recipeDescriptor8.getName()).thenReturn("RECIPE-8");
        when(recipeDescriptor9.getName()).thenReturn("RECIPE-9");
        when(recipeDescriptor10.getName()).thenReturn("RECIPE-10");

        HashSet<RecipeWidget> recipes = new HashSet<>(Arrays.asList(recipe1,
                                                       recipe2,
                                                       environment3,
                                                       environment4,
                                                       environment5,
                                                       environment6,
                                                       environment7,
                                                       environment8,
                                                       environment9,
                                                       environment10));

        String generated = NameGenerator.generateCustomRecipeName(recipes);

        assertEquals(newName, generated);
    }
}