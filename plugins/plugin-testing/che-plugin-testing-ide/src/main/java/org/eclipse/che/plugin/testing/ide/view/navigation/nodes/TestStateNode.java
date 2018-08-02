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
package org.eclipse.che.plugin.testing.ide.view.navigation.nodes;

import static com.google.gwt.user.client.ui.UIObject.DEBUG_ID_PREFIX;
import static org.eclipse.che.ide.util.dom.Elements.createSpanElement;

import com.google.gwt.dom.client.Element;
import com.google.inject.assistedinject.Assisted;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.ui.smartTree.data.AbstractTreeNode;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.plugin.testing.ide.TestResources;
import org.eclipse.che.plugin.testing.ide.model.TestState;
import org.eclipse.che.plugin.testing.ide.model.info.TestStateDescription;

/** Describes UI state of the test node. */
public class TestStateNode extends AbstractTreeNode implements HasPresentation {

  private final PromiseProvider promiseProvider;
  private final TestResources testResources;
  private final TestState testState;

  private NodePresentation nodePresentation;

  @Inject
  public TestStateNode(
      PromiseProvider promiseProvider, TestResources testResources, @Assisted TestState testState) {
    this.promiseProvider = promiseProvider;
    this.testResources = testResources;
    this.testState = testState;
  }

  @Override
  public String getName() {
    return testState.getPresentation();
  }

  @Override
  public boolean isLeaf() {
    return testState.isLeaf();
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    List<Node> child = new ArrayList<>();
    for (TestState state : testState.getChildren()) {
      if (!state.isConfig() || !state.isPassed()) {
        child.add(new TestStateNode(promiseProvider, testResources, state));
      }
    }
    return promiseProvider.resolve(child);
  }

  public TestState getTestState() {
    return testState;
  }

  @Override
  public void updatePresentation(NodePresentation presentation) {
    presentation.setPresentableText(testState.getPresentation());
    presentation.setUserElement((Element) createSpanElement());
    if (testState.isSuite()) {
      return;
    }
    if (testState.getDescription() == TestStateDescription.PASSED) {
      presentation.setPresentableTextCss("color: green;");
      presentation.getUserElement().setId(DEBUG_ID_PREFIX + "test-state-passed");
      presentation.setPresentableIcon(testResources.testResultSuccessIcon());
    } else if (testState.getDescription() == TestStateDescription.IGNORED) {
      presentation.setPresentableTextCss("text-decoration: line-through; color: yellow;");
      presentation.getUserElement().setId(DEBUG_ID_PREFIX + "test-state-ignore");
      presentation.setPresentableIcon(testResources.testResultSkippedIcon());
    } else if (testState.getDescription() == TestStateDescription.FAILED
        || testState.getDescription() == TestStateDescription.ERROR) {
      presentation.setPresentableTextCss("color: red;");
      presentation.getUserElement().setId(DEBUG_ID_PREFIX + "test-state-failed");
      presentation.setPresentableIcon(testResources.testResultFailureIcon());
    } else if (testState.getDescription() == TestStateDescription.RUNNING) {
      presentation.setPresentableIcon(testResources.testInProgressIcon());
    } else if (testState.getDescription() == TestStateDescription.NOT_RUN) {
      presentation.setPresentableIcon(testResources.testResultSkippedIcon());
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
