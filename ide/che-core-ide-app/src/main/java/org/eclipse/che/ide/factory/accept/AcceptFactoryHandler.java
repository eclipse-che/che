/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.factory.accept;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import javax.inject.Inject;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.factory.FactoryAcceptedEvent;
import org.eclipse.che.ide.api.factory.model.ActionImpl;
import org.eclipse.che.ide.api.factory.model.FactoryImpl;
import org.eclipse.che.ide.api.factory.model.IdeImpl;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.workspace.WorkspaceReadyEvent;
import org.eclipse.che.ide.factory.utils.FactoryProjectImporter;

/**
 * @author Sergii Leschenko
 * @author Anton Korneta
 */
@Singleton
public class AcceptFactoryHandler {
  private final CoreLocalizationConstant localizationConstant;
  private final FactoryProjectImporter factoryProjectImporter;
  private final EventBus eventBus;
  private final AppContext appContext;
  private final ActionManager actionManager;
  private final NotificationManager notificationManager;

  private StatusNotification notification;
  private boolean isImportingStarted;

  @Inject
  public AcceptFactoryHandler(
      CoreLocalizationConstant localizationConstant,
      FactoryProjectImporter factoryProjectImporter,
      EventBus eventBus,
      AppContext appContext,
      ActionManager actionManager,
      NotificationManager notificationManager) {
    this.factoryProjectImporter = factoryProjectImporter;
    this.localizationConstant = localizationConstant;
    this.eventBus = eventBus;
    this.appContext = appContext;
    this.actionManager = actionManager;
    this.notificationManager = notificationManager;
  }

  /** Accepts factory if it is present in context of application */
  public void process() {
    final FactoryImpl factory;
    if ((factory = appContext.getFactory()) == null) {
      return;
    }

    eventBus.addHandler(
        WorkspaceReadyEvent.getType(),
        e -> {
          if (isImportingStarted) {
            return;
          }

          isImportingStarted = true;

          notification =
              notificationManager.notify(
                  localizationConstant.cloningSource(),
                  StatusNotification.Status.PROGRESS,
                  NOT_EMERGE_MODE);
          performOnAppLoadedActions(factory);
          startImporting(factory);
        });
  }

  private void startImporting(final FactoryImpl factory) {
    factoryProjectImporter.startImporting(
        factory,
        new AsyncCallback<Void>() {
          @Override
          public void onSuccess(Void result) {
            notification.setStatus(StatusNotification.Status.SUCCESS);
            notification.setContent(localizationConstant.cloningSource());
            performOnProjectsLoadedActions(factory);
          }

          @Override
          public void onFailure(Throwable throwable) {
            notification.setStatus(StatusNotification.Status.FAIL);
            notification.setContent(throwable.getMessage());
          }
        });
  }

  private void performOnAppLoadedActions(final FactoryImpl factory) {
    final IdeImpl ide = factory.getIde();
    if (ide == null || ide.getOnAppLoaded() == null) {
      return;
    }
    for (ActionImpl action : ide.getOnAppLoaded().getActions()) {
      actionManager.performAction(action.getId(), action.getProperties());
    }
  }

  private void performOnProjectsLoadedActions(final FactoryImpl factory) {
    final IdeImpl ide = factory.getIde();
    if (ide == null || ide.getOnProjectsLoaded() == null) {
      eventBus.fireEvent(new FactoryAcceptedEvent(factory));
      return;
    }
    for (ActionImpl action : ide.getOnProjectsLoaded().getActions()) {
      actionManager.performAction(action.getId(), action.getProperties());
    }
    eventBus.fireEvent(new FactoryAcceptedEvent(factory));
  }
}
