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
package org.eclipse.che.plugin.testing.ide.view.navigation.nodes;

import static org.eclipse.che.ide.api.theme.Style.getEditorInfoTextColor;

import javax.validation.constraints.NotNull;

import org.eclipse.che.api.testing.shared.common.TestResultStatus;
import org.eclipse.che.api.testing.shared.dto.TestResultTraceDto;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.plugin.testing.ide.TestResources;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;
import org.eclipse.che.plugin.testing.ide.view.navigation.factory.TestResultNodeFactory;

/**
 * Abstract implementation of test result tree node.
 * 
 * @author Bartlomiej Laczkowski
 */
public abstract class AbstractTestResultTreeNode extends AbstractTreeNode implements HasPresentation {

    protected final TestResources         testResources;
    protected final TestResultNodeFactory nodeFactory;
    protected final TestServiceClient     testServiceClient;
    protected final String                frameworkName;
    private NodePresentation              nodePresentation;

    private static final String           TEXT_INFO_CSS = "color: " + getEditorInfoTextColor() + "; font-size: 11px";

    public AbstractTestResultTreeNode(TestServiceClient testServiceClient,
                                      TestResultNodeFactory nodeFactory,
                                      TestResources testResources,
                                      String frameworkName) {
        this.testServiceClient = testServiceClient;
        this.nodeFactory = nodeFactory;
        this.testResources = testResources;
        this.frameworkName = frameworkName;
    }

    /**
     * Implementors should return related test result trace if there is any.
     * 
     * @return related test result trace
     */
    public abstract TestResultTraceDto getTestTrace();

    /**
     * Implementors should return related test result info text if there is any.
     * 
     * @return related test result info text
     */
    public abstract String getTestInfoText();

    /**
     * Implementors should return related test result status.
     * 
     * @return related test result status
     */
    public abstract TestResultStatus getTestStatus();

    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        String infoText = getTestInfoText();
        switch (getTestStatus()) {
        case FAILURE:
        case ERROR: {
            presentation.setPresentableIcon(testResources.testResultFailureIcon());
            break;
        }
        case SUCCESS: {
            presentation.setPresentableIcon(testResources.testResultSuccessIcon());
            break;
        }
        case WARNING: {
            presentation.setPresentableIcon(testResources.testResultWarningIcon());
            break;
        }
        case SKIPPED: {
            presentation.setPresentableIcon(testResources.testResultSkippedIcon());
            break;
        }
        }
        presentation.setPresentableText(getName());
        if (infoText != null) {
            presentation.setInfoText(infoText);
            presentation.setInfoTextCss(TEXT_INFO_CSS);
        }
    }

    @Override
    public NodePresentation getPresentation(boolean update) {
        if (nodePresentation == null) {
            nodePresentation = new NodePresentation();
            updatePresentation(nodePresentation);
        }
        if (update) {
            updatePresentation(nodePresentation);
        }
        return nodePresentation;
    }

}
