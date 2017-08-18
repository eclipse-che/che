/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.testing.ide.view;

import static org.eclipse.che.ide.ui.smartTree.SelectionModel.Mode.SINGLE;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_TEST_SOURCE_FOLDER;

import com.google.common.base.Optional;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.testing.shared.TestCase;
import org.eclipse.che.api.testing.shared.TestResult;
import org.eclipse.che.api.testing.shared.dto.TestResultRootDto;
import org.eclipse.che.api.testing.shared.dto.TestResultTraceDto;
import org.eclipse.che.api.testing.shared.dto.TestResultTraceFrameDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.NodeInterceptor;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.shared.dto.Region;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.model.Method;
import org.eclipse.che.ide.ext.java.shared.dto.model.Type;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.NodeUniqueKeyProvider;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.testing.ide.view.navigation.TestClassNavigation;
import org.eclipse.che.plugin.testing.ide.view.navigation.factory.TestResultNodeFactory;
import org.eclipse.che.plugin.testing.ide.view.navigation.nodes.AbstractTestResultTreeNode;
import org.eclipse.che.plugin.testing.ide.view.navigation.nodes.TestResultClassNode;
import org.eclipse.che.plugin.testing.ide.view.navigation.nodes.TestResultGroupNode;
import org.eclipse.che.plugin.testing.ide.view.navigation.nodes.TestResultMethodNode;
import org.eclipse.che.plugin.testing.ide.view.navigation.nodes.TestResultRootNode;
import org.eclipse.che.plugin.testing.ide.view.navigation.nodes.TestResultTraceFrameNode;

/**
 * Implementation for TestResult view. Uses tree for presenting test results.
 *
 * @author Mirage Abeysekara
 */
@Singleton
public class TestResultViewImpl extends BaseView<TestResultView.ActionDelegate>
    implements TestResultView, TestClassNavigation {

  interface TestResultViewImplUiBinder extends UiBinder<Widget, TestResultViewImpl> {}

  interface Styles extends CssResource {

    String traceOutputMessage();

    String traceOutputStack();
  }

  private static final TestResultViewImplUiBinder UI_BINDER =
      GWT.create(TestResultViewImplUiBinder.class);

  private final JavaNavigationService javaNavigationService;
  private final AppContext appContext;
  private final EditorAgent editorAgent;
  private final EventBus eventBus;
  private final TestResultNodeFactory nodeFactory;
  private TestResult lastTestResult;
  private int lastWentLine = 0;
  private boolean showFailuresOnly = false;

  @UiField(provided = true)
  SplitLayoutPanel splitLayoutPanel;

  @UiField Styles style;

  @UiField DockLayoutPanel navigationPanel;

  @UiField FlowPanel traceOutputPanel;

  @Inject
  public TestResultViewImpl(
      PartStackUIResources resources,
      JavaNavigationService javaNavigationService,
      EditorAgent editorAgent,
      AppContext appContext,
      EventBus eventBus,
      TestResultNodeFactory nodeFactory) {
    super(resources);
    this.javaNavigationService = javaNavigationService;
    this.editorAgent = editorAgent;
    this.appContext = appContext;
    this.eventBus = eventBus;
    this.nodeFactory = nodeFactory;
    splitLayoutPanel = new SplitLayoutPanel(1);
    setContentWidget(UI_BINDER.createAndBindUi(this));
  }

  private Tree createTree() {
    NodeUniqueKeyProvider idProvider =
        new NodeUniqueKeyProvider() {
          @NotNull
          @Override
          public String getKey(@NotNull Node item) {
            return String.valueOf(item.hashCode());
          }
        };
    NodeStorage nodeStorage = new NodeStorage(idProvider);
    NodeLoader nodeLoader = new NodeLoader(Collections.<NodeInterceptor>emptySet());
    Tree tree = new Tree(nodeStorage, nodeLoader);
    tree.getSelectionModel().setSelectionMode(SINGLE);
    return tree;
  }

  /** {@inheritDoc} */
  @Override
  @Deprecated
  public void showResults(TestResult result) {
    clear();
    lastTestResult = result;
    setTitle("Test Results (Framework: " + result.getTestFramework() + ")");
    fillNavigationPanel(result);
    focusView();
  }

  /** {@inheritDoc} */
  @Override
  public void showResults(TestResultRootDto result) {
    setTitle("Test Results (Framework: " + result.getTestFrameworkName() + ")");
    fillNavigationPanel(result);
    focusView();
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    setTitle("");
    navigationPanel.clear();
    traceOutputPanel.clear();
  }

  @Deprecated
  private void fillNavigationPanel(final TestResult result) {
    Tree resultTree = buildResultTree(result);
    TestResultGroupNode root =
        nodeFactory.getTestResultGroupNode(
            lastTestResult,
            showFailuresOnly,
            new Runnable() {
              @Override
              public void run() {
                showFailuresOnly = !showFailuresOnly;
                clear();
                fillNavigationPanel(result);
              }
            });
    HashMap<String, List<Node>> classNodeHashMap = new LinkedHashMap<>();
    for (TestCase testCase : lastTestResult.getTestCases()) {
      if (!testCase.isFailed() && showFailuresOnly) {
        continue;
      }
      if (!classNodeHashMap.containsKey(testCase.getClassName())) {
        List<Node> methodNodes = new ArrayList<>();
        classNodeHashMap.put(testCase.getClassName(), methodNodes);
      }
      classNodeHashMap
          .get(testCase.getClassName())
          .add(
              nodeFactory.getTestResultMethodNodeNode(
                  !testCase.isFailed(),
                  testCase.getMethod(),
                  testCase.getTrace(),
                  testCase.getMessage(),
                  testCase.getFailingLine(),
                  this));
    }
    List<Node> classNodes = new ArrayList<>();
    for (Map.Entry<String, List<Node>> entry : classNodeHashMap.entrySet()) {
      TestResultClassNode classNode = nodeFactory.getTestResultClassNodeNode(entry.getKey());
      classNode.setChildren(entry.getValue());
      classNodes.add(classNode);
    }
    root.setChildren(classNodes);
    navigationPanel.add(resultTree);
    resultTree.getNodeStorage().add(root);
    resultTree.expandAll();
  }

  private void fillNavigationPanel(TestResultRootDto result) {
    Tree resultTree = buildResultTree(result);
    TestResultRootNode root =
        nodeFactory.createTestResultRootNode(result, result.getTestFrameworkName());
    resultTree.getNodeStorage().add(root);
    navigationPanel.add(resultTree);
  }

  @Deprecated
  private Tree buildResultTree(TestResult result) {
    Tree resultTree = createTree();
    resultTree
        .getSelectionModel()
        .addSelectionHandler(
            new SelectionHandler<Node>() {
              @Override
              public void onSelection(SelectionEvent<Node> event) {
                Node selectedNode = event.getSelectedItem();
                if (selectedNode instanceof TestResultMethodNode) {
                  fillOutputPanel(((TestResultMethodNode) selectedNode).getStackTrace());
                }
              }
            });
    return resultTree;
  }

  private Tree buildResultTree(TestResultRootDto result) {
    Tree resultTree = createTree();
    resultTree
        .getSelectionModel()
        .addSelectionHandler(
            new SelectionHandler<Node>() {
              @Override
              public void onSelection(SelectionEvent<Node> event) {
                Node selectedNode = event.getSelectedItem();
                if (selectedNode instanceof AbstractTestResultTreeNode) {
                  fillOutputPanel((AbstractTestResultTreeNode) selectedNode);
                }
              }
            });
    return resultTree;
  }

  @Deprecated
  private void fillOutputPanel(String text) {
    traceOutputPanel.clear();
    Label traceMessageLabel = new Label(text);
    traceMessageLabel.setStyleName(style.traceOutputMessage());
    traceOutputPanel.add(traceMessageLabel);
  }

  private void fillOutputPanel(AbstractTestResultTreeNode node) {
    traceOutputPanel.clear();
    TestResultTraceDto testTrace = node.getTestTrace();
    if (testTrace == null) return;
    Label traceOutputMessage = new Label(testTrace.getMessage());
    traceOutputMessage.setStyleName(style.traceOutputMessage());
    traceOutputMessage.setWordWrap(true);
    traceOutputPanel.add(traceOutputMessage);
    Tree traceTree = buildTraceTree(testTrace);
    DockLayoutPanel traceOutputStack = new DockLayoutPanel(Unit.PX);
    traceOutputStack.setStyleName(style.traceOutputStack());
    traceOutputStack.add(traceTree);
    traceOutputPanel.add(traceOutputStack);
  }

  private Tree buildTraceTree(TestResultTraceDto trace) {
    Tree traceTree = createTree();
    List<Node> traceNodes = new ArrayList<>();
    for (TestResultTraceFrameDto traceFrame : trace.getTraceFrames()) {
      TestResultTraceFrameNode traceNode = nodeFactory.createTestResultTraceFrameNode(traceFrame);
      traceNodes.add(traceNode);
    }
    traceTree.getNodeStorage().add(traceNodes);
    return traceTree;
  }

  @Override
  @Deprecated
  public void gotoClass(final String packagePath, String className, String methodName, int line) {
    if (lastTestResult == null) {
      return;
    }
    String projectPath = lastTestResult.getProjectPath();
    if (projectPath == null) {
      return;
    }

    lastWentLine = line;
    String testSrcPath = projectPath + "/" + DEFAULT_TEST_SOURCE_FOLDER;
    appContext
        .getWorkspaceRoot()
        .getFile(testSrcPath + "/" + packagePath)
        .then(
            new Operation<Optional<File>>() {
              @Override
              public void apply(Optional<File> maybeFile) throws OperationException {
                if (maybeFile.isPresent()) {
                  File file = maybeFile.get();
                  editorAgent.openEditor(file);
                  Timer t =
                      new Timer() {
                        @Override
                        public void run() {
                          EditorPartPresenter editorPart = editorAgent.getActiveEditor();
                          final Document doc = ((TextEditor) editorPart).getDocument();
                          if (line == -1 && className != null && methodName != null) {
                            Promise<CompilationUnit> cuPromise =
                                javaNavigationService.getCompilationUnit(
                                    file.getProject().getLocation(), className, true);
                            cuPromise.then(
                                new Operation<CompilationUnit>() {
                                  @Override
                                  public void apply(CompilationUnit cu) throws OperationException {
                                    for (Type type : cu.getTypes()) {
                                      if (type.isPrimary()) {
                                        for (Method m : type.getMethods()) {
                                          if (methodName.equals(m.getElementName())) {
                                            Region methodRegion = m.getFileRegion();
                                            if (methodRegion != null) {
                                              lastWentLine =
                                                  doc.getLineAtOffset(methodRegion.getOffset());
                                              doc.setCursorPosition(
                                                  new TextPosition(lastWentLine - 1, 0));
                                            }
                                          }
                                        }
                                      }
                                    }
                                  }
                                });
                          } else {
                            doc.setCursorPosition(new TextPosition(lastWentLine - 1, 0));
                          }
                        }
                      };
                  t.schedule(1000);
                }
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError error) throws OperationException {
                Log.info(TestResultViewImpl.class, error);
              }
            });
  }
}
