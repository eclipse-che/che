package org.eclipse.che.ide.ext.java.testing.core.client.view.navigation.factory;

import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.ide.ext.java.testing.core.client.view.navigation.TestClassNavigation;
import org.eclipse.che.ide.ext.java.testing.core.client.view.navigation.nodes.TestResultClassNode;
import org.eclipse.che.ide.ext.java.testing.core.client.view.navigation.nodes.TestResultGroupNode;
import org.eclipse.che.ide.ext.java.testing.core.client.view.navigation.nodes.TestResultMethodNode;
import org.eclipse.che.ide.ext.java.testing.core.shared.TestResult;

public interface TestResultNodeFactory {

    TestResultGroupNode getTestResultGroupNode(TestResult result);

    TestResultClassNode getTestResultClassNodeNode(String className);

    TestResultMethodNode getTestResultMethodNodeNode(@Assisted("methodName") String methodName,
                                                     @Assisted("stackTrace") String stackTrace,
                                                     @Assisted("message") String message,
                                                     int lineNumber,
                                                     TestClassNavigation navigationHandler);
}
