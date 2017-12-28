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
package org.eclipse.che.ide.ext.java.client.project.classpath;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ext.java.client.util.JavaUtil.isJavaProject;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.command.ClasspathContainer;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.ClasspathPagePresenter;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;

/**
 * Presenter for managing classpath.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ProjectClasspathPresenter
    implements ProjectClasspathView.ActionDelegate, ClasspathPagePresenter.DirtyStateListener {
  private final ProjectClasspathView view;
  private final Set<ClasspathPagePresenter> classpathPages;
  private final AppContext appContext;
  private final ClasspathContainer classpathContainer;
  private final JavaLocalizationConstant locale;
  private final DialogFactory dialogFactory;
  private final NotificationManager notificationManager;
  private final ClasspathResolver classpathResolver;

  private Map<String, Set<ClasspathPagePresenter>> propertiesMap;

  @Inject
  protected ProjectClasspathPresenter(
      ProjectClasspathView view,
      Set<ClasspathPagePresenter> classpathPages,
      AppContext appContext,
      ClasspathContainer classpathContainer,
      JavaLocalizationConstant locale,
      DialogFactory dialogFactory,
      NotificationManager notificationManager,
      ClasspathResolver classpathResolver) {
    this.view = view;
    this.classpathPages = classpathPages;
    this.appContext = appContext;
    this.classpathContainer = classpathContainer;
    this.locale = locale;
    this.dialogFactory = dialogFactory;
    this.notificationManager = notificationManager;
    this.classpathResolver = classpathResolver;
    this.view.setDelegate(this);
    for (ClasspathPagePresenter property : classpathPages) {
      property.setUpdateDelegate(this);
    }
  }

  @Override
  public void onDoneClicked() {
    for (ClasspathPagePresenter property : classpathPages) {
      if (property.isDirty()) {
        property.storeChanges();
      }
      property.clearData();
    }

    classpathResolver
        .updateClasspath()
        .then(
            new Operation<Void>() {
              @Override
              public void apply(Void arg) throws OperationException {
                view.close();
              }
            });
  }

  @Override
  public void onCloseClicked() {
    boolean haveUnsavedData = false;
    for (ClasspathPagePresenter property : classpathPages) {
      haveUnsavedData |= property.isDirty();
    }
    if (haveUnsavedData) {
      dialogFactory
          .createConfirmDialog(
              locale.unsavedChangesTitle(),
              locale.messagesPromptSaveChanges(),
              locale.buttonContinue(),
              locale.buttonSave(),
              getConfirmCallback(),
              getCancelCallback())
          .show();
    } else {
      clearData();
    }
  }

  @Override
  public void onEnterClicked() {
    if (view.isDoneButtonInFocus()) {
      onDoneClicked();
    }
  }

  @Override
  public void clearData() {
    for (ClasspathPagePresenter property : classpathPages) {
      property.clearData();
    }
  }

  @Override
  public void onConfigurationSelected(ClasspathPagePresenter pagePresenter) {
    pagePresenter.go(view.getConfigurationsContainer());
  }

  /** Show dialog. */
  public void show() {

    final Resource[] resources = appContext.getResources();

    Preconditions.checkState(resources != null && resources.length == 1);

    final Project project = resources[0].getProject();

    Preconditions.checkState(isJavaProject(project));

    classpathContainer
        .getClasspathEntries(project.getLocation().toString())
        .then(
            arg -> {
              classpathResolver.resolveClasspathEntries(arg);
              if (propertiesMap == null) {
                propertiesMap = new HashMap<>();
                for (ClasspathPagePresenter page : classpathPages) {
                  Set<ClasspathPagePresenter> pages =
                      propertiesMap.computeIfAbsent(page.getCategory(), k -> new HashSet<>());
                  pages.add(page);
                }

                view.setPages(propertiesMap);
              }
              view.showDialog();
              view.selectPage(
                  propertiesMap.entrySet().iterator().next().getValue().iterator().next());
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify(
                    "Problems with getting classpath", arg.getMessage(), FAIL, EMERGE_MODE);
              }
            });
  }

  @Override
  public void onDirtyChanged() {}

  private ConfirmCallback getConfirmCallback() {
    return new ConfirmCallback() {
      @Override
      public void accepted() {
        for (ClasspathPagePresenter property : classpathPages) {
          if (property.isDirty()) {
            property.revertChanges();
          }
        }
      }
    };
  }

  private CancelCallback getCancelCallback() {
    return new CancelCallback() {
      @Override
      public void cancelled() {
        onDoneClicked();
      }
    };
  }
}
