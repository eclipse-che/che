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
package org.eclipse.che.ide.ext.java.client.project.node.jar;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.api.project.node.HasAction;
import org.eclipse.che.ide.api.project.node.HasProjectConfig;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.node.settings.NodeSettings;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;
import org.eclipse.che.ide.ext.java.shared.JarEntry;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.vectomatic.dom.svg.ui.SVGImage;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

import static org.eclipse.che.ide.MimeType.APPLICATION_JAVA_CLASS;

/**
 * It might be used for any jar content.
 *
 * @author Vlad Zhukovskiy
 */
public class JarFileNode extends AbstractJarEntryNode implements VirtualFile, HasAction {

    private final IconRegistry iconRegistry;

    @Inject
    public JarFileNode(@Assisted JarEntry jarEntry,
                       @Nullable @Assisted Integer libId,
                       @Assisted ProjectConfigDto projectConfig,
                       @Assisted NodeSettings nodeSettings,
                       @NotNull JavaNodeManager nodeManager,
                       @NotNull IconRegistry iconRegistry) {
        super(jarEntry, libId, projectConfig, nodeSettings, nodeManager);
        this.iconRegistry = iconRegistry;
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return Promises.resolve(Collections.<Node>emptyList());
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed() {
        nodeManager.getEventBus().fireEvent(new FileEvent(this, FileEvent.FileOperation.OPEN));
    }

    /** {@inheritDoc} */
    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        presentation.setPresentableText(getDisplayName());
        presentation.setPresentableIcon(isClassFile() ? nodeManager.getJavaNodesResources().javaFile()
                                                      : nodeManager.getNodesResources().file());
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getName() {
        return getData().getName();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isLeaf() {
        return true;
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getPath() {
        return getData().getPath();
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayName() {
        if (isClassFile()) {
            return getData().getName().substring(0, getData().getName().lastIndexOf(".class"));
        } else {
            return getData().getName();
        }
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    public String getMediaType() {
        return isClassFile() ? APPLICATION_JAVA_CLASS : null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isReadOnly() {
        return true;
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    public HasProjectConfig getProject() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public String getContentUrl() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Promise<String> getContent() {
        return AsyncPromiseHelper.createFromAsyncRequest(new AsyncPromiseHelper.RequestCall<String>() {
            @Override
            public void makeCall(final AsyncCallback<String> callback) {
                JavaNavigationService javaService = nodeManager.getJavaService();
                if (libId != null) {
                    javaService.getContent(getProjectConfig().getPath(), libId, getData().getPath(),
                                           new AsyncRequestCallback<String>(new StringUnmarshaller()) {
                                               @Override
                                               protected void onSuccess(String result) {
                                                   callback.onSuccess(result);
                                               }

                                               @Override
                                               protected void onFailure(Throwable exception) {
                                                   callback.onFailure(exception);
                                               }
                                           });
                } else {
                    javaService.getContent(getProjectConfig().getPath(), getData().getPath(),
                                           new AsyncRequestCallback<String>(new StringUnmarshaller()) {
                                               @Override
                                               protected void onSuccess(String result) {
                                                   callback.onSuccess(result);
                                               }

                                               @Override
                                               protected void onFailure(Throwable exception) {
                                                   callback.onFailure(exception);
                                               }
                                           });
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Void> updateContent(String content) {
        return Promises.reject(JsPromiseError.create("Update content on class file is not supported."));
    }

    private boolean isClassFile() {
        return getData().getName().endsWith(".class");
    }

    private SVGImage getFileIcon() {
        String[] split = getData().getName().split("\\.");
        String ext = split[split.length - 1];
        return iconRegistry.getIcon(getProjectConfig().getType() + "/" + ext + ".file.small.icon").getSVGImage();
    }
}
