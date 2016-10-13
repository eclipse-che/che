/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.composer.ide.importer;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.wizard.Wizard;
import org.eclipse.che.plugin.composer.ide.ComposerLocalizationConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

/**
 * Testing {@link ComposerImporterPagePresenter} functionality.
 *
 * @author Kaloyan Raev
 */
@RunWith(MockitoJUnitRunner.class)
public class ComposerImporterPagePresenterTest {
    @Mock
    private Wizard.UpdateDelegate                     updateDelegate;
    @Mock
    private ComposerImporterPageView                  view;
    @Mock
    private ComposerLocalizationConstants             locale;
    @Mock
    private MutableProjectConfig                      dataObject;
    @Mock
    private MutableProjectConfig.MutableSourceStorage source;
    @Mock
    private Map<String, String>                       parameters;
    @InjectMocks
    private ComposerImporterPagePresenter             presenter;

    @Before
    public void setUp() {
        when(dataObject.getSource()).thenReturn(source);

        presenter.setUpdateDelegate(updateDelegate);
        presenter.init(dataObject);
    }

    @Test
    public void testGo() {
        AcceptsOneWidget container = mock(AcceptsOneWidget.class);

        presenter.go(container);

        verify(container).setWidget(eq(view));
        verify(view).setProjectName(anyString());
        verify(view).setProjectDescription(anyString());
        verify(view).setPackageName(anyString());
        verify(view).setInputsEnableState(eq(true));
        verify(view).focusInPackageNameInput();
    }

    @Test
    public void packageNameStartWithWhiteSpaceEnteredTest() {
        String incorrectPackageName = " vendor-name/package-name";
        String name = "package-name";
        when(view.getProjectName()).thenReturn("");

        presenter.packageNameChanged(incorrectPackageName);

        verify(view).markPackageNameInvalid();
        verify(view).setPackageNameErrorMessage(eq(locale.projectImporterPackageNameStartWithWhiteSpace()));

        verify(dataObject).setName(eq(name));
        verify(view).setProjectName(name);
        verify(updateDelegate).updateControls();
    }

    @Test
    public void incorrectPackageNameEnteredTest() {
        String incorrectPackageName = "package-name";
        String name = "package-name";
        when(view.getProjectName()).thenReturn("");

        presenter.packageNameChanged(incorrectPackageName);

        verify(view).markPackageNameInvalid();
        verify(view).setPackageNameErrorMessage(eq(locale.projectImporterPackageNameInvalid()));

        verify(dataObject).setName(eq(name));
        verify(view).setProjectName(name);
        verify(updateDelegate).updateControls();
    }

    @Test
    public void correctPackageNameEnteredTest() {
        String correctPackageName = "vendor-name/package-name";
        when(view.getProjectName()).thenReturn("");

        presenter.packageNameChanged(correctPackageName);

        verifyInvocationsForCorrectPackageName(correctPackageName);
    }

    @Test
    public void correctProjectNameEnteredTest() {
        String correctName = "angularjs";
        when(view.getProjectName()).thenReturn(correctName);

        presenter.projectNameChanged(correctName);

        verify(dataObject).setName(eq(correctName));
        verify(view).markNameValid();
        verify(view, never()).markNameInvalid();
        verify(updateDelegate).updateControls();
    }

    @Test
    public void correctProjectNameWithPointEnteredTest() {
        String correctName = "Test.project..ForCodenvy";
        when(view.getProjectName()).thenReturn(correctName);

        presenter.projectNameChanged(correctName);

        verify(dataObject).setName(eq(correctName));
        verify(view).markNameValid();
        verify(view, never()).markNameInvalid();
        verify(updateDelegate).updateControls();
    }

    @Test
    public void emptyProjectNameEnteredTest() {
        String emptyName = "";
        when(view.getProjectName()).thenReturn(emptyName);

        presenter.projectNameChanged(emptyName);

        verify(dataObject).setName(eq(emptyName));
        verify(updateDelegate).updateControls();
    }

    @Test
    public void incorrectProjectNameEnteredTest() {
        String incorrectName = "angularjs+";
        when(view.getProjectName()).thenReturn(incorrectName);

        presenter.projectNameChanged(incorrectName);

        verify(dataObject).setName(eq(incorrectName));
        verify(view).markNameInvalid();
        verify(updateDelegate).updateControls();
    }

    @Test
    public void projectDescriptionChangedTest() {
        String description = "description";
        presenter.projectDescriptionChanged(description);

        verify(dataObject).setDescription(eq(description));
    }

    private void verifyInvocationsForCorrectPackageName(String correctPackageName) {
        verify(view, never()).markPackageNameInvalid();
        verify(source).setLocation(eq(correctPackageName));
        verify(view).markPackageNameValid();
        verify(view).setProjectName(anyString());
        verify(updateDelegate).updateControls();
    }

}
