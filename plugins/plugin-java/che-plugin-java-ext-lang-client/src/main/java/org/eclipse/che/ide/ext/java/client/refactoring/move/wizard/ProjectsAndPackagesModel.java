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
package org.eclipse.che.ide.ext.java.client.refactoring.move.wizard;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;
import java.util.List;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactorInfo;
import org.eclipse.che.ide.ext.java.client.refactoring.move.RefactoredItemType;
import org.eclipse.che.jdt.ls.extension.api.dto.JavaProjectStructure;
import org.eclipse.che.jdt.ls.extension.api.dto.PackageFragment;
import org.eclipse.che.jdt.ls.extension.api.dto.PackageFragmentRoot;

/**
 * A model of a tree which contains all possible destinations from the current workspace.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
public class ProjectsAndPackagesModel implements TreeViewModel {
  private List<JavaProjectStructure> projects;
  private final RefactorInfo refactorInfo;
  private SingleSelectionModel<Object> selectionModel;
  private JavaResources resources;

  public ProjectsAndPackagesModel(
      List<JavaProjectStructure> projects,
      RefactorInfo refactorInfo,
      SingleSelectionModel<Object> selectionModel,
      JavaResources resources) {
    this.projects = projects;
    this.refactorInfo = refactorInfo;
    this.selectionModel = selectionModel;
    this.resources = resources;
  }

  /** {@inheritDoc} */
  @Override
  public <T> NodeInfo<?> getNodeInfo(T value) {
    if (value == null) {
      return new DefaultNodeInfo<>(
          new ListDataProvider<>(projects),
          new AbstractCell<JavaProjectStructure>() {
            @Override
            public void render(Context context, JavaProjectStructure value, SafeHtmlBuilder sb) {
              sb.appendHtmlConstant(resources.javaCategoryIcon().getSvg().getElement().getString())
                  .appendEscaped(" ");
              sb.appendEscaped(value.getName());
            }
          },
          selectionModel,
          null);
    }

    if (value instanceof JavaProjectStructure) {
      final JavaProjectStructure project = (JavaProjectStructure) value;
      return new DefaultNodeInfo<>(
          new ListDataProvider<>(project.getPackageRoots()),
          new AbstractCell<PackageFragmentRoot>() {
            @Override
            public void render(Context context, PackageFragmentRoot value, SafeHtmlBuilder sb) {
              sb.appendHtmlConstant(resources.sourceFolder().getSvg().getElement().getString())
                  .appendEscaped(" ");

              sb.appendEscaped(value.getUri().substring(project.getUri().length()));
            }
          },
          selectionModel,
          null);
    }

    if (value instanceof PackageFragmentRoot) {
      if (RefactoredItemType.PACKAGE.equals(refactorInfo.getRefactoredItemType())) {
        return null;
      }
      return new DefaultNodeInfo<>(
          new ListDataProvider<>(((PackageFragmentRoot) value).getPackages()),
          new AbstractCell<PackageFragment>() {
            @Override
            public void render(Context context, PackageFragment value, SafeHtmlBuilder sb) {
              sb.appendHtmlConstant(resources.packageItem().getSvg().getElement().getString())
                  .appendEscaped(" ");

              if (value.getName().isEmpty()) {
                sb.appendEscaped("(default package)");
              } else {
                sb.appendEscaped(value.getName());
              }
            }
          },
          selectionModel,
          null);
    }
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isLeaf(Object value) {
    return value instanceof PackageFragment;
  }
}
