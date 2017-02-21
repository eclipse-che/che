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
package org.eclipse.che.plugin.testing.ide.view;

import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_TEST_SOURCE_FOLDER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.testing.shared.Failure;
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
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
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

import com.google.common.base.Optional;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Implementation for TestResult view. Uses tree for presenting test results.
 *
 * @author Mirage Abeysekara
 */
@Singleton
public class TestResultViewImpl extends BaseView<TestResultView.ActionDelegate>
        implements TestResultView, TestClassNavigation {

    interface TestResultViewImplUiBinder extends UiBinder<Widget, TestResultViewImpl> {
    }

    interface Styles extends CssResource {

        String traceFrameMessage();

    }

    private static final TestResultViewImplUiBinder UI_BINDER = GWT.create(TestResultViewImplUiBinder.class);

    private final AppContext appContext;
    private final EditorAgent editorAgent;
    private final EventBus eventBus;
    private final TestResultNodeFactory nodeFactory;
    private Tree resultTree;
    private Tree traceTree;
    private int lastWentLine = 0;

    @UiField(provided = true)
    SplitLayoutPanel splitLayoutPanel;

    @UiField
    Styles style;

    @UiField
    FlowPanel navigationPanel;

    @UiField
    FlowPanel traceOutputPanel;

    @Inject
    public TestResultViewImpl(PartStackUIResources resources, EditorAgent editorAgent, AppContext appContext,
            EventBus eventBus, TestResultNodeFactory nodeFactory) {
        super(resources);
        this.editorAgent = editorAgent;
        this.appContext = appContext;
        this.eventBus = eventBus;
        this.nodeFactory = nodeFactory;
        splitLayoutPanel = new SplitLayoutPanel(1);
        setContentWidget(UI_BINDER.createAndBindUi(this));
        resultTree = createTree();
        resultTree.getSelectionModel().addSelectionHandler(new SelectionHandler<Node>() {
            @Override
            public void onSelection(SelectionEvent<Node> event) {
                Node selectedNode = event.getSelectedItem();
                if (selectedNode instanceof TestResultMethodNode) {
                    fillOutputPanel(((TestResultMethodNode) selectedNode).getStackTrace());
                }
                if (selectedNode instanceof AbstractTestResultTreeNode) {
                    fillOutputPanel((AbstractTestResultTreeNode) selectedNode);
                }
            }
        });
        navigationPanel.add(resultTree);
    }

    private Tree createTree() {
        NodeUniqueKeyProvider idProvider = new NodeUniqueKeyProvider() {
            @NotNull
            @Override
            public String getKey(@NotNull Node item) {
                return String.valueOf(item.hashCode());
            }
        };
        NodeStorage nodeStorage = new NodeStorage(idProvider);
        NodeLoader nodeLoader = new NodeLoader(Collections.<NodeInterceptor> emptySet());
        Tree tree = new Tree(nodeStorage, nodeLoader);
        tree.getElement().getStyle().setWidth(100, Style.Unit.PCT);
        tree.getElement().getStyle().setHeight(100, Style.Unit.PCT);
        return tree;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void focusView() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public void showResults(TestResult result) {
        setTitle("Test Results (Framework: " + result.getTestFramework() + ")");
        buildResultTree(result);
        focusView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showResults(TestResultRootDto result) {
        setTitle("Test Results (Framework: " + result.getTestFrameworkName() + ")");
        buildResultTree(result);
        focusView();
    }

    @Deprecated
    private void buildResultTree(TestResult result) {
        resultTree.getNodeStorage().clear();
        // outputResult.setText("");
        TestResultGroupNode root = nodeFactory.getTestResultGroupNode(result);
        Map<String, List<Node>> classNodeHashMap = new HashMap<>();
        for (Failure failure : result.getFailures()) {
            if (!classNodeHashMap.containsKey(failure.getFailingClass())) {
                List<Node> methodNodes = new ArrayList<>();
                classNodeHashMap.put(failure.getFailingClass(), methodNodes);
            }
            classNodeHashMap.get(failure.getFailingClass())
                    .add(nodeFactory.getTestResultMethodNodeNode(failure.getFailingMethod(), failure.getTrace(),
                            failure.getMessage(), failure.getFailingLine(), this));
        }
        List<Node> classNodes = new ArrayList<>();
        for (Map.Entry<String, List<Node>> entry : classNodeHashMap.entrySet()) {
            TestResultClassNode classNode = nodeFactory.getTestResultClassNodeNode(entry.getKey());
            classNode.setChildren(entry.getValue());
            classNodes.add(classNode);
        }
        root.setChildren(classNodes);
        resultTree.getNodeStorage().add(root);
    }

    private void buildResultTree(TestResultRootDto result) {
        resultTree.getNodeStorage().clear();
        TestResultRootNode root = nodeFactory.createTestResultRootNode(result, result.getTestFrameworkName());
        resultTree.getNodeStorage().add(root);
    }

    private void buildTraceTree(TestResultTraceDto trace) {
        traceTree = createTree();
        traceTree.getNodeStorage().clear();
        List<Node> traceNodes = new ArrayList<>();
        for (TestResultTraceFrameDto traceFrame : trace.getTraceFrames()) {
            TestResultTraceFrameNode traceNode = nodeFactory.createTestResultTraceFrameNode(traceFrame);
            traceNodes.add(traceNode);
        }
        traceTree.getNodeStorage().add(traceNodes);
    }

    @Deprecated
    private void fillOutputPanel(String text) {
        traceOutputPanel.clear();
        Label traceMessageLabel = new Label(text);
        traceMessageLabel.setStyleName(style.traceFrameMessage());
        traceOutputPanel.add(traceMessageLabel);
    }

    private void fillOutputPanel(AbstractTestResultTreeNode node) {
        traceOutputPanel.clear();
        TestResultTraceDto testTrace = node.getTestTrace();
        if (testTrace == null)
            return;
        Label traceMessageLabel = new Label(testTrace.getMessage());
        traceMessageLabel.setStyleName(style.traceFrameMessage());
        traceOutputPanel.add(traceMessageLabel);
        buildTraceTree(testTrace);
        traceTree.getElement().getStyle().setOverflow(Style.Overflow.HIDDEN);
        traceOutputPanel.add(traceTree);
    }

    @Override
    @Deprecated
    public void gotoClass(String packagePath, int line) {
        lastWentLine = line;
        final Project project = appContext.getRootProject();
        String testSrcPath = project.getPath() + "/" + DEFAULT_TEST_SOURCE_FOLDER;
        appContext.getWorkspaceRoot().getFile(testSrcPath + packagePath).then(new Operation<Optional<File>>() {
            @Override
            public void apply(Optional<File> file) throws OperationException {
                if (file.isPresent()) {
                    eventBus.fireEvent(FileEvent.createOpenFileEvent(file.get()));
                    Timer t = new Timer() {
                        @Override
                        public void run() {
                            EditorPartPresenter editorPart = editorAgent.getActiveEditor();
                            Document doc = ((TextEditor) editorPart).getDocument();
                            doc.setCursorPosition(new TextPosition(lastWentLine - 1, 0));
                        }
                    };
                    t.schedule(500);
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                Log.info(TestResultViewImpl.class, error);
            }
        });
    }
}
