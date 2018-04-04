/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.part.editor;

import static org.eclipse.che.ide.part.editor.actions.EditorAbstractAction.CURRENT_FILE_PROP;
import static org.eclipse.che.ide.part.editor.actions.EditorAbstractAction.CURRENT_PANE_PROP;
import static org.eclipse.che.ide.part.editor.actions.EditorAbstractAction.CURRENT_TAB_PROP;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.actions.EditorActions;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.AbstractEditorPresenter;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorWithErrors;
import org.eclipse.che.ide.api.parts.EditorTab;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStackView.TabItem;
import org.eclipse.che.ide.api.parts.PropertyListener;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.menu.PartMenu;
import org.eclipse.che.ide.part.PartStackPresenter.PartStackEventHandler;
import org.eclipse.che.ide.part.PartsComparator;
import org.eclipse.che.ide.part.editor.actions.CloseAllTabsPaneAction;
import org.eclipse.che.ide.part.editor.actions.ClosePaneAction;
import org.eclipse.che.ide.part.editor.actions.SplitHorizontallyAction;
import org.eclipse.che.ide.part.editor.actions.SplitVerticallyAction;
import org.eclipse.che.ide.part.widgets.TabItemFactory;
import org.eclipse.che.ide.part.widgets.panemenu.EditorPaneMenu;
import org.eclipse.che.ide.part.widgets.panemenu.EditorPaneMenuItem;
import org.eclipse.che.ide.part.widgets.panemenu.EditorPaneMenuItemFactory;
import org.eclipse.che.ide.part.widgets.panemenu.PaneMenuActionItemWidget;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.toolbar.PresentationFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Dmitry Shnurenko */
@RunWith(GwtMockitoTestRunner.class)
public class EditorPartStackPresenterTest {

  private static final String SOME_TEXT = "someText";

  // constructor mocks
  @Mock private EditorPartStackView view;
  @Mock private AppContext appContext;
  @Mock private PartMenu partMenu;
  @Mock private PartsComparator partsComparator;
  @Mock private EventBus eventBus;
  @Mock private TabItemFactory tabItemFactory;
  @Mock private PartStackEventHandler partStackEventHandler;
  @Mock private EditorPaneMenu editorPaneMenu;
  @Mock private Provider<EditorAgent> editorAgentProvider;
  @Mock private PresentationFactory presentationFactory;
  @Mock private ActionManager actionManager;
  @Mock private ClosePaneAction closePaneAction;
  @Mock private CloseAllTabsPaneAction closeAllTabsPaneAction;
  @Mock private EditorPaneMenuItemFactory editorPaneMenuItemFactory;
  @Mock private EditorAgent editorAgent;
  @Mock private AddEditorTabMenuFactory addEditorTabMenuFactory;

  // additional mocks
  @Mock private SplitHorizontallyAction splitHorizontallyAction;
  @Mock private SplitVerticallyAction splitVerticallyAction;
  @Mock private Presentation presentation;
  @Mock private EditorTab editorTab1;
  @Mock private EditorTab editorTab2;
  @Mock private EditorTab editorTab3;
  @Mock private EditorWithErrors withErrorsPart;
  @Mock private AbstractEditorPresenter partPresenter1;
  @Mock private AbstractEditorPresenter partPresenter2;
  @Mock private AbstractEditorPresenter partPresenter3;
  @Mock private EditorPaneMenuItem<Action> editorPaneActionMenuItem;
  @Mock private EditorPaneMenuItem<TabItem> editorPaneTabMenuItem;
  @Mock private SVGResource resource1;
  @Mock private SVGResource resource2;
  @Mock private ProjectConfigDto descriptor;
  @Mock private EditorPartPresenter editorPartPresenter;
  @Mock private EditorInput editorInput1;
  @Mock private EditorInput editorInput2;
  @Mock private EditorInput editorInput3;
  @Mock private VirtualFile file1;
  @Mock private VirtualFile file2;
  @Mock private VirtualFile file3;
  @Mock private HandlerRegistration handlerRegistration;

  @Captor private ArgumentCaptor<EditorPaneMenuItem> itemCaptor;
  @Captor private ArgumentCaptor<AsyncCallback<Void>> argumentCaptor;

  private EditorPartStackPresenter presenter;

  @Before
  public void setUp() {
    when(partPresenter1.getTitle()).thenReturn(SOME_TEXT);
    when(partPresenter1.getTitleImage()).thenReturn(resource1);

    when(partPresenter2.getTitle()).thenReturn(SOME_TEXT);
    when(partPresenter2.getTitleImage()).thenReturn(resource2);

    when(partPresenter1.getEditorInput()).thenReturn(editorInput1);
    when(editorInput1.getFile()).thenReturn(file1);

    when(partPresenter2.getEditorInput()).thenReturn(editorInput2);
    when(editorInput2.getFile()).thenReturn(file2);

    when(partPresenter3.getEditorInput()).thenReturn(editorInput3);
    when(editorInput3.getFile()).thenReturn(file3);

    when(presentationFactory.getPresentation(nullable(BaseAction.class))).thenReturn(presentation);

    when(eventBus.addHandler(any(), any())).thenReturn(handlerRegistration);

    when(actionManager.getAction(EditorActions.SPLIT_HORIZONTALLY))
        .thenReturn(splitHorizontallyAction);
    when(actionManager.getAction(EditorActions.SPLIT_VERTICALLY)).thenReturn(splitVerticallyAction);

    when(closePaneAction.getTemplatePresentation()).thenReturn(presentation);
    when(closeAllTabsPaneAction.getTemplatePresentation()).thenReturn(presentation);
    when(splitHorizontallyAction.getTemplatePresentation()).thenReturn(presentation);
    when(splitVerticallyAction.getTemplatePresentation()).thenReturn(presentation);
    when(editorPaneMenuItemFactory.createMenuItem((Action) any()))
        .thenReturn(editorPaneActionMenuItem);
    when(editorPaneMenuItemFactory.createMenuItem((TabItem) any()))
        .thenReturn(editorPaneTabMenuItem);

    Container container = mock(Container.class);
    Promise promise = mock(Promise.class);
    when(appContext.getWorkspaceRoot()).thenReturn(container);
    when(container.getFile(nullable(Path.class))).thenReturn(promise);

    presenter =
        new EditorPartStackPresenter(
            view,
            appContext,
            partMenu,
            partsComparator,
            editorPaneMenuItemFactory,
            presentationFactory,
            eventBus,
            tabItemFactory,
            partStackEventHandler,
            editorPaneMenu,
            actionManager,
            closePaneAction,
            closeAllTabsPaneAction,
            editorAgent,
            addEditorTabMenuFactory);

    when(tabItemFactory.createEditorPartButton(partPresenter1, presenter)).thenReturn(editorTab1);
    when(tabItemFactory.createEditorPartButton(partPresenter2, presenter)).thenReturn(editorTab2);
    when(tabItemFactory.createEditorPartButton(partPresenter3, presenter)).thenReturn(editorTab3);
    when(editorTab1.getFile()).thenReturn(file1);
  }

  @Test
  public void constructorShouldBeVerified() {
    verify(view, times(2)).setDelegate(presenter);
    verify(view).addPaneMenuButton(editorPaneMenu);
    verify(editorPaneMenuItemFactory, times(4))
        .createMenuItem(org.mockito.ArgumentMatchers.<BaseAction>anyObject());
    verify(editorPaneMenu)
        .addItem(org.mockito.ArgumentMatchers.<PaneMenuActionItemWidget>anyObject(), eq(true));
    verify(editorPaneMenu, times(3))
        .addItem(org.mockito.ArgumentMatchers.<PaneMenuActionItemWidget>anyObject());
  }

  @Test
  public void focusShouldBeSet() {
    presenter.setFocus(true);

    verify(view).setFocus(true);
  }

  @Test
  public void partShouldBeAdded() {
    presenter.addPart(partPresenter1);

    verify(partPresenter1, times(2))
        .addPropertyListener(org.mockito.ArgumentMatchers.<PropertyListener>anyObject());

    verify(tabItemFactory).createEditorPartButton(partPresenter1, presenter);

    verify(editorTab1).setDelegate(presenter);

    verify(view).addTab(editorTab1, partPresenter1);

    verify(view).selectTab(partPresenter1);
  }

  @Test
  public void partShouldNotBeAddedWhenItExist() {
    presenter.addPart(partPresenter1);
    reset(view);

    presenter.addPart(partPresenter1);

    verify(view, never()).addTab(editorTab1, partPresenter1);

    verify(view).selectTab(partPresenter1);
  }

  @Test
  public void activePartShouldBeReturned() {
    presenter.setActivePart(partPresenter1);

    assertEquals(presenter.getActivePart(), partPresenter1);
  }

  @Test
  public void onTabShouldBeClicked() {
    presenter.addPart(partPresenter1);
    reset(view);

    presenter.onTabClicked(editorTab1);

    verify(view).selectTab(partPresenter1);
  }

  @Test
  public void tabShouldBeClosed() {
    presenter.addPart(partPresenter1);

    presenter.removePart(partPresenter1);
    presenter.onTabClose(editorTab1);

    verify(view).removeTab(partPresenter1);
  }

  @Test
  public void activePartShouldBeChangedWhenWeClickOnTab() {
    presenter.addPart(partPresenter1);
    presenter.addPart(partPresenter2);

    presenter.onTabClicked(editorTab1);

    assertEquals(presenter.getActivePart(), partPresenter1);

    presenter.onTabClicked(editorTab2);

    assertEquals(presenter.getActivePart(), partPresenter2);
  }

  @Test
  public void previousTabSelectedWhenWeRemovePart() {
    presenter.addPart(partPresenter1);
    presenter.addPart(partPresenter2);

    presenter.onTabClicked(editorTab2);
    presenter.removePart(partPresenter2);
    presenter.onTabClose(editorTab2);

    assertEquals(presenter.getActivePart(), partPresenter1);
  }

  @Test
  public void activePartShouldBeNullWhenWeCloseAllParts() {
    presenter.addPart(partPresenter1);

    presenter.onTabClose(editorTab1);

    assertThat(presenter.getActivePart(), is(nullValue()));
  }

  @Test
  public void shouldReturnNextPart() {
    presenter.addPart(partPresenter1);
    presenter.addPart(partPresenter2);
    presenter.addPart(partPresenter3);

    EditorPartPresenter result = presenter.getNextFor(partPresenter2);

    assertNotNull(result);
    assertEquals(partPresenter3, result);
  }

  @Test
  public void shouldReturnFirstPart() {
    presenter.addPart(partPresenter1);
    presenter.addPart(partPresenter2);
    presenter.addPart(partPresenter3);

    EditorPartPresenter result = presenter.getNextFor(partPresenter3);

    assertNotNull(result);
    assertEquals(partPresenter1, result);
  }

  @Test
  public void shouldReturnPreviousPart() {
    presenter.addPart(partPresenter1);
    presenter.addPart(partPresenter2);
    presenter.addPart(partPresenter3);

    EditorPartPresenter result = presenter.getPreviousFor(partPresenter2);

    assertNotNull(result);
    assertEquals(partPresenter1, result);
  }

  @Test
  public void shouldReturnLastPart() {
    presenter.addPart(partPresenter1);
    presenter.addPart(partPresenter2);
    presenter.addPart(partPresenter3);

    EditorPartPresenter result = presenter.getPreviousFor(partPresenter1);

    assertNotNull(result);
    assertEquals(partPresenter3, result);
  }

  @Test
  public void tabShouldBeReturnedByPath() throws Exception {
    Path path = new Path(SOME_TEXT);
    when(editorTab1.getFile()).thenReturn(file1);
    when(file1.getLocation()).thenReturn(path);

    presenter.addPart(partPresenter1);

    assertEquals(editorTab1, presenter.getTabByPath(path));
  }

  @Test
  public void shouldAddHandlers() throws Exception {
    presenter.addPart(partPresenter1);

    verify(eventBus, times(2))
        .addHandler((Event.Type<EditorPartStackPresenter>) anyObject(), eq(presenter));
  }

  @Test
  public void shouldNotAddHandlersWhenHandlersAlreadyExist() throws Exception {
    presenter.addPart(partPresenter1);
    reset(eventBus);

    presenter.addPart(partPresenter2);

    verify(eventBus, never())
        .addHandler((Event.Type<EditorPartStackPresenter>) anyObject(), eq(presenter));
  }

  @Test
  public void shouldRemoveHandlersWhenPartsIsAbsent() throws Exception {
    presenter.addPart(partPresenter1);
    reset(handlerRegistration);

    presenter.removePart(partPresenter1);

    verify(handlerRegistration, times(2)).removeHandler();
  }

  @Test
  public void shouldAvoidNPEWhenHandlersAlreadyRemoved() throws Exception {
    presenter.addPart(partPresenter1);
    presenter.removePart(partPresenter1);
    reset(handlerRegistration);

    presenter.removePart(partPresenter2);

    verify(handlerRegistration, never()).removeHandler();
  }

  @Test
  public void openPreviousActivePartTest() {
    presenter.addPart(partPresenter1);

    presenter.openPreviousActivePart();

    verify(view).selectTab(eq(partPresenter1));
  }

  @Test
  public void onTabItemClickedTest() {
    TabItem tabItem = mock(TabItem.class);
    when(editorPaneTabMenuItem.getData()).thenReturn(tabItem);

    presenter.paneMenuTabItemHandler.onItemClicked(editorPaneTabMenuItem);

    verify(view).selectTab((PartPresenter) anyObject());
  }

  @Test
  public void onActionClickedTest() {
    Action action = mock(BaseAction.class);
    when(editorPaneActionMenuItem.getData()).thenReturn(action);
    presenter.addPart(partPresenter1);
    presenter.setActivePart(partPresenter1);

    presenter.paneMenuActionItemHandler.onItemClicked(editorPaneActionMenuItem);

    verify(presentation).putClientProperty(eq(CURRENT_PANE_PROP), eq(presenter));
    verify(presentation).putClientProperty(eq(CURRENT_TAB_PROP), eq(editorTab1));
    verify(presentation).putClientProperty(eq(CURRENT_FILE_PROP), eq(file1));
  }

  @Test
  public void onItemCloseTest() {
    when(editorPaneTabMenuItem.getData()).thenReturn(editorTab1);

    presenter.paneMenuTabItemHandler.onCloseButtonClicked(editorPaneTabMenuItem);

    verify(editorAgent).closeEditor(nullable(EditorPartPresenter.class));
  }
}
