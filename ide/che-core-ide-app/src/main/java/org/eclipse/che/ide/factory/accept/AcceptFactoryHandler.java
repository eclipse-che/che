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
package org.eclipse.che.ide.factory.accept;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import javax.inject.Inject;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.factory.shared.dto.IdeActionDto;
import org.eclipse.che.api.factory.shared.dto.IdeDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.factory.FactoryAcceptedEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
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
    final FactoryDto factory;
    if ((factory = appContext.getFactory()) == null) {
      return;
    }
    eventBus.addHandler(
        WsAgentStateEvent.TYPE,
        new WsAgentStateHandler() {
          @Override
          public void onWsAgentStarted(final WsAgentStateEvent event) {
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
          }

          @Override
          public void onWsAgentStopped(WsAgentStateEvent event) {}
        });
  }

  private void startImporting(final FactoryDto factory) {
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

  private void performOnAppLoadedActions(final FactoryDto factory) {
    final IdeDto ide = factory.getIde();
    if (ide == null || ide.getOnAppLoaded() == null) {
      return;
    }
    for (IdeActionDto action : ide.getOnAppLoaded().getActions()) {
      actionManager.performAction(action.getId(), action.getProperties());
    }
  }

  private void performOnProjectsLoadedActions(final FactoryDto factory) {
    final IdeDto ide = factory.getIde();
    if (ide == null || ide.getOnProjectsLoaded() == null) {
      eventBus.fireEvent(new FactoryAcceptedEvent(factory));
      return;
    }
    for (IdeActionDto action : ide.getOnProjectsLoaded().getActions()) {
      actionManager.performAction(action.getId(), action.getProperties());
    }
    eventBus.fireEvent(new FactoryAcceptedEvent(factory));
  }
}
