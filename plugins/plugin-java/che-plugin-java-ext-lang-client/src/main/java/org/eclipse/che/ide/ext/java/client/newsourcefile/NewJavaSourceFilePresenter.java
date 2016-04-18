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
package org.eclipse.che.ide.ext.java.client.newsourcefile;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.project.node.HasStorablePath.StorablePath;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.ext.java.client.project.node.PackageNode;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

import static org.eclipse.che.ide.ext.java.client.JavaUtils.checkCompilationUnitName;
import static org.eclipse.che.ide.ext.java.client.JavaUtils.checkPackageName;
import static org.eclipse.che.ide.ext.java.client.JavaUtils.isValidCompilationUnitName;
import static org.eclipse.che.ide.ext.java.client.JavaUtils.isValidPackageName;
import static org.eclipse.che.ide.ext.java.client.newsourcefile.JavaSourceFileType.ANNOTATION;
import static org.eclipse.che.ide.ext.java.client.newsourcefile.JavaSourceFileType.CLASS;
import static org.eclipse.che.ide.ext.java.client.newsourcefile.JavaSourceFileType.ENUM;
import static org.eclipse.che.ide.ext.java.client.newsourcefile.JavaSourceFileType.INTERFACE;

/**
 * Presenter for creating Java source file.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class NewJavaSourceFilePresenter implements NewJavaSourceFileView.ActionDelegate {
    private static final String DEFAULT_CONTENT = " {\n}\n";

    private final ProjectExplorerPresenter projectExplorer;
    private final NewJavaSourceFileView    view;
    private final ProjectServiceClient     projectServiceClient;
    private final DtoUnmarshallerFactory   dtoUnmarshaller;
    private final List<JavaSourceFileType> sourceFileTypes;
    private final AppContext               appContext;
    private final DialogFactory dialogFactory;

    @Inject
    public NewJavaSourceFilePresenter(NewJavaSourceFileView view,
                                      ProjectExplorerPresenter projectExplorer,
                                      AppContext appContext,
                                      ProjectServiceClient projectServiceClient,
                                      DtoUnmarshallerFactory dtoUnmarshaller,
                                      DialogFactory dialogFactory) {
        this.appContext = appContext;
        this.dialogFactory = dialogFactory;
        sourceFileTypes = Arrays.asList(CLASS, INTERFACE, ENUM, ANNOTATION);
        this.view = view;
        this.projectExplorer = projectExplorer;
        this.projectServiceClient = projectServiceClient;
        this.dtoUnmarshaller = dtoUnmarshaller;
        this.view.setDelegate(this);
    }

    public void showDialog() {
        view.setTypes(sourceFileTypes);
        view.showDialog();
    }

    @Override
    public void onCancelClicked() {
        view.close();
    }

    @Override
    public void onNameChanged() {
        try {
            final String fileNameWithExtension = getFileNameWithExtension(view.getName());
            if (!fileNameWithExtension.trim().isEmpty()) {
                checkCompilationUnitName(fileNameWithExtension);
            }
            final String packageName = getPackageFragment(view.getName());
            if (!packageName.trim().isEmpty()) {
                checkPackageName(packageName);
            }
            view.hideErrorHint();
        } catch (IllegalStateException e) {
            view.showErrorHint(e.getMessage());
        }
    }

    @Override
    public void onOkClicked() {
        final String fileNameWithExtension = getFileNameWithExtension(view.getName());
        final String fileNameWithoutExtension = fileNameWithExtension.substring(0, fileNameWithExtension.lastIndexOf(".java"));
        final String packageFragment = getPackageFragment(view.getName());

        if (!packageFragment.isEmpty() && !isValidPackageName(packageFragment)) {
            return;
        }
        if (isValidCompilationUnitName(fileNameWithExtension)) {
            view.close();

            FolderReferenceNode parent = (FolderReferenceNode)projectExplorer.getSelection().getHeadElement();
            switch (view.getSelectedType()) {
                case CLASS:
                    createClass(fileNameWithoutExtension, parent, packageFragment);
                    break;
                case INTERFACE:
                    createInterface(fileNameWithoutExtension, parent, packageFragment);
                    break;
                case ENUM:
                    createEnum(fileNameWithoutExtension, parent, packageFragment);
                    break;
                case ANNOTATION:
                    createAnnotation(fileNameWithoutExtension, parent, packageFragment);
                    break;
            }
        }
    }

    private String getFileNameWithExtension(String name) {
        if (name.endsWith(".java")) {
            name = name.substring(0, name.lastIndexOf(".java"));
        }
        final int lastDotPos = name.lastIndexOf('.');
        name = name.substring(lastDotPos + 1);
        return name + ".java";
    }

    private String getPackageFragment(String name) {
        if (name.endsWith(".java")) {
            name = name.substring(0, name.lastIndexOf(".java"));
        }
        final int lastDotPos = name.lastIndexOf('.');
        if (lastDotPos >= 0) {
            return name.substring(0, lastDotPos);
        }
        return "";
    }

    private void createClass(String name, FolderReferenceNode parent, String packageFragment) {
        String content = getPackageQualifier(parent, packageFragment) +
                         "public class " + name + DEFAULT_CONTENT;

        createSourceFile(name, parent, packageFragment, content);
    }

    private void createInterface(String name, FolderReferenceNode parent, String packageFragment) {
        String content = getPackageQualifier(parent, packageFragment) +
                         "public interface " + name + DEFAULT_CONTENT;

        createSourceFile(name, parent, packageFragment, content);
    }

    private void createEnum(String name, FolderReferenceNode parent, String packageFragment) {
        String content = getPackageQualifier(parent, packageFragment) +
                         "public enum " + name + DEFAULT_CONTENT;

        createSourceFile(name, parent, packageFragment, content);
    }

    private void createAnnotation(String name, FolderReferenceNode parent, String packageFragment) {
        String content = getPackageQualifier(parent, packageFragment) +
                         "public @interface " + name + DEFAULT_CONTENT;

        createSourceFile(name, parent, packageFragment, content);
    }

    private String getPackageQualifier(FolderReferenceNode parent, String packageFragment) {
        String packageFQN = "";
        if (parent instanceof PackageNode) {
            packageFQN = ((PackageNode)parent).getPackage();
        }
        if (!packageFragment.isEmpty()) {
            packageFQN = packageFQN.isEmpty() ? packageFragment : packageFQN + '.' + packageFragment;
        }
        if (!packageFQN.isEmpty()) {
            return "package " + packageFQN + ";\n\n";
        }
        return "\n";
    }

    private void createSourceFile(final String nameWithoutExtension, final FolderReferenceNode parent, String packageFragment,
                                  final String content) {
        final String path = parent.getStorablePath() + (packageFragment.isEmpty() ? "" : '/' + packageFragment.replace('.', '/'));

        getOrCreateFolder(path).thenPromise(createFile(nameWithoutExtension, content))
                               .thenPromise(navigateToNode())
                               .then(selectNode())
                               .then(openNode())
                               .catchError(onFailedFileCreation());
    }

    private Operation<PromiseError> onFailedFileCreation() {
        return new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                dialogFactory.createMessageDialog("Cannot create java file", arg.getMessage(), null).show();
            }
        };
    }

    private Function<ItemReference, Promise<Node>> navigateToNode() {
        return new Function<ItemReference, Promise<Node>>() {
            @Override
            public Promise<Node> apply(ItemReference createdItem) throws FunctionException {
                final HasStorablePath path = new StorablePath(createdItem.getPath());

                return projectExplorer.getNodeByPath(path, true);
            }
        };
    }

    private Function<ItemReference, Promise<ItemReference>> createFile(final String nameWithoutExtension, final String content) {
        return new Function<ItemReference, Promise<ItemReference>>() {
            @Override
            public Promise<ItemReference> apply(ItemReference folder) throws FunctionException {
                return AsyncPromiseHelper.createFromAsyncRequest(createFileRC(folder, nameWithoutExtension, content));
            }
        };
    }

    private AsyncPromiseHelper.RequestCall<ItemReference> createFileRC(final ItemReference folder, final String nameWithoutExtension,
                                                                       final String content) {
        return new AsyncPromiseHelper.RequestCall<ItemReference>() {
            @Override
            public void makeCall(AsyncCallback<ItemReference> callback) {
                projectServiceClient.createFile(appContext.getDevMachine(),
                                                folder.getPath(),
                                                nameWithoutExtension + ".java",
                                                content,
                                                _callback(callback, dtoUnmarshaller.newUnmarshaller(ItemReference.class)));
            }
        };
    }

    private Promise<ItemReference> getOrCreateFolder(String path) {
        return AsyncPromiseHelper.createFromAsyncRequest(getFolderRC(path))
                                 .catchErrorPromise(catchAndCreateFolder(path));
    }

    private AsyncPromiseHelper.RequestCall<ItemReference> getFolderRC(final String path) {
        return new AsyncPromiseHelper.RequestCall<ItemReference>() {
            @Override
            public void makeCall(AsyncCallback<ItemReference> callback) {
                projectServiceClient.getItem(appContext.getDevMachine(), path, _callback(callback, dtoUnmarshaller.newUnmarshaller(ItemReference.class)));
            }
        };
    }

    @NotNull
    protected <T> AsyncRequestCallback<T> _callback(@NotNull final AsyncCallback<T> callback, @NotNull Unmarshallable<T> u) {
        return new AsyncRequestCallback<T>(u) {
            @Override
            protected void onSuccess(T result) {
                callback.onSuccess(result);
            }

            @Override
            protected void onFailure(Throwable e) {
                callback.onFailure(e);
            }
        };
    }

    private Function<PromiseError, Promise<ItemReference>> catchAndCreateFolder(final String path) {
        return new Function<PromiseError, Promise<ItemReference>>() {
            @Override
            public Promise<ItemReference> apply(PromiseError arg) throws FunctionException {
                return createFolder(path);
            }
        };
    }

    private Promise<ItemReference> createFolder(String path) {
        return AsyncPromiseHelper.createFromAsyncRequest(createFolderRC(path));
    }

    private AsyncPromiseHelper.RequestCall<ItemReference> createFolderRC(final String path) {
        return new AsyncPromiseHelper.RequestCall<ItemReference>() {
            @Override
            public void makeCall(AsyncCallback<ItemReference> callback) {
                projectServiceClient.createFolder(appContext.getDevMachine(), path, _callback(callback, dtoUnmarshaller.newUnmarshaller(ItemReference.class)));
            }
        };
    }

    protected Function<Node, Node> selectNode() {
        return new Function<Node, Node>() {
            @Override
            public Node apply(Node node) throws FunctionException {
                projectExplorer.select(node, false);

                return node;
            }
        };
    }

    protected Function<Node, Node> openNode() {
        return new Function<Node, Node>() {
            @Override
            public Node apply(Node node) throws FunctionException {
                if (node instanceof FileReferenceNode) {
                    ((FileReferenceNode)node).actionPerformed();
                }

                return node;
            }
        };
    }
}
