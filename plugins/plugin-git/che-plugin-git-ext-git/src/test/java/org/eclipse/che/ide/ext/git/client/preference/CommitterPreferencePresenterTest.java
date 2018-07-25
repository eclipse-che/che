/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.preference;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** @author Valeriy Svydenko */
@RunWith(MockitoJUnitRunner.class)
public class CommitterPreferencePresenterTest {
  public static final String SOME_TEXT = "text";
  public static final String COMMITTER_NAME = "git.committer.name";
  public static final String COMMITTER_EMAIL = "git.committer.email";
  public static final String DEFAULT_COMMITTER_NAME = "Anonymous";
  public static final String DEFAULT_COMMITTER_EMAIL = "anonymous@noemail.com";

  @Mock private CommitterPreferenceView view;
  @Mock private GitLocalizationConstant constant;
  @Mock private PreferencesManager preferencesManager;
  @Mock private AcceptsOneWidget container;

  private CommitterPreferencePresenter presenter;

  @Before
  public void setUp() throws Exception {
    when(constant.committerTitle()).thenReturn(SOME_TEXT);
    when(constant.committerPreferenceCategory()).thenReturn(SOME_TEXT);

    presenter = new CommitterPreferencePresenter(view, constant, preferencesManager);
  }

  @Test
  public void constructorShouldBePerformed() throws Exception {
    verify(view).setDelegate(presenter);
    verify(constant).committerTitle();
    verify(constant).committerPreferenceCategory();
    verify(preferencesManager).getValue(COMMITTER_NAME);
    verify(preferencesManager).getValue(COMMITTER_EMAIL);
  }

  @Test
  public void dirtyStateShouldBeReturned() throws Exception {
    assertFalse(presenter.isDirty());
  }

  @Test
  public void widgetShouldBePrepared() throws Exception {
    presenter.go(container);

    verify(container).setWidget(view);
    verify(view).setEmail(anyString());
    verify(view).setName(anyString());
  }

  @Test
  public void changesShouldBeRestored() throws Exception {
    presenter.revertChanges();

    verify(preferencesManager, times(2)).getValue(COMMITTER_NAME);
    verify(preferencesManager, times(2)).getValue(COMMITTER_EMAIL);

    assertFalse(presenter.isDirty());
  }

  @Test
  public void defaultUserNameAndEmailShouldBeRestored() throws Exception {
    //        when(preferencesManager.getValue(COMMITTER_EMAIL)).thenReturn(null);
    //        when(preferencesManager.getValue(COMMITTER_NAME)).thenReturn(null);
    //
    //        presenter.revertChanges();
    //
    //        verify(preferencesManager, times(2)).getValue(COMMITTER_NAME);
    //        verify(preferencesManager, times(2)).getValue(COMMITTER_EMAIL);
    //
    //        verify(view).setEmail(DEFAULT_COMMITTER_EMAIL);
    //        verify(view).setName(DEFAULT_COMMITTER_NAME);
    //
    //        assertFalse(presenter.isDirty());
  }
}
