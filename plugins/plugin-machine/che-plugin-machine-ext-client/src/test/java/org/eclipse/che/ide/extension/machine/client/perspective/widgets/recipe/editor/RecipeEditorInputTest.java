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

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.vectomatic.dom.svg.ui.SVGResource;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class RecipeEditorInputTest {

    private static final String TEXT = "some text";

    //mocks for constructor
    @Mock
    private FileType    fileType;
    @Mock
    private VirtualFile file;

    @Mock
    private SVGResource   svgImage;

    @InjectMocks
    private RecipeEditorInput recipeEditorInput;

    @Before
    public void setUp() {
        when(file.getDisplayName()).thenReturn(TEXT);
        when(fileType.getImage()).thenReturn(svgImage);
    }

    @Test
    public void ToolTipTextShouldBeReturned() {
        assertThat(recipeEditorInput.getToolTipText(), is(""));
    }

    @Test
    public void nameShouldBeReturned() {
        assertThat(recipeEditorInput.getName(), is(TEXT));

        verify(file).getDisplayName();
    }

    @Test
    public void svgResourcesShouldBeReturned() {
        assertThat(recipeEditorInput.getSVGResource(), is(svgImage));

        verify(fileType).getImage();
    }

    @Test
    public void fileShouldBeChanged() {
        VirtualFile file1 = mock(VirtualFile.class);

        assertThat(recipeEditorInput.getFile(), is(file));

        recipeEditorInput.setFile(file1);

        assertThat(recipeEditorInput.getFile(), is(file1));
    }
}
