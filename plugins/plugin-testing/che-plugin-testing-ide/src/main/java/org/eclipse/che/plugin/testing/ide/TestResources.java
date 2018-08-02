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
package org.eclipse.che.plugin.testing.ide;

import com.google.gwt.resources.client.ClientBundle;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Resources for test extension.
 *
 * @author Mirage Abeysekara
 */
public interface TestResources extends ClientBundle {

  @Source("org/eclipse/che/plugin/testing/ide/svg/test.svg")
  SVGResource testIcon();

  @Source("org/eclipse/che/plugin/testing/ide/svg/test_all.svg")
  SVGResource debugIcon();

  @Source("org/eclipse/che/plugin/testing/ide/svg/test_results_pass.svg")
  @Deprecated
  SVGResource testResultsPass();

  @Source("org/eclipse/che/plugin/testing/ide/svg/test_results_fail.svg")
  @Deprecated
  SVGResource testResultsFail();

  @Source("org/eclipse/che/plugin/testing/ide/svg/test_method_fail.svg")
  SVGResource testMethodFail();

  @Source("org/eclipse/che/plugin/testing/ide/svg/test_class_fail.svg")
  SVGResource testClassFail();

  @Source("org/eclipse/che/plugin/testing/ide/svg/show_all_tests_icon.svg")
  SVGResource showAllTestsButtonIcon();

  @Source("org/eclipse/che/plugin/testing/ide/svg/show_failures_only_icon.svg")
  SVGResource showFailuresOnlyButtonIcon();

  @Source("org/eclipse/che/plugin/testing/ide/svg/test_result_failure.svg")
  SVGResource testResultFailureIcon();

  @Source("org/eclipse/che/plugin/testing/ide/svg/test_result_success.svg")
  SVGResource testResultSuccessIcon();

  @Source("org/eclipse/che/plugin/testing/ide/svg/test_result_warning.svg")
  SVGResource testResultWarningIcon();

  @Source("org/eclipse/che/plugin/testing/ide/svg/test_result_skipped.svg")
  SVGResource testResultSkippedIcon();

  @Source("org/eclipse/che/plugin/testing/ide/svg/test_result_trace_frame.svg")
  SVGResource testResultTraceFrameIcon();

  @Source("org/eclipse/che/plugin/testing/ide/svg/test_in_progress.svg")
  SVGResource testInProgressIcon();
}
