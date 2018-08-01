/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.client.wizard;

import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.UPDATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.WIZARD_MODE_KEY;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.message.MessageDialog;
import org.eclipse.che.plugin.maven.client.MavenLocalizationConstant;
import org.eclipse.che.plugin.maven.shared.MavenAttributes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** @author Valeriy Svydenko */
@RunWith(MockitoJUnitRunner.class)
public class MavenPagePresenterTest {
  private static final String TEXT = "to be or not to be";

  @Mock private MavenPageView view;
  @Mock private EventBus eventBus;
  @Mock private DialogFactory dialogFactory;
  @Mock private AppContext appContext;
  @Mock private MavenLocalizationConstant localization;

  @InjectMocks private MavenPagePresenter mavenPagePresenter;

  @Mock private MutableProjectConfig projectConfig;
  @Mock private Container workspaceRoot;
  @Mock private Promise<Optional<Container>> containerPromise;
  @Mock private Optional<Container> optionalContainer;
  @Mock private Promise<SourceEstimation> sourceEstimationPromise;

  @Captor private ArgumentCaptor<Operation<PromiseError>> containerArgumentErrorCapture;
  @Captor private ArgumentCaptor<Operation<Optional<Container>>> optionContainerCapture;

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

    when(containerPromise.then(
            org.mockito.ArgumentMatchers.<Operation<Optional<Container>>>anyObject()))
        .thenReturn(containerPromise);
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
    when(dialogFactory.createMessageDialog(anyString(), anyString(), anyObject()))
        .thenReturn(messageDialog);
    when(sourceEstimationPromise.then(
            org.mockito.ArgumentMatchers.<Operation<SourceEstimation>>anyObject()))
        .thenReturn(sourceEstimationPromise);
    when(sourceEstimationPromise.catchError(
            org.mockito.ArgumentMatchers.<Operation<PromiseError>>anyObject()))
        .thenReturn(sourceEstimationPromise);

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
