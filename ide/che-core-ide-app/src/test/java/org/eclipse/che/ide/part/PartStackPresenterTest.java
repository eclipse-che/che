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
package org.eclipse.che.ide.part;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.EditorDirtyStateChangedEvent;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStackView;
import org.eclipse.che.ide.api.parts.PropertyListener;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.part.widgets.TabItemFactory;
import org.eclipse.che.ide.part.PartStackPresenter.PartStackEventHandler;
import org.eclipse.che.ide.part.widgets.partbutton.PartButton;
import org.eclipse.che.ide.workspace.WorkBenchPartController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Roman Nikitenko
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class PartStackPresenterTest {

    private static final String SOME_TEXT = "someText";
    private static final double PART_SIZE = 170;

    //constructor mocks
    @Mock
    private EventBus                eventBus;
    @Mock
    private WorkBenchPartController workBenchPartController;
    @Mock
    private PartsComparator         partsComparator;
    @Mock
    private PartStackEventHandler   partStackHandler;
    @Mock
    private TabItemFactory          tabItemFactory;
    @Mock
    private PartStackView           view;
    @Mock
    private PropertyListener        propertyListener;

    //additional mocks
    @Mock
    private AcceptsOneWidget    container;
    @Mock
    private PartPresenter       partPresenter;
    @Mock
    private Constraints         constraints;
    @Mock
    private BasePresenter       basePresenter;
    @Mock
    private SVGResource         resource;
    @Mock
    private PartButton          partButton;
    @Mock
    private EditorPartPresenter editorPartPresenter;

    @Captor
    public ArgumentCaptor<PropertyListener> listenerCaptor;

    private PartStackPresenter presenter;

    @Before
    public void setUp() {
        when(basePresenter.getTitle()).thenReturn(SOME_TEXT);
        when(basePresenter.getTitleToolTip()).thenReturn(SOME_TEXT);
        when(basePresenter.getTitleImage()).thenReturn(resource);

        when(partPresenter.getTitle()).thenReturn(SOME_TEXT);
        when(partPresenter.getTitleToolTip()).thenReturn(SOME_TEXT);
        when(partPresenter.getTitleImage()).thenReturn(resource);

        when(tabItemFactory.createPartButton(SOME_TEXT)).thenReturn(partButton);
        when(partButton.setTooltip(SOME_TEXT)).thenReturn(partButton);
        when(partButton.setIcon(resource)).thenReturn(partButton);

        presenter = new PartStackPresenter(eventBus, partStackHandler, tabItemFactory, partsComparator, view, workBenchPartController);
    }

    @Test
    public void partShouldBeUpdated() {
        presenter.addPart(partPresenter);

        verify(partPresenter).addPropertyListener(listenerCaptor.capture());
        listenerCaptor.getValue().propertyChanged(partPresenter, PartPresenter.TITLE_PROPERTY);

        verify(view).updateTabItem(partPresenter);
    }

    @Test
    public void dirtyStateChangedEventShouldBeFired() {
        presenter.addPart(partPresenter);

        verify(partPresenter).addPropertyListener(listenerCaptor.capture());
        listenerCaptor.getValue().propertyChanged(editorPartPresenter, EditorPartPresenter.PROP_DIRTY);

        verify(eventBus).fireEvent(Matchers.<EditorDirtyStateChangedEvent>anyObject());
    }

    @Test
    public void goShouldBeActioned() {
        presenter.go(container);

        verify(container).setWidget(view);
    }

    @Test
    public void partShouldBeAddedWithoutConstraints() {
        presenter.addPart(basePresenter);

        verify(basePresenter).setPartStack(presenter);
        verify(tabItemFactory).createPartButton(SOME_TEXT);

        verify(partButton).setTooltip(SOME_TEXT);
        verify(partButton).setIcon(resource);

        verify(partButton).setDelegate(presenter);

        verify(view).addTab(partButton, basePresenter);
        verify(view).setTabPositions(Matchers.<List<PartPresenter>>anyObject());
        verify(partStackHandler).onRequestFocus(presenter);
    }

    @Test
    public void partShouldNotBeAddedWhenItAlreadyExist() {
        presenter.addPart(partPresenter);
        reset(view);

        presenter.addPart(partPresenter);

        verify(workBenchPartController).setHidden(true);

        verify(partButton).unSelect();

        verify(view, never()).addTab(partButton, partPresenter);
    }

    @Test
    public void partShouldBeContained() {
        presenter.addPart(basePresenter);

        boolean isContained = presenter.containsPart(basePresenter);

        assertThat(isContained, is(true));
    }

    @Test
    public void activePartShouldBeReturned() {
        presenter.addPart(partPresenter);

        presenter.setActivePart(partPresenter);

        assertThat(presenter.getActivePart(), sameInstance(partPresenter));
    }

    @Test
    public void focusShouldBeSet() {
        presenter.setFocus(true);

        verify(view).setFocus(true);
    }

    @Test
    public void activePartShouldNotBeSet() {
        presenter.setActivePart(partPresenter);

        assertThat(presenter.getActivePart(), nullValue());
    }

    @Test
    public void partShouldBeHidden() {
        presenter.addPart(partPresenter);

        presenter.minimize();

        verify(workBenchPartController).getSize();
        verify(workBenchPartController).setSize(0);

        assertThat(presenter.getActivePart(), nullValue());
    }

    @Test
    public void partShouldBeRemoved() {
        presenter.addPart(partPresenter);

        presenter.removePart(partPresenter);

        verify(view).removeTab(partPresenter);
    }

    @Test
    public void previousActivePartShouldNotBeDisplayedWhenActivePartIsNull() {
        presenter.openPreviousActivePart();

        verify(view, never()).selectTab(partPresenter);
    }

    @Test
    public void previousPartShouldBeOpened() {
        presenter.addPart(partPresenter);
        presenter.setActivePart(partPresenter);
        reset(view);

        presenter.openPreviousActivePart();

        verify(view).selectTab(partPresenter);
    }

    @Test
    public void requestShouldBeOnFocus() {
        presenter.onRequestFocus();

        verify(partStackHandler).onRequestFocus(presenter);
    }

    @Test
    public void onTabShouldBeClicked() {
        reset(workBenchPartController);
        presenter.addPart(partPresenter);

        presenter.onTabClicked(partButton);

        verify(workBenchPartController).setHidden(false);

        verify(view).selectTab(partPresenter);
    }

    @Test
    public void shouldSetCurrentSizeForPart() {
        reset(workBenchPartController);
        presenter.addPart(partPresenter);
        when(workBenchPartController.getSize()).thenReturn(0d);

        presenter.onTabClicked(partButton);

        verify(workBenchPartController).setHidden(false);

        verify(view).selectTab(partPresenter);
    }

    @Test
    public void shouldSetInitialSizeForPart() {
        reset(workBenchPartController);
        presenter.addPart(partPresenter);
        when(workBenchPartController.getSize()).thenReturn(PART_SIZE);

        presenter.onTabClicked(partButton);

        verify(workBenchPartController).setHidden(false);

        verify(view).selectTab(partPresenter);
    }
}
