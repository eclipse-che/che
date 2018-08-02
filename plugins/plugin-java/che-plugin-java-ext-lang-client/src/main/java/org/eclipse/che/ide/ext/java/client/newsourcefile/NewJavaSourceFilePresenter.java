/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.newsourcefile;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.ide.ext.java.client.JavaUtils.checkCompilationUnitName;
import static org.eclipse.che.ide.ext.java.client.JavaUtils.checkPackageName;
import static org.eclipse.che.ide.ext.java.client.JavaUtils.isValidCompilationUnitName;
import static org.eclipse.che.ide.ext.java.client.JavaUtils.isValidPackageName;
import static org.eclipse.che.ide.ext.java.client.newsourcefile.JavaSourceFileType.ANNOTATION;
import static org.eclipse.che.ide.ext.java.client.newsourcefile.JavaSourceFileType.CLASS;
import static org.eclipse.che.ide.ext.java.client.newsourcefile.JavaSourceFileType.ENUM;
import static org.eclipse.che.ide.ext.java.client.newsourcefile.JavaSourceFileType.INTERFACE;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;

/**
 * Presenter for creating Java source file.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class NewJavaSourceFilePresenter implements NewJavaSourceFileView.ActionDelegate {
  private static final String DEFAULT_CONTENT = " {\n}\n";

  private final NewJavaSourceFileView view;
  private final List<JavaSourceFileType> sourceFileTypes;
  private final JavaLocalizationConstant locale;
  private final EventBus eventBus;
  private final EditorAgent editorAgent;
  private Container parent;

  @Inject
  public NewJavaSourceFilePresenter(
      NewJavaSourceFileView view,
      JavaLocalizationConstant locale,
      EventBus eventBus,
      EditorAgent editorAgent) {
    this.locale = locale;
    this.eventBus = eventBus;
    this.editorAgent = editorAgent;
    sourceFileTypes = Arrays.asList(CLASS, INTERFACE, ENUM, ANNOTATION);
    this.view = view;
    this.view.setDelegate(this);
  }

  public void showDialog(Container parent) {
    this.parent = parent;
    view.setTypes(sourceFileTypes); // todo why we need this there?
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
        checkCompilationUnitName(view.getName());
      }
      final String packageName = getPackageFragment(view.getName());
      if (!packageName.trim().isEmpty()) {
        checkPackageName(packageName);
      }
      view.hideErrorHint();
    } catch (IllegalStateException e) {
      view.showErrorHint(locale.actionNewClassNameIsInvalid());
    }
  }

  @Override
  public void onOkClicked() {
    final String fileNameWithExtension = getFileNameWithExtension(view.getName());
    final String fileNameWithoutExtension =
        fileNameWithExtension.substring(0, fileNameWithExtension.lastIndexOf(".java"));
    final String packageFragment = getPackageFragment(view.getName());

    if (!packageFragment.isEmpty() && !isValidPackageName(packageFragment)) {
      return;
    }
    if (isValidCompilationUnitName(fileNameWithoutExtension)) {
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
    String content =
        getPackageQualifier(packageFragment) + "public class " + name + DEFAULT_CONTENT;

    createSourceFile(name, packageFragment, content);
  }

  private void createInterface(String name, String packageFragment) {
    String content =
        getPackageQualifier(packageFragment) + "public interface " + name + DEFAULT_CONTENT;

    createSourceFile(name, packageFragment, content);
  }

  private void createEnum(String name, String packageFragment) {
    String content = getPackageQualifier(packageFragment) + "public enum " + name + DEFAULT_CONTENT;

    createSourceFile(name, packageFragment, content);
  }

  private void createAnnotation(String name, String packageFragment) {
    String content =
        getPackageQualifier(packageFragment) + "public @interface " + name + DEFAULT_CONTENT;

    createSourceFile(name, packageFragment, content);
  }

  private String getPackageQualifier(String packageFragment) {
    final Optional<Resource> srcFolder = parent.getParentWithMarker(SourceFolderMarker.ID);

    if (!srcFolder.isPresent() && isNullOrEmpty(packageFragment)) {
      return "\n";
    }

    final Path path =
        parent.getLocation().removeFirstSegments(srcFolder.get().getLocation().segmentCount());

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

  private void createSourceFile(
      final String nameWithoutExtension, String packageFragment, final String content) {
    if (!isNullOrEmpty(packageFragment)) {
      parent
          .newFolder(packageFragment.replace('.', '/'))
          .then(
              pkg -> {
                pkg.newFile(nameWithoutExtension + ".java", content)
                    .then(
                        file -> {
                          editorAgent.openEditor(file);
                          eventBus.fireEvent(new RevealResourceEvent(file));
                        });
              });
    } else {
      parent
          .newFile(nameWithoutExtension + ".java", content)
          .then(
              file -> {
                editorAgent.openEditor(file);
                eventBus.fireEvent(new RevealResourceEvent(file));
              });
    }
  }
}
