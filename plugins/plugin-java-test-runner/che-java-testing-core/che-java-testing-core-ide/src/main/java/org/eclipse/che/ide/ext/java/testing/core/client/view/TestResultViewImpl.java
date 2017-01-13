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
package org.eclipse.che.ide.ext.java.testing.core.client.view;

import com.google.common.base.Optional;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
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

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
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
import org.eclipse.che.ide.ext.java.testing.core.client.view.navigation.TestClassNavigation;
import org.eclipse.che.ide.ext.java.testing.core.client.view.navigation.factory.TestResultNodeFactory;
import org.eclipse.che.ide.ext.java.testing.core.client.view.navigation.nodes.TestResultClassNode;
import org.eclipse.che.ide.ext.java.testing.core.client.view.navigation.nodes.TestResultGroupNode;
import org.eclipse.che.ide.ext.java.testing.core.client.view.navigation.nodes.TestResultMethodNode;
import org.eclipse.che.ide.ext.java.testing.core.shared.Failure;
import org.eclipse.che.ide.ext.java.testing.core.shared.TestResult;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.NodeUniqueKeyProvider;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_TEST_SOURCE_FOLDER;

/**
 * Implementation for TestResult view.
 * Uses tree for presenting test results.
 *
 * @author Mirage Abeysekara
 */
@Singleton
public class TestResultViewImpl extends BaseView<TestResultView.ActionDelegate> implements TestResultView, TestClassNavigation {

    interface TestResultViewImplUiBinder extends UiBinder<Widget, TestResultViewImpl> {
    }

    private static final TestResultViewImplUiBinder UI_BINDER = GWT.create(TestResultViewImplUiBinder.class);

    private final AppContext appContext;
    private final EditorAgent editorAgent;
    private final EventBus eventBus;
    private final TestResultNodeFactory nodeFactory;
    private TestResult lastTestResult;
    private Tree resultTree;
    private int lastWentLine = 0;

    @UiField(provided = true)
    SplitLayoutPanel splitLayoutPanel;

    @UiField
    Label outputResult;

    @UiField
    FlowPanel navigationPanel;

    @Inject
    public TestResultViewImpl(PartStackUIResources resources,
                              EditorAgent editorAgent,
                              AppContext appContext,
                              EventBus eventBus,
                              TestResultNodeFactory nodeFactory) {
        super(resources);
        this.editorAgent = editorAgent;
        this.appContext = appContext;
        this.eventBus = eventBus;
        this.nodeFactory = nodeFactory;
        splitLayoutPanel = new SplitLayoutPanel(1);
        setContentWidget(UI_BINDER.createAndBindUi(this));

        NodeUniqueKeyProvider idProvider = new NodeUniqueKeyProvider() {
            @NotNull
            @Override
            public String getKey(@NotNull Node item) {
                return String.valueOf(item.hashCode());
            }
        };

        NodeStorage nodeStorage = new NodeStorage(idProvider);
        NodeLoader nodeLoader = new NodeLoader(Collections.<NodeInterceptor>emptySet());

        resultTree = new Tree(nodeStorage, nodeLoader);

        resultTree.getSelectionModel().addSelectionHandler(new SelectionHandler<Node>() {
            @Override
            public void onSelection(SelectionEvent<Node> event) {
                Node methodNode = event.getSelectedItem();
                if (methodNode instanceof TestResultMethodNode) {
                    outputResult.setText(((TestResultMethodNode) methodNode).getStackTrace());
                }
                //Log.info(TestResultViewImpl.class, event.getSelectedItem().getName());
            }
        });

        resultTree.getElement().getStyle().setWidth(100, Style.Unit.PCT);
        resultTree.getElement().getStyle().setHeight(100, Style.Unit.PCT);
        navigationPanel.add(resultTree);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void focusView() {
//        tree.setFocus(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showResults(TestResult result) {

        this.lastTestResult = result;
        setTitle("Test Results (Framework: " + result.getTestFramework() + ")");

        buildTree();

        focusView();
    }

    private void buildTree() {

        resultTree.getNodeStorage().clear();
        outputResult.setText("");

        TestResultGroupNode root = nodeFactory.getTestResultGroupNode(lastTestResult);

        HashMap<String, List<Node>> classNodeHashMap = new HashMap<>();

        for (Failure failure : lastTestResult.getFailures()) {
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

    @Override
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
