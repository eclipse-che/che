/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.hotupdate;

import com.google.inject.Inject;

import org.eclipse.che.selenium.core.provider.CheTestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.requestfactory.CheTestAdminHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.utils.process.ProcessAgent;
import org.eclipse.che.selenium.core.utils.process.ProcessAgentException;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Ide;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class RecreateUpdate {
    @Inject
    CheTestAdminHttpJsonRequestFactory testUserHttpJsonRequestFactory;

    @Inject
    CheTestApiEndpointUrlProvider cheTestApiEndpointUrlProvider;

    @Inject
    private ProcessAgent processAgent;

    @Inject
    private TestWorkspace workspace;

    @javax.inject.Inject
    private Ide ide;

    @BeforeTest
    public void setUp() throws ProcessAgentException {
        String command = "/tmp/oc  describe dc/che | grep Strategy";
        String result = processAgent.process(command);
    }

    @Test
    public void checkRecreateUpdateStrategy() throws Exception {
        ide.open(workspace);

    }




}
