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
package org.eclipse.che.plugin.testing.ide.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.NodeUniqueKeyProvider;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.plugin.testing.ide.model.TestRootState;
import org.eclipse.che.plugin.testing.ide.model.TestState;
import org.eclipse.che.plugin.testing.ide.view.navigation.factory.TestResultNodeFactory;
import org.eclipse.che.plugin.testing.ide.view.navigation.nodes.TestRootNode;
import org.eclipse.che.plugin.testing.ide.view.navigation.nodes.TestStateNode;

/**
 * Implementation for TestResult view. Uses tree for presenting test results.
 *
 * @author Mirage Abeysekara
 */
@Singleton
public class TestResultViewImpl extends BaseView<TestResultView.ActionDelegate>
    implements TestResultView {

  private static final TestResultViewImplUiBinder UI_BINDER =
      GWT.create(TestResultViewImplUiBinder.class);
  private final TestResultNodeFactory nodeFactory;

  @UiField(provided = true)
  SplitLayoutPanel splitLayoutPanel;

  @UiField FlowPanel navigationPanel;
  private Tree resultTree;

  private TestRootState testRootState;
  private TestRootNode testRootNode;

  @Inject
  public TestResultViewImpl(TestResultNodeFactory nodeFactory, PrinterOutputConsole outputConsole) {
    this.nodeFactory = nodeFactory;
    splitLayoutPanel = new SplitLayoutPanel(1);
    setContentWidget(UI_BINDER.createAndBindUi(this));
    splitLayoutPanel.add(outputConsole);

    NodeUniqueKeyProvider idProvider =
        new NodeUniqueKeyProvider() {
          @NotNull
          @Override
          public String getKey(@NotNull Node item) {
            return String.valueOf(item.hashCode());
          }
        };
    NodeStorage nodeStorage = new NodeStorage(idProvider);
    NodeLoader nodeLoader = new NodeLoader(Collections.emptySet());
    resultTree = new Tree(nodeStorage, nodeLoader);
    resultTree
        .getSelectionModel()
        .addSelectionHandler(
            event -> {
              Node methodNode = event.getSelectedItem();
              if (methodNode instanceof TestStateNode) {
                outputConsole.testSelected(((TestStateNode) methodNode).getTestState());
              }
            });

    resultTree.setAutoExpand(true);
    resultTree.getNodeLoader().setUseCaching(false);

    resultTree.getElement().getStyle().setWidth(100, Style.Unit.PCT);
    resultTree.getElement().getStyle().setHeight(100, Style.Unit.PCT);
    navigationPanel.add(resultTree);

    testRootState = new TestRootState();
  }

  @Override
  public void onTestingStarted(TestRootState testRootState) {
    resultTree.clear();
    resultTree.getNodeStorage().clear();
    testRootNode = nodeFactory.create(testRootState);
    resultTree.getNodeStorage().add(testRootNode);
  }

  @Override
  public void onTestingFinished(TestRootState testRootState) {
    resultTree.refresh(findNodeByState(testRootState));
  }

  private void addSuiteOrTest(TestState testState) {
    TestState parent = testState.getParent();
    if (parent == null) {
      return;
    }

    TestStateNode parentStateNode = findNodeByState(parent);
    if (parentStateNode != null) {
      Node parentStateNodeParent = parentStateNode.getParent();
      resultTree.refresh(parentStateNode);
      int parentIndex = resultTree.getNodeStorage().indexOf(parentStateNode);
      resultTree.getNodeStorage().remove(parentStateNode);
      if (parentStateNodeParent != null) {
        resultTree.getNodeStorage().insert(parentStateNodeParent, parentIndex, parentStateNode);
      } else {
        resultTree.getNodeStorage().add(parentStateNode);
      }

      resultTree.setExpanded(parentStateNode, true);
      TestStateNode nodeByState = findNodeByState(testState);
      if (nodeByState != null) {
        resultTree.getSelectionModel().select(nodeByState, false);
      }
    }
  }

  private TestStateNode findNodeByState(TestState parent) {
    for (Node node : resultTree.getNodeStorage().getAll()) {
      if (node instanceof TestStateNode) {
        TestStateNode stateNode = (TestStateNode) node;
        if (stateNode.getTestState().equals(parent)) {
          return stateNode;
        }
      }
    }
    return null;
  }

  @Override
  public void onSuiteStarted(TestState testState) {
    addSuiteOrTest(testState);
  }

  @Override
  public void onSuiteFinished(TestState testState) {}

  @Override
  public void onSuiteTreeNodeAdded(TestState testState) {
    // addSuiteOrTest(testState);
  }

  @Override
  public void onSuiteTreeStarted(TestState testState) {
    addSuiteOrTest(testState);
  }

  @Override
  public void onSuiteTreeNodeFinished(TestState suite) {}

  @Override
  public void onTestsCountInSuite(int count) {}

  @Override
  public void onTestStarted(TestState testState) {
    addSuiteOrTest(testState);
  }

  @Override
  public void onTestFinished(TestState testState) {
    handleTestMethodEnded(testState);
  }

  @Override
  public void onTestFailed(TestState testState) {
    handleTestMethodEnded(testState);
  }

  @Override
  public void onTestIgnored(TestState testState) {
    handleTestMethodEnded(testState);
  }

  private void handleTestMethodEnded(TestState testState) {
    TestStateNode nodeByState = findNodeByState(testState);
    if (nodeByState == null) {
      return;
    }
    if (testState.isConfig()) {
      resultTree.getNodeStorage().remove(nodeByState);
    }
    resultTree.refresh(nodeByState);
  }

  @Override
  public void onRootPresentationAdded(TestRootState testRootState) {
    resultTree.refresh(testRootNode);
    resultTree.getNodeStorage().update(testRootNode);
    resultTree.setExpanded(testRootNode, true);
  }

  /** {@inheritDoc} */
  @Override
  protected void focusView() {}

  @Override
  public TestRootState getRootState() {
    testRootState = new TestRootState();
    return testRootState;
  }

  interface TestResultViewImplUiBinder extends UiBinder<Widget, TestResultViewImpl> {}
}
