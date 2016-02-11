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
package org.eclipse.che.ide.settings;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.settings.common.SettingsPagePresenter;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialog;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class SettingsPresenterTest {

    private static final String SOME_TEXT_1 = "someText1";
    private static final String SOME_TEXT_2 = "someText2";

    //constructor mocks
    @Mock
    private SettingsView             view;
    @Mock
    private DialogFactory            dialogFactory;
    @Mock
    private CoreLocalizationConstant locale;

    //additional mocks
    @Mock
    private SettingsPagePresenter settingsPresenter1;
    @Mock
    private SettingsPagePresenter settingsPresenter2;
    @Mock
    private ConfirmDialog         confirmDialog;

    @Captor
    private ArgumentCaptor<ConfirmCallback>                         confirmCallbackCaptor;
    @Captor
    private ArgumentCaptor<CancelCallback>                          cancelCallbackCaptor;
    @Captor
    private ArgumentCaptor<Map<String, Set<SettingsPagePresenter>>> settingMapCaptor;

    private SettingsPresenter presenter;

    @Before
    public void setUp() {
        when(settingsPresenter1.getCategory()).thenReturn(SOME_TEXT_1);
        when(settingsPresenter2.getCategory()).thenReturn(SOME_TEXT_2);

        when(dialogFactory.createConfirmDialog(anyString(),
                                               anyString(),
                                               Matchers.<ConfirmCallback>anyObject(),
                                               Matchers.<CancelCallback>anyObject())).thenReturn(confirmDialog);

        Set<SettingsPagePresenter> settings = new HashSet<>();
        settings.add(settingsPresenter1);
        settings.add(settingsPresenter2);

        presenter = new SettingsPresenter(view, settings, dialogFactory, locale);
    }

    @Test
    public void constructorShouldBeVerified() {
        verify(view).setDelegate(presenter);

        verify(settingsPresenter1).setUpdateDelegate(presenter);
        verify(settingsPresenter2).setUpdateDelegate(presenter);
    }

    @Test
    public void onSaveButtonShouldBeClickedWhenFirstPageIsDirtyAndSecondNot() {
        when(settingsPresenter1.isDirty()).thenReturn(true);

        presenter.onSaveClicked();

        verify(settingsPresenter1).isDirty();
        verify(settingsPresenter2).isDirty();

        verify(settingsPresenter1).storeChanges();
        verify(settingsPresenter2, never()).storeChanges();

        reset(settingsPresenter1);

        verify(view).enableSaveButton(false);
    }

    @Test
    public void onRefreshButtonShouldBeClicked() {
        presenter.onRefreshClicked();

        verify(settingsPresenter1).revertChanges();
        verify(settingsPresenter2).revertChanges();

        verify(view).enableSaveButton(false);
    }

    @Test
    public void onCloseButtonShouldBeClickedWhenThereAreNotAnyUnSavedChanges() {
        when(locale.messagesPromptSaveChanges()).thenReturn(SOME_TEXT_1);

        presenter.onCloseClicked();

        verify(view).close();

        verify(dialogFactory, never()).createConfirmDialog(anyString(),
                                                           anyString(),
                                                           Matchers.<ConfirmCallback>anyObject(),
                                                           Matchers.<CancelCallback>anyObject());
    }

    @Test
    public void onCloseButtonShouldBeClickedWhenThereAreUnSavedChanges() {
        when(settingsPresenter1.isDirty()).thenReturn(true);
        when(locale.messagesPromptSaveChanges()).thenReturn(SOME_TEXT_1);

        presenter.onCloseClicked();

        verify(view, never()).close();

        verify(dialogFactory).createConfirmDialog(eq(""), eq(SOME_TEXT_1), confirmCallbackCaptor.capture(), cancelCallbackCaptor.capture());
    }

    @Test
    public void confirmCallbackShouldBeVerified() {
        when(settingsPresenter1.isDirty()).thenReturn(true);

        presenter.onCloseClicked();

        verify(dialogFactory).createConfirmDialog(eq(""), anyString(), confirmCallbackCaptor.capture(), cancelCallbackCaptor.capture());
        confirmCallbackCaptor.getValue().accepted();

        verify(settingsPresenter1, times(2)).isDirty();
        verify(settingsPresenter2, times(2)).isDirty();

        verify(settingsPresenter1).storeChanges();
        verify(settingsPresenter2, never()).storeChanges();

        verify(view).close();
    }

    @Test
    public void cancelCallbackShouldBeVerified() {
        when(settingsPresenter1.isDirty()).thenReturn(true);

        presenter.onCloseClicked();

        verify(dialogFactory).createConfirmDialog(eq(""), anyString(), confirmCallbackCaptor.capture(), cancelCallbackCaptor.capture());
        cancelCallbackCaptor.getValue().cancelled();

        verify(settingsPresenter1, times(2)).isDirty();
        verify(settingsPresenter2, times(2)).isDirty();

        verify(settingsPresenter1).revertChanges();
        verify(settingsPresenter2, never()).revertChanges();

        verify(view).close();
    }

    @Test
    public void onSettingsGroupShouldBeSelected() {
        AcceptsOneWidget widget = mock(AcceptsOneWidget.class);
        when(view.getContentPanel()).thenReturn(widget);

        presenter.onSettingsGroupSelected(settingsPresenter1);

        verify(view).getContentPanel();
        verify(settingsPresenter1).go(widget);
    }

    @Test
    public void dialogShouldBeShown() {
        presenter.show();

        verify(settingsPresenter1).getCategory();
        verify(settingsPresenter2).getCategory();

        verify(view).setSettings(settingMapCaptor.capture());
        Map<String, Set<SettingsPagePresenter>> map = settingMapCaptor.getValue();

        assertThat(map.get(SOME_TEXT_1).contains(settingsPresenter1), is(true));
        assertThat(map.get(SOME_TEXT_2).contains(settingsPresenter2), is(true));

        verify(view).show();
        verify(view).enableSaveButton(false);
        verify(view).selectSettingGroup(Matchers.<SettingsPagePresenter>anyObject());
    }

    @Test
    public void dialogShouldBeShownWhenWeOpenWindowSecondTime() {
        presenter.show();
        reset(view);

        presenter.show();

        verify(view).show();

        verify(view, never()).setSettings(Matchers.<Map<String, Set<SettingsPagePresenter>>>anyObject());
    }
}