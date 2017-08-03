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
package org.eclipse.che.plugin.testing.ide.view2;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.NodeUniqueKeyProvider;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.plugin.testing.ide.TestResources;
import org.eclipse.che.plugin.testing.ide.model.TestRootState;
import org.eclipse.che.plugin.testing.ide.model.TestState;
import org.eclipse.che.plugin.testing.ide.view.navigation.TestClassNavigation;
import org.eclipse.che.plugin.testing.ide.view2.navigation.factory.TestResultNodeFactory;
import org.eclipse.che.plugin.testing.ide.view2.navigation.nodes.TestRootNode;
import org.eclipse.che.plugin.testing.ide.view2.navigation.nodes.TestStateNode;

import javax.validation.constraints.NotNull;
import java.util.Collections;

/**
 * Implementation for TestResult view. Uses tree for presenting test results.
 *
 * @author Mirage Abeysekara
 */
@Singleton
public class TestResultViewImpl extends BaseView<TestResultView.ActionDelegate>
        implements TestResultView, TestClassNavigation {

    private static final TestResultViewImplUiBinder UI_BINDER = GWT.create(TestResultViewImplUiBinder.class);
    private final JavaNavigationService javaNavigationService;
    private final AppContext            appContext;
    private final EditorAgent           editorAgent;
    private final TestResultNodeFactory nodeFactory;

    @UiField(provided = true)
    SplitLayoutPanel splitLayoutPanel;
    @UiField
    FlowPanel        navigationPanel;
    private Tree resultTree;
    private int     lastWentLine     = 0;
    private boolean showFailuresOnly = false;

    private TestRootState testRootState;
    private TestRootNode  testRootNode;

    @Inject
    public TestResultViewImpl(TestResources testResources,
                              PartStackUIResources resources,
                              JavaNavigationService javaNavigationService,
                              EditorAgent editorAgent,
                              AppContext appContext,
                              TestResultNodeFactory nodeFactory,
                              PrinterOutputConsole outputConsole) {
        super(resources);
        this.javaNavigationService = javaNavigationService;
        this.editorAgent = editorAgent;
        this.appContext = appContext;
        this.nodeFactory = nodeFactory;
        splitLayoutPanel = new SplitLayoutPanel(1);
        setContentWidget(UI_BINDER.createAndBindUi(this));
        splitLayoutPanel.add(outputConsole);

        NodeUniqueKeyProvider idProvider = new NodeUniqueKeyProvider() {
            @NotNull
            @Override
            public String getKey(@NotNull Node item) {
                return String.valueOf(item.hashCode());
            }
        };
        NodeStorage nodeStorage = new NodeStorage(idProvider);
        NodeLoader nodeLoader = new NodeLoader(Collections.emptySet());
        resultTree = new Tree(nodeStorage, nodeLoader);
        resultTree.getSelectionModel().addSelectionHandler(event -> {
            Node methodNode = event.getSelectedItem();
            if (methodNode instanceof TestStateNode) {
                outputConsole.testSelected(((TestStateNode)methodNode).getTestState());
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
                TestStateNode stateNode = (TestStateNode)node;
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
    public void onSuiteFinished(TestState testState) {
    }

    @Override
    public void onSuiteTreeNodeAdded(TestState testState) {
        //addSuiteOrTest(testState);
    }

    @Override
    public void onSuiteTreeStarted(TestState testState) {
        addSuiteOrTest(testState);
    }

    @Override
    public void onSuiteTreeNodeFinished(TestState suite) {
    }

    @Override
    public void onTestsCountInSuite(int count) {

    }

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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void focusView() {
    }

    @Override
    public TestRootState getRootState() {
        testRootState = new TestRootState();
        return testRootState;
    }

    @Override
    public void gotoClass(final String packagePath, String className, String methodName, int line) {
        CompilationUnit cu = null;
//        if (lastTestResult == null) {
//            return;
//        }
//        String projectPath = lastTestResult.getProjectPath();
//        if (projectPath == null) {
//            return;
//        }
//
//        lastWentLine = line;
//        String testSrcPath = projectPath + "/" + DEFAULT_TEST_SOURCE_FOLDER;
//        appContext.getWorkspaceRoot().getFile(testSrcPath + "/" + packagePath).then(new Operation<Optional<File>>() {
//            @Override
//            public void apply(Optional<File> maybeFile) throws OperationException {
//                if (maybeFile.isPresent()) {
//                    File file = maybeFile.get();
//                    editorAgent.openEditor(file);
//                    Timer t = new Timer() {
//                        @Override
//                        public void run() {
//                            EditorPartPresenter editorPart = editorAgent.getActiveEditor();
//                            final Document doc = ((TextEditor) editorPart).getDocument();
//                            if (line == -1 &&
//                                    className != null &&
//                                    methodName != null) {
//                                Promise<CompilationUnit> cuPromise =
//                                        javaNavigationService.getCompilationUnit(file.getProject().getLocation(),
//                                                className, true);
//                                cuPromise.then(new Operation<CompilationUnit>() {
//                                    @Override
//                                    public void apply(CompilationUnit cu) throws OperationException {
//                                        for (Type type : cu.getTypes()) {
//                                            if (type.isPrimary()) {
//                                                for (Method m : type.getMethods()) {
//                                                    if (methodName.equals(m.getElementName())) {
//                                                        Region methodRegion = m.getFileRegion();
//                                                        if (methodRegion != null) {
//                                                            lastWentLine = doc.getLineAtOffset(methodRegion.getOffset());
//                                                            doc.setCursorPosition(new TextPosition(lastWentLine - 1, 0));
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//                                });
//                            } else {
//                                doc.setCursorPosition(new TextPosition(lastWentLine - 1, 0));
//                            }
//                        }
//                    };
//                    t.schedule(1000);
//                }
//            }
//        }).catchError(new Operation<PromiseError>() {
//            @Override
//            public void apply(PromiseError error) throws OperationException {
//                Log.info(TestResultViewImpl.class, error);
//            }
//        });
        throw new UnsupportedOperationException();
    }

    interface TestResultViewImplUiBinder extends UiBinder<Widget, TestResultViewImpl> {
    }
}
