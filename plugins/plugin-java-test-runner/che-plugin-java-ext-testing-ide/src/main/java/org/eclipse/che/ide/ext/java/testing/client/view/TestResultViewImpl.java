/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p>
 * Contributors:
 * Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.testing.client.view;

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
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.events.CursorActivityEvent;
import org.eclipse.che.ide.api.editor.events.CursorActivityHandler;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.NodeInterceptor;
//import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.java.testing.client.view.navigation.TestClassNavigation;
import org.eclipse.che.ide.ext.java.testing.client.view.navigation.nodes.TestResultClassNode;
import org.eclipse.che.ide.ext.java.testing.client.view.navigation.nodes.TestResultGroupNode;
import org.eclipse.che.ide.ext.java.testing.client.view.navigation.nodes.TestResultMethodNode;
import org.eclipse.che.ide.ext.java.testing.shared.Failure;
import org.eclipse.che.ide.ext.java.testing.shared.TestResult;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.search.factory.FindResultNodeFactory;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.NodeUniqueKeyProvider;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.util.loging.Log;
//import org.eclipse.che.ide.ui.tree.Tree;
//import org.eclipse.che.ide.ui.smartTree.NodeUniqueKeyProvider;
//import org.eclipse.che.ide.ui.smartTree.Tree;
//import org.eclipse.che.ide.ui.smartTree.NodeLoader;
//import org.eclipse.che.ide.ui.smartTree.NodeStorage;
//import org.eclipse.che.ide.ui.smartTree.UniqueKeyProvider;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.OPEN;

/**
 * Implementation for FindResult view.
 * Uses tree for presenting search results.
 *
 * @author Valeriy Svydenko
 */
@Singleton
class TestResultViewImpl extends BaseView<TestResultView.ActionDelegate> implements TestResultView, TestClassNavigation {

//    private final Tree                  tree;
//    private final FindResultNodeFactory findResultNodeFactory;

    interface TestResultViewImplUiBinder extends UiBinder<Widget, TestResultViewImpl> {
    }

    @UiField(provided = true)
    SplitLayoutPanel splitLayoutPanel;

    private final AppContext appContext;
    private final ProjectExplorerPresenter projectExplorer;
    private final EditorAgent editorAgent;
    private final EventBus eventBus;

    //    @UiField(provided = true)
//    Tree<String> processTree;
    private TestResult lastTestResult;
    private Tree resultTree;
    private int lastWentLine = 0;
    @UiField
    Label outputResult;

    @UiField
    FlowPanel navigationPanel;

    @Inject
    public TestResultViewImpl(PartStackUIResources resources,
                              EditorAgent editorAgent,
                              AppContext appContext,
                              ProjectExplorerPresenter projectExplorer,
                              EventBus eventBus,
                              FindResultNodeFactory findResultNodeFactory,
                              CoreLocalizationConstant localizationConstant,
                              TestResultViewImplUiBinder uiBinder) {
        super(resources);

        this.editorAgent = editorAgent;
        this.appContext = appContext;
        this.projectExplorer = projectExplorer;
        this.eventBus = eventBus;
        splitLayoutPanel = new SplitLayoutPanel(1);
        setContentWidget(uiBinder.createAndBindUi(this));

//        splitLayoutPanel.getElement().getStyle().setPropertyPx("width", 1);
//        NodeList<com.google.gwt.dom.client.Node> nodes = splitLayoutPanel.getElement().getChildNodes();
//        for (int i = 0; i < nodes.getLength(); i++) {
//            com.google.gwt.dom.client.Node node = nodes.getItem(i);
//            if (node.hasChildNodes()) {
//                com.google.gwt.dom.client.Element el = node.getFirstChild().cast();
//                if ("gwt-SplitLayoutPanel-HDragger".equals(el.getClassName())) {
//                    el.getStyle().setPropertyPx("width", 2);
//                }
//            }
//        }
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
                Log.info(TestResultViewImpl.class, event.getSelectedItem().getName());
            }
        });

        resultTree.getElement().getStyle().setWidth(100, Style.Unit.PCT);
        resultTree.getElement().getStyle().setHeight(100, Style.Unit.PCT);
        navigationPanel.add(resultTree);

//        this.findResultNodeFactory = findResultNodeFactory;

//        UniqueKeyProvider<Node> nodeIdProvider = new NodeUniqueKeyProvider() {
//            @NotNull
//            @Override
//            public String getKey(@NotNull Node item) {
//                if (item instanceof HasStorablePath) {
//                    return ((HasStorablePath)item).getStorablePath();
//                } else {
//                    return String.valueOf(item.hashCode());
//                }
//            }
//        };
//
//        NodeStorage nodeStorage = new NodeStorage(nodeIdProvider);
//        NodeLoader loader = new NodeLoader(Collections.<NodeInterceptor>emptySet());
//        tree = new Tree(nodeStorage, loader);

//        setContentWidget(tree);

//        tree.setAutoSelect(true);

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
//        tree.getNodeStorage().clear();
//        tree.getNodeStorage().add(findResultNodeFactory.newResultNode(nodes, request));
//        tree.expandAll();
//        tree.getSelectionModel().select(tree.getRootNodes().get(0), false);
        this.lastTestResult = result;
        setTitle("Test Results (Framework: " + result.getTestFramework() + ")");

        buildTree();

        focusView();
//        outputResult.getElement().getStyle().setOverflow(Style.Overflow.SCROLL);
//        outputResult.getElement().getStyle().setProperty("webkitUserSelect","text");
    }

    private void buildTree() {

        resultTree.getNodeStorage().clear();
        outputResult.setText("");

        TestResultGroupNode root = new TestResultGroupNode(lastTestResult);

        HashMap<String, List<Node>> classNodeHashMap = new HashMap<>();

        for (Failure failure : lastTestResult.getFailures()) {
            if (!classNodeHashMap.containsKey(failure.getFailingClass())) {
                List<Node> methodNodes = new ArrayList<>();
                classNodeHashMap.put(failure.getFailingClass(), methodNodes);
            }
            classNodeHashMap.get(failure.getFailingClass())
                    .add(new TestResultMethodNode(failure.getFailingMethod(), failure.getTrace(), failure.getMessage(),
                            failure.getFailingLine(), this));
        }

        List<Node> classNodes = new ArrayList<>();
        for (Map.Entry<String, List<Node>> entry : classNodeHashMap.entrySet()) {
            TestResultClassNode classNode = new TestResultClassNode(entry.getKey());
            classNode.setChildren(entry.getValue());
            classNodes.add(classNode);
        }

        root.setChildren(classNodes);

        resultTree.getNodeStorage().add(root);

    }

    @Override
    public void gotoClass(String packagePath, int line) {

        EditorPartPresenter editorPart = editorAgent.getActiveEditor();
        final VirtualFile file = editorPart.getEditorInput().getFile();
//        appContext.getCurrentProject().
//        Document doc = ((TextEditor) editorPart).getDocument();
//        Log.info(TestRunnerPresenter.class, doc.getLineStart(2));
//        doc.setCursorPosition(new TextPosition(5, 0));
//        Log.info(TestRunnerPresenter.class, file);
        lastWentLine = line;
        String testSrcPath = appContext.getCurrentProject().getRootProject().getPath() + "/src/test/java/";

        Log.info(TestResultViewImpl.class, "hi");
        Log.info(TestResultViewImpl.class, testSrcPath + packagePath);
        projectExplorer.getNodeByPath(new HasStorablePath.StorablePath(testSrcPath + packagePath)).then(new Operation<Node>() {
            @Override
            public void apply(final Node node) throws OperationException {
                Log.info(TestResultViewImpl.class, node);
                if (!(node instanceof FileReferenceNode)) {
                    return;
                }
                eventBus.fireEvent(new FileEvent((VirtualFile) node, OPEN));
//
//                // // TODO: 6/8/16 find a way to get a call back
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
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                Log.info(TestResultViewImpl.class, error);
            }
        });
    }
}
