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
package org.eclipse.che.plugin.testing.ide.view.navigation.factory;

import org.eclipse.che.api.testing.shared.TestResult;
import org.eclipse.che.api.testing.shared.dto.TestResultDto;
import org.eclipse.che.api.testing.shared.dto.TestResultRootDto;
import org.eclipse.che.api.testing.shared.dto.TestResultTraceFrameDto;
import org.eclipse.che.plugin.testing.ide.view.navigation.TestClassNavigation;
import org.eclipse.che.plugin.testing.ide.view.navigation.nodes.TestResultClassNode;
import org.eclipse.che.plugin.testing.ide.view.navigation.nodes.TestResultGroupNode;
import org.eclipse.che.plugin.testing.ide.view.navigation.nodes.TestResultMethodNode;
import org.eclipse.che.plugin.testing.ide.view.navigation.nodes.TestResultNode;
import org.eclipse.che.plugin.testing.ide.view.navigation.nodes.TestResultRootNode;
import org.eclipse.che.plugin.testing.ide.view.navigation.nodes.TestResultTraceFrameNode;

import com.google.inject.assistedinject.Assisted;

/**
 * Factory for providing test navigation tree nodes.
 * 
 * @author Mirage Abeysekara
 */
public interface TestResultNodeFactory {

    TestResultRootNode createTestResultRootNode(TestResultRootDto testResultRootDto, 
                                                String frameworkName);

    TestResultNode createTestResultEntryNode(TestResultDto testResultDto,
                                             String frameworkName);
    
    TestResultTraceFrameNode createTestResultTraceFrameNode(TestResultTraceFrameDto testResultTraceFrameDto);

    @Deprecated
    TestResultGroupNode getTestResultGroupNode(TestResult result,
                                               boolean showFailuresOnly,
                                               Runnable showOnlyFailuresDelegate);

    @Deprecated
    TestResultClassNode getTestResultClassNodeNode(String className);

    @Deprecated
    TestResultMethodNode getTestResultMethodNodeNode(boolean success,
                                                     @Assisted("methodName") String methodName,
                                                     @Assisted("stackTrace") String stackTrace, 
                                                     @Assisted("message") String message, 
                                                     int lineNumber,
                                                     TestClassNavigation navigationHandler);

}
