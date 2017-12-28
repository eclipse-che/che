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
package org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.sources;

import static org.eclipse.che.ide.ext.java.shared.ClasspathEntryKind.SOURCE;
import static org.eclipse.che.ide.ext.java.shared.Constants.JAVAC;

import com.google.common.base.Preconditions;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import java.util.TreeMap;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.command.ClasspathContainer;
import org.eclipse.che.ide.ext.java.client.project.classpath.ClasspathResolver;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.AbstractClasspathPagePresenter;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode.SelectNodePresenter;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode.interceptors.SourceFolderNodeInterceptor;
import org.eclipse.che.jdt.ls.extension.api.dto.ClasspathEntry;

/**
 * The page for the information about source folders which are including into classpath.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class SourceEntryPresenter extends AbstractClasspathPagePresenter
    implements SourceEntryView.ActionDelegate {
  private final DtoFactory dtoFactory;
  private final ClasspathContainer classpathContainer;
  private final ClasspathResolver classpathResolver;
  private final AppContext appContext;
  private final SelectNodePresenter selectNodePresenter;
  private final SourceEntryView view;

  private boolean dirty;
  private boolean isPlainJava;
  private Map<String, ClasspathEntry> categories;

  @Inject
  public SourceEntryPresenter(
      SourceEntryView view,
      DtoFactory dtoFactory,
      ClasspathContainer classpathContainer,
      ClasspathResolver classpathResolver,
      JavaLocalizationConstant localization,
      AppContext appContext,
      SelectNodePresenter selectNodePresenter) {
    super(localization.sourcePropertyName(), localization.javaBuildPathCategory(), null);
    this.view = view;
    this.dtoFactory = dtoFactory;
    this.classpathContainer = classpathContainer;
    this.classpathResolver = classpathResolver;
    this.appContext = appContext;
    this.selectNodePresenter = selectNodePresenter;

    categories = new TreeMap<>();
    dirty = false;

    view.setDelegate(this);
  }

  @Override
  public boolean isDirty() {
    return dirty;
  }

  @Override
  public void go(AcceptsOneWidget container) {

    final Resource resource = appContext.getResource();

    Preconditions.checkState(resource != null);

    final Project project = resource.getProject();

    isPlainJava = JAVAC.equals(project.getType());

    setReadOnlyMod();

    container.setWidget(view);

    if (!categories.isEmpty()) {
      view.renderNodes();
      return;
    }

    classpathContainer
        .getClasspathEntries(project.getLocation().toString())
        .then(
            entries -> {
              categories.clear();
              for (ClasspathEntry entry : entries) {
                if (SOURCE == entry.getEntryKind()) {
                  categories.put(entry.getPath(), entry);
                }
              }
              view.setData(categories);
              view.renderNodes();
            });
  }

  @Override
  public void onAddSourceClicked() {
    selectNodePresenter.show(this, new SourceFolderNodeInterceptor(), true);
  }

  @Override
  public void onRemoveClicked(String path) {
    removeNode(path);
  }

  @Override
  public void storeChanges() {
    classpathResolver.getSources().clear();

    for (Map.Entry<String, ClasspathEntry> entry : categories.entrySet()) {
      if (SOURCE == entry.getValue().getEntryKind()) {
        classpathResolver.getSources().add(entry.getKey());
      }
    }

    dirty = false;
    delegate.onDirtyChanged();
  }

  @Override
  public void revertChanges() {
    clearData();

    dirty = false;
    delegate.onDirtyChanged();
  }

  @Override
  public void clearData() {
    categories.clear();
  }

  @Override
  public void addNode(String path, int kind) {
    if (categories.containsKey(path)) {
      return;
    }

    dirty = true;
    delegate.onDirtyChanged();

    categories.put(path, dtoFactory.createDto(ClasspathEntry.class).withEntryKind(kind));
    view.setData(categories);
    view.renderNodes();
  }

  @Override
  public void removeNode(String path) {
    dirty = true;
    delegate.onDirtyChanged();
    categories.remove(path);
    view.setData(categories);
    view.renderNodes();
  }

  @Override
  public boolean isPlainJava() {
    return isPlainJava;
  }

  private void setReadOnlyMod() {
    view.setAddSourceButtonState(isPlainJava);
  }
}
