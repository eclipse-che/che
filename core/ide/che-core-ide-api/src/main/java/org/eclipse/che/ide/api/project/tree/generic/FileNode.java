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
package org.eclipse.che.ide.api.project.tree.generic;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.api.project.tree.TreeStructure;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.rest.Unmarshallable;

import java.util.List;
import java.util.Objects;

/**
 * A node that represents a file (an {@link ItemReference} with type - file).
 *
 * @author Artem Zatsarynnyi
 */
@Deprecated
public class FileNode extends ItemNode implements VirtualFile {

    private final AppContext appContext;

    @Inject
    public FileNode(@Assisted TreeNode<?> parent,
                    @Assisted ItemReference data,
                    @Assisted TreeStructure treeStructure,
                    EventBus eventBus,
                    AppContext appContext,
                    ProjectServiceClient projectService,
                    DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        super(parent,
              data,
              treeStructure,
              eventBus,
              appContext,
              projectService,
              dtoUnmarshallerFactory);
        this.appContext = appContext;
    }

    /** {@inheritDoc} */
    @Override
    public void rename(final String newName, final RenameCallback renameCallback) {
//        final FileNode fileNode = this;
//        String newMediaType = fileNode.getMediaType();
//
//        final String parentPath = ((StorableNode)getParent()).getPath();
//        final String oldNodePath = getPath();
//        final String newPath = parentPath + "/" + newName;
//
//        projectServiceClient.rename(oldNodePath, newName, newMediaType, new AsyncRequestCallback<Void>() {
//            @Override
//            protected void onSuccess(Void result) {
//
//                updateData(new AsyncCallback<Void>() {
//                    @Override
//                    public void onSuccess(Void result) {
//                        eventBus.fireEvent(NodeChangedEvent.createNodeRenamedEvent(fileNode));
//                    }
//
//                    @Override
//                    public void onFailure(Throwable exception) {
//                        renameCallback.onFailure(exception);
//                    }
//                }, newPath);
//
//            }
//
//            @Override
//            protected void onFailure(Throwable exception) {
//                renameCallback.onFailure(exception);
//            }
//        });
    }

    /** {@inheritDoc} */
    public void updateData(final AsyncCallback<Void> asyncCallback, String newPath) {
        final String oldNodePath = FileNode.this.getPath();

        Unmarshallable<ItemReference> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(ItemReference.class);
        projectServiceClient.getItem(appContext.getDevMachine(), newPath, new AsyncRequestCallback<ItemReference>(unmarshaller) {
            @Override
            protected void onSuccess(ItemReference result) {
                setData(result);

                asyncCallback.onSuccess(null);
            }

            @Override
            protected void onFailure(Throwable exception) {
                asyncCallback.onFailure(exception);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public boolean isLeaf() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void processNodeAction() {
        eventBus.fireEvent(new FileEvent(this, FileEvent.FileOperation.OPEN));
    }

    /** {@inheritDoc} */
    @Override
    public void delete(final DeleteCallback callback) {
        super.delete(new DeleteCallback() {
            @Override
            public void onDeleted() {
                eventBus.fireEvent(new FileEvent(FileNode.this, FileEvent.FileOperation.CLOSE));
                callback.onDeleted();
            }

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }
        });
    }

    @Override
    public boolean isReadOnly() {
        //todo add permissions check here
        return false;
    }

    @Override
    public String getContentUrl() {
        List<Link> links = getData().getLinks();
        Link li = null;
        for (Link link : links) {
            if (link.getRel().equals("get content")) {
                li = link;
                break;
            }
        }
        return li == null ? null : li.getHref();
    }

    /**
     * Get content of the file which this node represents.
     */
    @Override
    public Promise<String> getContent() {
        return AsyncPromiseHelper.createFromAsyncRequest(new AsyncPromiseHelper.RequestCall<String>() {
            @Override
            public void makeCall(final AsyncCallback<String> callback) {
                projectServiceClient.getFileContent(appContext.getDevMachine(), getPath(), new AsyncRequestCallback<String>(new StringUnmarshaller()) {
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
        });
    }

    /**
     * Update content of the file which this node represents.
     *
     * @param content
     *         new content of the file
     */
    @Override
    public Promise<Void> updateContent(final String content) {
        return AsyncPromiseHelper.createFromAsyncRequest(new AsyncPromiseHelper.RequestCall<Void>() {
            @Override
            public void makeCall(final AsyncCallback<Void> callback) {
                projectServiceClient.updateFile(appContext.getDevMachine(), getPath(), content, new AsyncRequestCallback<Void>() {
                    @Override
                    protected void onSuccess(Void result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                });
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof FileNode)) {
            return false;
        }

        FileNode other = (FileNode)o;
        return Objects.equals(getData(), other.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getData());
    }

    @Override
    public boolean canContainsFolder() {
        return false;
    }
}
