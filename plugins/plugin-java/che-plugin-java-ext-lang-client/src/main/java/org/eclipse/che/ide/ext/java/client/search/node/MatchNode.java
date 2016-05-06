/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.client.search.node;

import elemental.html.SpanElement;

import com.google.gwt.dom.client.Element;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.OpenEditorCallbackImpl;
import org.eclipse.che.ide.api.project.node.HasAction;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;
import org.eclipse.che.ide.ext.java.client.project.node.jar.JarFileNode;
import org.eclipse.che.ide.ext.java.shared.JarEntry;
import org.eclipse.che.ide.ext.java.shared.dto.Region;
import org.eclipse.che.ide.ext.java.shared.dto.model.ClassFile;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.search.Match;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.TreeStyles;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.ide.util.dom.Elements;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Node represent Match for find usages search.
 *
 * @author Evgen Vidolob
 */
public class MatchNode extends AbstractPresentationNode implements HasAction {

    private TreeStyles      styles;
    private JavaResources   resources;
    private EditorAgent     editorAgent;
    private ProjectExplorerPresenter
                            projectExplorer;
    private DtoFactory      dtoFactory;
    private JavaNodeManager javaNodeManager;
    private AppContext      appContext;
    private Match           match;
    private CompilationUnit compilationUnit;
    private ClassFile       classFile;

    @Inject
    public MatchNode(TreeStyles styles,
                     JavaResources resources,
                     EditorAgent editorAgent,
                     ProjectExplorerPresenter projectExplorer,
                     DtoFactory dtoFactory,
                     JavaNodeManager javaNodeManager,
                     AppContext appContext,
                     @Assisted Match match,
                     @Nullable @Assisted CompilationUnit compilationUnit, @Nullable @Assisted ClassFile classFile) {
        this.styles = styles;
        this.resources = resources;
        this.editorAgent = editorAgent;
        this.projectExplorer = projectExplorer;
        this.dtoFactory = dtoFactory;
        this.javaNodeManager = javaNodeManager;
        this.appContext = appContext;
        this.match = match;
        this.compilationUnit = compilationUnit;
        this.classFile = classFile;
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return null;
    }

    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        SpanElement spanElement = Elements.createSpanElement(styles.styles().presentableTextContainer());

        SpanElement lineNumberElement = Elements.createSpanElement();
        lineNumberElement.setInnerHTML(String.valueOf(match.getMatchLineNumber() + 1) + ":&nbsp;&nbsp;&nbsp;");
        spanElement.appendChild(lineNumberElement);

        SpanElement textElement = Elements.createSpanElement();
        Region matchInLine = match.getMatchInLine();
        String matchedLine = match.getMatchedLine();
        if (matchedLine != null && matchInLine != null) {
            String startLine = matchedLine.substring(0, matchInLine.getOffset());
            textElement.appendChild(Elements.createTextNode(startLine));
            SpanElement highlightElement = Elements.createSpanElement(resources.css().searchMatch());
            highlightElement
                    .setInnerText(matchedLine.substring(matchInLine.getOffset(), matchInLine.getOffset() + matchInLine.getLength()));
            textElement.appendChild(highlightElement);

            textElement.appendChild(Elements.createTextNode(matchedLine.substring(matchInLine.getOffset() + matchInLine.getLength())));
        } else {
            textElement.appendChild(Elements.createTextNode("Can't find sources"));
        }
        spanElement.appendChild(textElement);

        presentation.setPresentableIcon(resources.searchMatch());
        presentation.setUserElement((Element)spanElement);
    }

    @Override
    public String getName() {
        return match.getMatchedLine();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    public Match getMatch() {
        return match;
    }

    @Override
    public void actionPerformed() {
        if (compilationUnit != null) {
            EditorPartPresenter editorPartPresenter = editorAgent.getOpenedEditor(Path.valueOf(compilationUnit.getPath()));
            if (editorPartPresenter != null) {
                editorAgent.activateEditor(editorPartPresenter);
                fileOpened(editorPartPresenter);
                return;
            }

            projectExplorer.getNodeByPath(new HasStorablePath.StorablePath(compilationUnit.getPath()))
                           .then(selectNode())
                           .then(openNode());
        } else if (classFile != null) {
            String className = classFile.getElementName();

            JarEntry jarEntry = dtoFactory.createDto(JarEntry.class);
            jarEntry.setName(className);
            jarEntry.setType(JarEntry.JarEntryType.CLASS_FILE);
            jarEntry.setPath(classFile.getPath());

            JarFileNode jarFileNode = javaNodeManager.getJavaNodeFactory()
                                                     .newJarFileNode(jarEntry,
                                                                     null,
                                                                     appContext.getCurrentProject().getProjectConfig(),
                                                                     javaNodeManager.getJavaSettingsProvider().getSettings());
            openFile(jarFileNode);
        }
    }

    private Function<Node, Node> selectNode() {
        return new Function<Node, Node>() {
            @Override
            public Node apply(Node node) throws FunctionException {
                projectExplorer.select(node, false);
                return node;
            }
        };
    }

    private Function<Node, Node> openNode() {
        return new Function<Node, Node>() {
            @Override
            public Node apply(Node node) throws FunctionException {
                if (node instanceof FileReferenceNode) {
                    openFile((VirtualFile)node);
                }

                return node;
            }
        };
    }

    private void openFile(VirtualFile result) {
        editorAgent.openEditor(result, new OpenEditorCallbackImpl() {
            @Override
            public void onEditorOpened(EditorPartPresenter editor) {
                fileOpened(editor);
            }
        });
    }

    private void fileOpened(EditorPartPresenter editor) {
        if (editor instanceof TextEditorPresenter) {
            ((TextEditorPresenter)editor).getDocument().setSelectedRange(
                    LinearRange.createWithStart(match.getFileMatchRegion().getOffset()).andLength(match.getFileMatchRegion().getLength()),
                    true);
        }
    }

}
