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
package org.eclipse.che.ide.ext.java.client.newsourcefile;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Folder;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;

import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
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

    private final NewJavaSourceFileView    view;
    private final List<JavaSourceFileType> sourceFileTypes;
    private final EventBus                 eventBus;
    private       Container                parent;

    @Inject
    public NewJavaSourceFilePresenter(NewJavaSourceFileView view, EventBus eventBus) {
        this.eventBus = eventBus;
        sourceFileTypes = Arrays.asList(CLASS, INTERFACE, ENUM, ANNOTATION);
        this.view = view;
        this.view.setDelegate(this);
    }

    public void showDialog(Container parent) {
        this.parent = parent;
        view.setTypes(sourceFileTypes); //todo why we need this there?
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

            switch (view.getSelectedType()) {
                case CLASS:
                    createClass(fileNameWithoutExtension, packageFragment);
                    break;
                case INTERFACE:
                    createInterface(fileNameWithoutExtension, packageFragment);
                    break;
                case ENUM:
                    createEnum(fileNameWithoutExtension, packageFragment);
                    break;
                case ANNOTATION:
                    createAnnotation(fileNameWithoutExtension, packageFragment);
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

    private void createClass(String name, String packageFragment) {
        String content = getPackageQualifier(packageFragment) +
                         "public class " + name + DEFAULT_CONTENT;

        createSourceFile(name, packageFragment, content);
    }

    private void createInterface(String name, String packageFragment) {
        String content = getPackageQualifier(packageFragment) +
                         "public interface " + name + DEFAULT_CONTENT;

        createSourceFile(name, packageFragment, content);
    }

    private void createEnum(String name, String packageFragment) {
        String content = getPackageQualifier(packageFragment) +
                         "public enum " + name + DEFAULT_CONTENT;

        createSourceFile(name, packageFragment, content);
    }

    private void createAnnotation(String name, String packageFragment) {
        String content = getPackageQualifier(packageFragment) +
                         "public @interface " + name + DEFAULT_CONTENT;

        createSourceFile(name, packageFragment, content);
    }

    private String getPackageQualifier(String packageFragment) {
        final Optional<Resource> srcFolder = parent.getParentWithMarker(SourceFolderMarker.ID);

        if (!srcFolder.isPresent() && isNullOrEmpty(packageFragment)) {
            return "\n";
        }

        final Path path = parent.getLocation().removeFirstSegments(srcFolder.get().getLocation().segmentCount());

        String packageFQN = path.toString().replace('/', '.');
        if (!packageFragment.isEmpty()) {
            packageFQN = packageFQN.isEmpty() ? packageFragment : packageFQN + '.' + packageFragment;
        }
        if (!packageFQN.isEmpty()) {
            return "package " + packageFQN + ";\n\n";
        } else {
            return "\n";
        }
    }

    private void createSourceFile(final String nameWithoutExtension, String packageFragment, final String content) {
        if (!isNullOrEmpty(packageFragment)) {
            parent.newFolder(packageFragment.replace('.', '/')).then(new Operation<Folder>() {
                @Override
                public void apply(Folder pkg) throws OperationException {
                    pkg.newFile(nameWithoutExtension + ".java", content).then(new Operation<File>() {
                        @Override
                        public void apply(File file) throws OperationException {
                            eventBus.fireEvent(FileEvent.createOpenFileEvent(file));
                            eventBus.fireEvent(new RevealResourceEvent(file));
                        }
                    });
                }
            });
        } else {
            parent.newFile(nameWithoutExtension + ".java", content).then(new Operation<File>() {
                @Override
                public void apply(File file) throws OperationException {
                    eventBus.fireEvent(FileEvent.createOpenFileEvent(file));
                    eventBus.fireEvent(new RevealResourceEvent(file));
                }
            });
        }
    }
}
