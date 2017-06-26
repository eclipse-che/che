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
package org.eclipse.che.plugin.testing.ide.view.navigation.nodes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.testing.shared.common.TestResultStatus;
import org.eclipse.che.api.testing.shared.common.TestResultType;
import org.eclipse.che.api.testing.shared.dto.SimpleLocationDto;
import org.eclipse.che.api.testing.shared.dto.TestResultDto;
import org.eclipse.che.api.testing.shared.dto.TestResultTraceDto;
import org.eclipse.che.ide.api.data.tree.HasAction;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.plugin.testing.ide.TestResources;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;
import org.eclipse.che.plugin.testing.ide.view.navigation.SimpleLocationHandler;
import org.eclipse.che.plugin.testing.ide.view.navigation.factory.TestResultNodeFactory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Test result node (i.e. test suite, test case).
 * 
 * @author Bartlomiej Laczkowski
 */
public class TestResultNode extends AbstractTestResultTreeNode implements HasAction {

    private final TestResultDto         testResultDto;
    private final SimpleLocationHandler simpleLocationHandler;

    @Inject
    public TestResultNode(TestServiceClient testServiceClient,
                          TestResultNodeFactory nodeFactory,
                          TestResources testResources,
                          SimpleLocationHandler simpleLocationHandler,
                          @Assisted TestResultDto testResultDto,
                          @Assisted String frameworkName) {
        super(testServiceClient, nodeFactory, testResources, frameworkName);
        this.testResultDto = testResultDto;
        this.simpleLocationHandler = simpleLocationHandler;
    }

    @Override
    public String getName() {
        return testResultDto.getName();
    }

    @Override
    public boolean isLeaf() {
        return testResultDto.getType() == TestResultType.TEST_CASE ? true : false;
    }

    @Override
    public String getTestInfoText() {
        if (testResultDto.getInfoText() != null)
            return testResultDto.getInfoText();
        return null;
    }

    @Override
    public TestResultStatus getTestStatus() {
        return testResultDto.getStatus();
    }

    @Override
    public TestResultTraceDto getTestTrace() {
        return testResultDto.getTrace();
    }

    @Override
    public void actionPerformed() {
        SimpleLocationDto location = testResultDto.getTestLocation();
        if (location != null) {
            simpleLocationHandler.openFile(location);
        }
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        final List<Node> children = new ArrayList<Node>();
        Promise<List<TestResultDto>> promise = testServiceClient.getTestResults(frameworkName,
                                                                                testResultDto.getResultPath());
        return promise.then(new Function<List<TestResultDto>, List<Node>>() {
            @Override
            public List<Node> apply(List<TestResultDto> arg) {
                for (TestResultDto entry : arg) {
                    TestResultNode child = nodeFactory.createTestResultEntryNode(entry, frameworkName);
                    children.add(child);
                }
                return children;
            }
        });
    }

}
