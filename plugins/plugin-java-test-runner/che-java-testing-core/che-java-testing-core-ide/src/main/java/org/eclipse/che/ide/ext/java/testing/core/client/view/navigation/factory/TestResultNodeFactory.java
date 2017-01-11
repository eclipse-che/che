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
package org.eclipse.che.ide.ext.java.testing.core.client.view.navigation.factory;

import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.ide.ext.java.testing.core.client.view.navigation.TestClassNavigation;
import org.eclipse.che.ide.ext.java.testing.core.client.view.navigation.nodes.TestResultClassNode;
import org.eclipse.che.ide.ext.java.testing.core.client.view.navigation.nodes.TestResultGroupNode;
import org.eclipse.che.ide.ext.java.testing.core.client.view.navigation.nodes.TestResultMethodNode;
import org.eclipse.che.ide.ext.java.testing.core.shared.TestResult;
/**
 * Factory for providing test navigation tree nodes.
 * @author Mirage Abeysekara
 */
public interface TestResultNodeFactory {

    TestResultGroupNode getTestResultGroupNode(TestResult result);

    TestResultClassNode getTestResultClassNodeNode(String className);

    TestResultMethodNode getTestResultMethodNodeNode(@Assisted("methodName") String methodName,
                                                     @Assisted("stackTrace") String stackTrace,
                                                     @Assisted("message") String message,
                                                     int lineNumber,
                                                     TestClassNavigation navigationHandler);
}
