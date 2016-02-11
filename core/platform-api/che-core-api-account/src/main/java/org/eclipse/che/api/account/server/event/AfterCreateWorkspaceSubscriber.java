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
package org.eclipse.che.api.account.server.event;

import org.eclipse.che.api.account.server.dao.AccountWorkspacesDao;
import org.eclipse.che.api.core.notification.EventService;
//import org.eclipse.che.api.core.notification.EventSubscriber;
//import org.eclipse.che.api.workspace.server.event.AfterCreateWorkspaceEvent;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

/**
 * @author gazarenkov
 * TODO move to hosted
 */
@Singleton
public class AfterCreateWorkspaceSubscriber {
//    private final EventService eventService;
//    private final EventSubscriber<AfterCreateWorkspaceEvent> subscriber;

//    public AfterCreateWorkspaceSubscriber(final EventService eventService, final AccountWorkspacesDao workspacesDao) {
//        this.eventService = eventService;
//        subscriber = new EventSubscriber<AfterCreateWorkspaceEvent> () {
//            @Override
//            public void onEvent(AfterCreateWorkspaceEvent event) {
//                String accountId = event.getOptions().get("accountId");
//                if(accountId != null) {
//                    try {
//                        workspacesDao.create(event.getWorkspace().getId(), accountId);
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }
//        };
//    }
//
//    @PostConstruct
//    public void subscribe() {
//        eventService.subscribe(subscriber);
//    }
//
//    @PreDestroy
//    public void unsubscribe() {
//        eventService.unsubscribe(subscriber);
//    }
}
