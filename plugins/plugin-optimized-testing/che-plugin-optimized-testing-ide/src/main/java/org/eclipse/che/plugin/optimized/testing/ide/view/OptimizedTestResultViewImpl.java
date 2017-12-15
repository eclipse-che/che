/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.optimized.testing.ide.view;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.testing.shared.OptimizedConstants;
import org.eclipse.che.api.testing.shared.dto.FailedTestsToStoreDto;
import org.eclipse.che.api.testing.shared.dto.SmartTestingConfigurationParamDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.plugin.optimized.testing.ide.OptimizedTestLocalizationConstant;
import org.eclipse.che.plugin.optimized.testing.ide.OptimizedTestServiceClient;
import org.eclipse.che.plugin.optimized.testing.ide.preference.SmartTestingExperimentalFeature;
import org.eclipse.che.plugin.testing.ide.TestResources;
import org.eclipse.che.plugin.testing.ide.model.TestRootState;
import org.eclipse.che.plugin.testing.ide.model.TestState;
import org.eclipse.che.plugin.testing.ide.view.PrinterOutputConsole;
import org.eclipse.che.plugin.testing.ide.view.TestResultViewImpl;
import org.eclipse.che.plugin.testing.ide.view.navigation.factory.TestResultNodeFactory;

/** Slightly modified TestResult view logic - adds additional label related to optimized testing */
@Singleton
public class OptimizedTestResultViewImpl extends TestResultViewImpl {

  private final OptimizedTestServiceClient optimizedTestServiceClient;
  private final OptimizedTestLocalizationConstant localizationConstant;
  private final Boolean isOptimizedFeatureEnabled;
  private final DtoFactory dtoFactory;
  private List<String> failedTests = new ArrayList<>();

  @Inject
  public OptimizedTestResultViewImpl(
      TestResources testResources,
      PartStackUIResources resources,
      JavaNavigationService javaNavigationService,
      EditorAgent editorAgent,
      AppContext appContext,
      TestResultNodeFactory nodeFactory,
      PrinterOutputConsole outputConsole,
      OptimizedTestServiceClient optimizedTestServiceClient,
      OptimizedTestLocalizationConstant localizationConstant,
      PreferencesManager preferencesManager,
      DtoFactory dtoFactory) {
    super(
        testResources,
        resources,
        javaNavigationService,
        editorAgent,
        appContext,
        nodeFactory,
        outputConsole,
        localizationConstant);
    this.optimizedTestServiceClient = optimizedTestServiceClient;
    this.localizationConstant = localizationConstant;
    isOptimizedFeatureEnabled =
        Boolean.valueOf(
            preferencesManager.getValue(
                SmartTestingExperimentalFeature.SMART_TESTING_FEATURE_ENABLE));
    this.dtoFactory = dtoFactory;
  }

  @Override
  public void onTestingStarted(TestRootState testRootState) {
    super.onTestingStarted(testRootState);
    failedTests.clear();

    Boolean isOptimizedEnabled =
        Boolean.valueOf(
            getTestExecutionContext()
                .getTestContextParameters()
                .get(OptimizedConstants.TEST_CONTEXT_PARAMETER_OPTIMIZED_IS_ENABLED));

    if (isOptimizedEnabled) {
      setTitle(localizationConstant.titleOptimizedTestResultPanel());
      optimizedTestServiceClient
          .getConfiguration(getTestExecutionContext().getProjectPath())
          .onSuccess(
              result -> {
                final StringBuffer stInfo = new StringBuffer();
                appendParamInfo(stInfo, result.getParameters().get(OptimizedConstants.STRATEGIES));
                appendParamInfo(stInfo, result.getParameters().get(OptimizedConstants.MODE));
                setAdditionalInfoLabel(stInfo.toString());
              })
          .onFailure(() -> setAdditionalInfoLabel(""));
    } else {
      setTestTitle();
      setAdditionalInfoLabel("");
    }
  }

  @Override
  public void onTestingFinished(TestRootState testRootState) {
    super.onTestingFinished(testRootState);
    if (isOptimizedFeatureEnabled) {
      FailedTestsToStoreDto failedTestsToStore = dtoFactory.createDto(FailedTestsToStoreDto.class);
      failedTestsToStore.getFailedTests().addAll(failedTests);
      failedTestsToStore.setProjectDir(getTestExecutionContext().getProjectPath());
      optimizedTestServiceClient.saveTestFailureResults(failedTestsToStore);
    }
  }

  @Override
  public void onSuiteFinished(TestState testState) {
    super.onTestFinished(testState);
    if (isOptimizedFeatureEnabled && isErroredOrFailed(testState)) {
      failedTests.add(testState.getName());
    }
  }

  private boolean isErroredOrFailed(TestState testState) {
    return testState.hasErrorTests() || testState.hasFailedTest();
  }

  private void appendParamInfo(StringBuffer stInfo, SmartTestingConfigurationParamDto paramToAdd) {
    stInfo.append(paramToAdd.getName()).append("=").append(paramToAdd.getValues()).append(" ");
  }
}
