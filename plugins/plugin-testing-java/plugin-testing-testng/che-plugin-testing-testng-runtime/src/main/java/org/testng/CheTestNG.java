/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.testng;

import org.testng.listeners.CheSuiteListener;
import org.testng.listeners.CheTestListener;
import org.testng.xml.XmlSuite;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for running tests in the TestNG framework.
 */
public class CheTestNG extends TestNG {

    @Override
    public void run() {
        try {
            initializeSuitesAndJarFile();

            List<XmlSuite> suites = new ArrayList<>();
            flatSuites(m_suites, suites);

            if (suites.isEmpty()) {
                TestingMessageHelper.reporterAttached(System.out);
                System.err.print("No test found to run.");
            } else {
                addCheListeners();
                super.run();
                System.exit(0);
            }

        } catch (Throwable throwable) {
            throwable.printStackTrace(System.err);
        }
    }

    private void addCheListeners() {
        CheTestNGListener listener = new CheTestNGListener();
        addListener(new CheSuiteListener(listener));
        addListener(new CheTestListener(listener));

        try {
            Class<?> configurationListenerClass = Class.forName("org.testng.listeners.CheConfigurationListener");
            Object confListener = configurationListenerClass.getConstructor(CheTestNGListener.class).newInstance(listener);
            addListener(confListener);

            Class<?> methodListenerClass = Class.forName("org.testng.listeners.CheInvokedMethodListener");
            Object methodListener = methodListenerClass.getConstructor(CheTestNGListener.class).newInstance(listener);
            addListener(methodListener);

            configurationListenerClass.getMethod("setIgnoreStarted").invoke(confListener);
        } catch (Throwable ignored) {
        }
    }

    private void flatSuites(List<XmlSuite> suites, List<XmlSuite> result) {
        for (XmlSuite suite : suites) {
            result.add(suite);
            flatSuites(suite.getChildSuites(), result);
        }
    }
}
