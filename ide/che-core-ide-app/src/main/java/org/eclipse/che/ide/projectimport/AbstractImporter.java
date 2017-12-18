/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.projectimport;

import static com.google.common.base.Preconditions.checkNotNull;

import org.eclipse.che.api.core.model.workspace.config.SourceStorage;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.projectimport.wizard.ImportProjectNotificationSubscriberFactory;
import org.eclipse.che.ide.resource.Path;

/**
 * The general class for all importers. The class contains business logic which allows add and
 * remove projects in list projects which are in importing state. The project is added in special
 * list before import starts and removed from list when import finishes or some exception occurs.
 *
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 */
public abstract class AbstractImporter {

  protected final AppContext appContext;
  protected final ImportProjectNotificationSubscriberFactory subscriberFactory;

  protected AbstractImporter(
      AppContext appContext, ImportProjectNotificationSubscriberFactory subscriberFactory) {
    this.appContext = checkNotNull(appContext);
    this.subscriberFactory = checkNotNull(subscriberFactory);
  }

  /**
   * Starts project importing. This method should be called when we want mark project as importing.
   *
   * @param sourceStorage information about project location and repository type
   * @return returns instance of Promise
   */
  protected Promise<Project> startImport(final Path path, SourceStorage sourceStorage) {
    appContext.addProjectToImporting(path.toString());

    return importProject(path, sourceStorage)
        .then(
            new Function<Project, Project>() {
              @Override
              public Project apply(Project project) throws FunctionException {
                appContext.removeProjectFromImporting(project.getLocation().toString());

                return project;
              }
            })
        .catchErrorPromise(
            new Function<PromiseError, Promise<Project>>() {
              @Override
              public Promise<Project> apply(PromiseError error) throws FunctionException {
                appContext.removeProjectFromImporting(path.toString());

                throw new IllegalStateException(error.getCause());
              }
            });
  }

  /**
   * The method imports projects from location.
   *
   * @param sourceStorage information about project location and repository type
   * @return returns instance of Promise
   */
  protected abstract Promise<Project> importProject(Path path, SourceStorage sourceStorage);
}
