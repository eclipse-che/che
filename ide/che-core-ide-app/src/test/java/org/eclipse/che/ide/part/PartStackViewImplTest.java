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
package org.eclipse.che.ide.part;

import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.PartStackView;
import org.eclipse.che.ide.api.parts.PartStackView.ActionDelegate;
import org.eclipse.che.ide.api.parts.PartStackView.TabItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.util.Arrays;

import static org.eclipse.che.ide.api.parts.PartStackView.TabPosition.BELOW;
import static org.eclipse.che.ide.api.parts.PartStackView.TabPosition.LEFT;
import static org.eclipse.che.ide.api.parts.PartStackView.TabPosition.RIGHT;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class PartStackViewImplTest {

    private static final String SOME_TEXT = "someText";

    @Mock(answer = RETURNS_DEEP_STUBS)
    PartStackUIResources resources;
    @Mock
    DeckLayoutPanel      contentPanel;
    @Mock
    FlowPanel            tabsPanel;

    //additional mocks
    @Mock
    private MouseDownEvent   event;
    @Mock
    private ContextMenuEvent contextMenuEvent;
    @Mock
    private ActionDelegate   delegate;
    @Mock
    private TabItem          tabItem;
    @Mock
    private TabItem          tabItem2;
    @Mock
    private PartPresenter    partPresenter;
    @Mock
    private PartPresenter    partPresenter2;
    @Mock
    private IsWidget         widget;
    @Mock
    private IsWidget         widget2;
    @Mock
    private Widget           focusedWidget;
    @Mock
    private Element          element;

    @Captor
    private ArgumentCaptor<AcceptsOneWidget> contentCaptor;

    private PartStackViewImpl view;

    @Before
    public void setUp() {
        when(focusedWidget.getElement()).thenReturn(element);
        when(tabItem.getView()).thenReturn(widget);
        when(partPresenter.getView()).thenReturn(widget);

        when(resources.partStackCss().idePartStackContent()).thenReturn(SOME_TEXT);

        view = new PartStackViewImpl(resources, contentPanel, BELOW, tabsPanel);
        view.setDelegate(delegate);
    }

    @Test
    public void constructorShouldBeVerifiedInPositionBelow() {
        verify(contentPanel).setStyleName(SOME_TEXT);

        verifyNoMoreInteractions(tabsPanel);
    }

    @Test
    public void constructorShouldBeVerifiedInPositionLeft() {
        when(resources.partStackCss().idePartStackContent()).thenReturn(SOME_TEXT);

        reset(contentPanel);
        reset(tabsPanel);
        view = new PartStackViewImpl(resources, contentPanel, LEFT, tabsPanel);

        verify(contentPanel).setStyleName(SOME_TEXT);
    }

    @Test
    public void constructorShouldBeVerifiedInPositionRight() {
        when(resources.partStackCss().idePartStackContent()).thenReturn(SOME_TEXT);

        reset(contentPanel);
        reset(tabsPanel);
        view = new PartStackViewImpl(resources, contentPanel, RIGHT, tabsPanel);

        verify(contentPanel).setStyleName(SOME_TEXT);
    }

    @Test
    public void onPartStackMouseShouldBeDown() {
        view.onMouseDown(event);

        verify(delegate).onRequestFocus();
    }

    @Test
    public void onPartStackContextMenuShouldBeClicked() {
        view.onContextMenu(contextMenuEvent);

        verify(delegate).onRequestFocus();
    }

    @Test
    public void tabShouldBeAdded() {
        view.addTab(tabItem, partPresenter);

        verify(tabItem).setTabPosition(BELOW);
        verify(tabsPanel).add(widget);
        verify(partPresenter).go(contentCaptor.capture());

        contentCaptor.getValue().setWidget(widget);

        verify(contentPanel).add(widget);
    }

    @Test
    public void tabShouldBeRemoved() {
        view.addTab(tabItem, partPresenter);

        view.removeTab(partPresenter);

        verify(tabsPanel).remove(widget);
        verify(contentPanel).remove(widget);
    }

    @Test
    public void tabPositionsShouldBeSet() {
        view.addTab(tabItem, partPresenter);
        view.addTab(tabItem2, partPresenter2);

        when(partPresenter2.getView()).thenReturn(widget2);

        view.setTabPositions(Arrays.asList(partPresenter, partPresenter2));

        verify(tabsPanel).insert(widget, 0);
    }

    @Test
    public void tabShouldBeSelectedWhenContentExist() {
        view.addTab(tabItem, partPresenter);

        view.selectTab(partPresenter);

        verify(contentPanel).getWidgetIndex(widget);
        verify(contentPanel).showWidget(0);
        verify(tabItem).select();
        verify(delegate).onRequestFocus();
        verify(tabItem).setTabPosition((PartStackView.TabPosition)any());
    }

    @Test
    public void tabShouldBeSelectedWhenContentIsAbsent() {
        view.addTab(tabItem, partPresenter);

        view.selectTab(partPresenter);

        verify(contentPanel).getWidgetIndex(widget);
        verify(partPresenter).go(contentCaptor.capture());
        verify(contentPanel).showWidget(0);
        verify(tabItem).select();
        verify(delegate).onRequestFocus();
    }

    @Test
    public void partShouldBeFocused() {
        when(contentPanel.getVisibleWidget()).thenReturn(focusedWidget);

        view.setFocus(true);

        verify(contentPanel).getVisibleWidget();

        verify(element).setAttribute("focused", "");
    }

    @Test
    public void partShouldNotBeFocused() {
        when(contentPanel.getVisibleWidget()).thenReturn(focusedWidget);

        view.setFocus(false);

        verify(contentPanel).getVisibleWidget();

        verify(focusedWidget, never()).getElement();
    }

    @Test
    public void tabItemShouldBeUpdated() {
        view.addTab(tabItem, partPresenter);

        view.updateTabItem(partPresenter);

        verify(tabItem).update(partPresenter);
    }
}
