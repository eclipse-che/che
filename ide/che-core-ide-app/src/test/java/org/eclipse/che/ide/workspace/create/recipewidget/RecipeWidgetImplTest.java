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
package org.eclipse.che.ide.workspace.create.recipewidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Element;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.ide.workspace.create.recipewidget.RecipeWidget.ActionDelegate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.ui.SVGResource;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class RecipeWidgetImplTest {

    @Mock
    private RecipeDescriptor              descriptor;
    @Mock
    private org.eclipse.che.ide.Resources resources;

    @Mock
    private Element         element;
    @Mock
    private SVGResource     svgResource;
    @Mock
    private OMSVGSVGElement svg;
    @Mock
    private ActionDelegate  delegate;

    private RecipeWidgetImpl tag;

    @Before
    public void setUp() {
        when(resources.recipe()).thenReturn(svgResource);
        when(svgResource.getSvg()).thenReturn(svg);

        tag = new RecipeWidgetImpl(resources, descriptor);

        when(tag.icon.getElement()).thenReturn(element);
    }

    @Test
    public void constructorShouldVerified() {
        verify(descriptor).getName();
        verify(descriptor).getType();

        verify(tag.tagName).setText(anyString());
        verify(tag.type).setText(anyString());

        verify(resources).recipe();
        verify(tag.icon).getElement();
    }

    @Test
    public void recipeURLShouldBeReturned() {
        Link link = mock(Link.class);
        when(descriptor.getLink(anyString())).thenReturn(link);

        tag.getRecipeUrl();

        verify(descriptor).getLink("get recipe script");
        verify(link).getHref();
    }

    @Test
    public void tagShouldBeSelected() {
        ClickEvent clickEvent = mock(ClickEvent.class);
        tag.setDelegate(delegate);

        tag.onClick(clickEvent);

        verify(delegate).onTagClicked(tag);
    }

}