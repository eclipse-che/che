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
import org.eclipse.che.api.testing.shared.dto.TestResultDto;
import org.eclipse.che.api.testing.shared.dto.TestResultRootDto;
import org.eclipse.che.api.testing.shared.dto.TestResultTraceDto;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.plugin.testing.ide.TestResources;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;
import org.eclipse.che.plugin.testing.ide.view.navigation.factory.TestResultNodeFactory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Test results root node.
 * 
 * @author Bartlomiej Laczkowski
 */
public class TestResultRootNode extends AbstractTestResultTreeNode {

    private final TestResultRootDto testResultRootDto;

    @Inject
    public TestResultRootNode(TestServiceClient testServiceClient,
                              TestResultNodeFactory nodeFactory,
                              TestResources testResources,
                              @Assisted TestResultRootDto testResultRootDto,
                              @Assisted String frameworkName) {
        super(testServiceClient, nodeFactory, testResources, frameworkName);
        this.testResultRootDto = testResultRootDto;

    }

    @Override
    public String getName() {
        return testResultRootDto.getName();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public String getTestInfoText() {
        if (testResultRootDto.getInfoText() != null)
            return testResultRootDto.getInfoText();
        return null;
    }

    @Override
    public TestResultStatus getTestStatus() {
        return testResultRootDto.getStatus();
    }

    @Override
    public TestResultTraceDto getTestTrace() {
        return testResultRootDto.getTrace();
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        final List<Node> children = new ArrayList<Node>();
        Promise<List<TestResultDto>> promise = testServiceClient.getTestResults(frameworkName,
                                                                                testResultRootDto.getResultPath());
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
