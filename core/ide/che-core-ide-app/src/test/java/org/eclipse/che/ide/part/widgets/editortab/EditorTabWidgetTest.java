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
package org.eclipse.che.ide.part.widgets.editortab;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Element;
import com.google.gwtmockito.GwtMockitoTestRunner;

import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.part.editor.EditorTabContextMenuFactory;
import org.eclipse.che.ide.part.widgets.editortab.EditorTab.ActionDelegate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

import static org.eclipse.che.ide.api.parts.PartStackView.TabPosition.BELOW;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class EditorTabWidgetTest {

    private static final String SOME_TEXT = "someText";

    //constructor mocks
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PartStackUIResources resources;
    @Mock
    private SVGResource          icon;

    @Mock
    private SVGImage iconImage;

    //additional mocks
    @Mock
    private Element         element;
    @Mock
    private OMSVGSVGElement svg;
    @Mock
    private ActionDelegate  delegate;
    @Mock
    private ClickEvent      event;
    @Mock
    private VirtualFile     file;
    @Mock
    private EditorTabContextMenuFactory editorTabContextMenuFactory;
    @Mock
    private EventBus eventBus;

    private EditorTabWidget tab;

    @Before
    public void setUp() {
        when(icon.getSvg()).thenReturn(svg);
        when(event.getNativeButton()).thenReturn(NativeEvent.BUTTON_LEFT);

        tab = new EditorTabWidget(file, icon, SOME_TEXT, resources, editorTabContextMenuFactory, eventBus);
        tab.setDelegate(delegate);
    }

    @Test
    public void titleShouldBeReturned() {
        tab.getTitle();

        verify(tab.title).getText();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void exceptionShouldBeThrownWhenTrySetTabPosition() {
        tab.setTabPosition(BELOW);
    }

    @Test
    public void errorMarkShouldBeSet() {
        when(resources.partStackCss().lineError()).thenReturn(SOME_TEXT);

        tab.setErrorMark(true);

        verify(resources.partStackCss()).lineError();
        verify(tab.title).addStyleName(SOME_TEXT);
    }

    @Test
    public void errorMarkShouldNotBeSet() {
        when(resources.partStackCss().lineError()).thenReturn(SOME_TEXT);

        tab.setErrorMark(false);

        verify(resources.partStackCss()).lineError();
        verify(tab.title).removeStyleName(SOME_TEXT);
    }

    @Test
    public void warningMarkShouldBeSet() {
        when(resources.partStackCss().lineWarning()).thenReturn(SOME_TEXT);

        tab.setWarningMark(true);

        verify(resources.partStackCss()).lineWarning();
        verify(tab.title).addStyleName(SOME_TEXT);
    }

    @Test
    public void warningMarkShouldNotBeSet() {
        when(resources.partStackCss().lineWarning()).thenReturn(SOME_TEXT);

        tab.setWarningMark(false);

        verify(resources.partStackCss()).lineWarning();
        verify(tab.title).removeStyleName(SOME_TEXT);
    }

    @Test
    public void onTabShouldBeClicked() {
        tab.onClick(event);

        verify(delegate).onTabClicked(tab);
    }

    @Test
    public void onCloseButtonShouldBeClicked() {
        tab.onCloseButtonClicked(event);

        verify(delegate).onTabClose(tab);
    }

}
