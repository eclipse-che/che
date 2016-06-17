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
package org.eclipse.che.ide.part.editor.recent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.data.tree.HasAttributes;
import org.eclipse.che.ide.api.project.node.HasProjectConfig;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.NodeInterceptor;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.ui.smartTree.KeyboardNavigationHandler;
import org.eclipse.che.ide.ui.smartTree.NodeUniqueKeyProvider;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.event.SelectionChangedEvent;
import org.eclipse.che.ide.ui.smartTree.presentation.DefaultPresentationRenderer;
import org.eclipse.che.ide.ui.window.Window;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

import static com.google.gwt.dom.client.Style.Overflow.AUTO;
import static org.eclipse.che.ide.part.editor.recent.RecentFileStore.getShortPath;
import static org.eclipse.che.ide.project.node.AbstractProjectBasedNode.CUSTOM_BACKGROUND_FILL;
import static org.eclipse.che.ide.ui.smartTree.SelectionModel.Mode.SINGLE;

/**
 * Implementation for the {@link OpenRecentFilesView}.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class OpenRecentFileViewImpl extends Window implements OpenRecentFilesView {

    interface OpenRecentFileViewImplUiBinder extends UiBinder<Widget, OpenRecentFileViewImpl> {
    }

    private static OpenRecentFileViewImplUiBinder uiBinder = GWT.create(OpenRecentFileViewImplUiBinder.class);

    @UiField
    DockLayoutPanel content;

    private Tree  tree;
    private Label pathLabel;

    @Inject
    public OpenRecentFileViewImpl(CoreLocalizationConstant locale, Styles styles) {

        setWidget(uiBinder.createAndBindUi(this));

        styles.css().ensureInjected();

        pathLabel = new Label();
        pathLabel.setStyleName(styles.css().label());

        NodeStorage storage = new NodeStorage(new NodeUniqueKeyProvider() {
            @Override
            public String getKey(@NotNull Node item) {
                if (item instanceof HasStorablePath) {
                    return ((HasStorablePath)item).getStorablePath();
                } else {
                    return String.valueOf(item.hashCode());
                }
            }
        });
        NodeLoader loader = new NodeLoader(Collections.<NodeInterceptor>emptySet());
        tree = new Tree(storage, loader);
        tree.setPresentationRenderer(new DefaultPresentationRenderer<Node>(tree.getTreeStyles()) {
            @Override
            public Element render(Node node, String domID, Tree.Joint joint, int depth) {
                Element element = super.render(node, domID, joint, depth);

                element.setAttribute("name", node.getName());

                if (node instanceof HasStorablePath) {
                    element.setAttribute("path", ((HasStorablePath)node).getStorablePath());
                }

                if (node instanceof HasProjectConfig) {
                    element.setAttribute("project", ((HasProjectConfig)node).getProjectConfig().getPath());
                }

                if (node instanceof HasAttributes && ((HasAttributes)node).getAttributes().containsKey(CUSTOM_BACKGROUND_FILL)) {
                    element.getFirstChildElement().getStyle()
                           .setBackgroundColor(((HasAttributes)node).getAttributes().get(CUSTOM_BACKGROUND_FILL).get(0));
                }

                return element;
            }
        });
        tree.setAutoSelect(true);
        tree.getSelectionModel().setSelectionMode(SINGLE);
        tree.getSelectionModel().addSelectionChangedHandler(new SelectionChangedEvent.SelectionChangedHandler() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                List<Node> selection = event.getSelection();
                if (selection == null || selection.isEmpty()) {
                    pathLabel.setText("");
                    pathLabel.setTitle("");
                    return;
                }

                Node head = selection.get(0);

                if (head instanceof HasStorablePath) {
                    String path = getShortPath(((HasStorablePath)head).getStorablePath());
                    pathLabel.setText(path);
                    pathLabel.setTitle(path);
                    return;
                }

                pathLabel.setText("");
                pathLabel.setTitle("");
            }
        });

        KeyboardNavigationHandler handler = new KeyboardNavigationHandler() {
            @Override
            public void onEnter(NativeEvent evt) {
                hide();
            }
        };

        handler.bind(tree);

        tree.addDomHandler(new DoubleClickHandler() {
            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                hide();
            }
        }, DoubleClickEvent.getType());

        tree.ensureDebugId("recent-files");
        tree.getElement().getStyle().setOverflowY(AUTO);

        content.addSouth(pathLabel, 20.);
        content.add(tree);

        setTitle(locale.openRecentFilesTitle());

        setHideOnEscapeEnabled(true);

        getFooter().setVisible(false);

        getWidget().setStyleName(styles.css().window());

        hideCrossButton();
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
    }

    /** {@inheritDoc} */
    @Override
    public void setRecentFiles(List<FileReferenceNode> recentFiles) {
        tree.getNodeStorage().clear();
        for (FileReferenceNode recentFile : recentFiles) {
            tree.getNodeStorage().add(recentFile);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clearRecentFiles() {
        tree.getNodeStorage().clear();
    }

    /** {@inheritDoc} */
    @Override
    public void show() {
        super.show(tree);
        if (!tree.getRootNodes().isEmpty()) {
            tree.getSelectionModel().select(tree.getRootNodes().get(0), false);
        }
    }

    public interface Styles extends ClientBundle {
        interface Css extends CssResource {
            String window();

            String label();
        }

        @Source("recent.css")
        Css css();
    }
}
