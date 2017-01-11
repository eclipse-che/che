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
package org.eclipse.che.plugin.maven.client.wizard;

import com.google.common.base.Optional;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.dialogs.MessageDialog;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.plugin.maven.client.MavenLocalizationConstant;
import org.eclipse.che.plugin.maven.shared.MavenAttributes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.UPDATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.WIZARD_MODE_KEY;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(MockitoJUnitRunner.class)
public class MavenPagePresenterTest {
    private final static String TEXT = "to be or not to be";

    @Mock
    private MavenPageView             view;
    @Mock
    private EventBus                  eventBus;
    @Mock
    private DialogFactory             dialogFactory;
    @Mock
    private AppContext                appContext;
    @Mock
    private MavenLocalizationConstant localization;

    @InjectMocks
    private MavenPagePresenter mavenPagePresenter;

    @Mock
    private MutableProjectConfig         projectConfig;
    @Mock
    private Container                    workspaceRoot;
    @Mock
    private Promise<Optional<Container>> containerPromise;
    @Mock
    private Optional<Container>          optionalContainer;
    @Mock
    private Promise<SourceEstimation>    sourceEstimationPromise;

    @Captor
    private ArgumentCaptor<Operation<PromiseError>>        containerArgumentErrorCapture;
    @Captor
    private ArgumentCaptor<Operation<Optional<Container>>> optionContainerCapture;

    private Map<String, String> context;

    @Before
    public void setUp() throws Exception {
        context = new HashMap<>();
        mavenPagePresenter.setContext(context);

        when(optionalContainer.isPresent()).thenReturn(true);
        when(optionalContainer.get()).thenReturn(workspaceRoot);

        when(appContext.getWorkspaceRoot()).thenReturn(workspaceRoot);
        when(projectConfig.getPath()).thenReturn(TEXT);

        when(workspaceRoot.getContainer(TEXT)).thenReturn(containerPromise);
        when(workspaceRoot.estimate(MavenAttributes.MAVEN_ID)).thenReturn(sourceEstimationPromise);

        when(containerPromise.then(Matchers.<Operation<Optional<Container>>>anyObject())).thenReturn(containerPromise);
        when(containerPromise.catchError(Matchers.<Operation<PromiseError>>anyObject())).thenReturn(containerPromise);
    }

    @Test
    public void constructorShouldBePerformed() throws Exception {
        verify(view).setDelegate(mavenPagePresenter);
    }

    @Test
    public void warningWindowShouldBeShowedIfProjectEstimationHasSomeError() throws Exception {
        final String dialogTitle = "Not valid Maven project";
        PromiseError promiseError = mock(PromiseError.class);
        MessageDialog messageDialog = mock(MessageDialog.class);
        context.put(WIZARD_MODE_KEY, UPDATE.toString());

        when(promiseError.getMessage()).thenReturn(TEXT);
        when(localization.mavenPageErrorDialogTitle()).thenReturn(dialogTitle);
        when(dialogFactory.createMessageDialog(anyString(), anyString(), anyObject())).thenReturn(messageDialog);
        when(sourceEstimationPromise.then(Matchers.<Operation<SourceEstimation>>anyObject())).thenReturn(sourceEstimationPromise);
        when(sourceEstimationPromise.catchError(Matchers.<Operation<PromiseError>>anyObject())).thenReturn(sourceEstimationPromise);

        mavenPagePresenter.init(projectConfig);

        verify(containerPromise).then(optionContainerCapture.capture());
        optionContainerCapture.getValue().apply(optionalContainer);

        verify(sourceEstimationPromise).catchError(containerArgumentErrorCapture.capture());
        containerArgumentErrorCapture.getValue().apply(promiseError);

        verify(promiseError).getMessage();
        verify(dialogFactory).createMessageDialog(dialogTitle, TEXT, null);
        verify(messageDialog).show();
    }


}
