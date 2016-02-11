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
//
//import org.eclipse.che.api.account.server.dao.Account;
//import org.eclipse.che.api.account.server.dao.AccountDao;
//import org.eclipse.che.api.account.server.dao.AccountWorkspacesDao;
//import org.eclipse.che.api.core.ServerException;
//import org.eclipse.che.api.core.notification.EventService;
//import org.eclipse.che.api.core.notification.EventSubscriber;
//import org.eclipse.che.api.user.server.dao.MembershipDao;
//import org.eclipse.che.api.workspace.server.event.AfterCreateWorkspaceEvent;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.PreDestroy;
import javax.inject.Singleton;

/**
 * @author gazarenkov
 * @TODO move to hosted
 */
@Singleton
public class BeforeCreateWorkspaceSubscriber {
//    private final EventService eventService;
//    private final EventSubscriber<AfterCreateWorkspaceEvent> subscriber;
//
//    public BeforeCreateWorkspaceSubscriber(final EventService eventService, final AccountWorkspacesDao workspacesDao,
//                                           final AccountDao accountDao) {
//        this.eventService = eventService;
//        subscriber = new EventSubscriber<AfterCreateWorkspaceEvent> () {
//            @Override
//            public void onEvent(AfterCreateWorkspaceEvent event) {
//                String accountId = event.getOptions().get("accountId");
//                if(accountId != null) {
//
//
//
//                    // TODO throw Exception
//
//                    try {
//
//                        Account account = accountDao.getById(accountId);
//
//                        if (account.getAttributes().containsKey(org.eclipse.che.api.account.server.Constants.RESOURCES_LOCKED_PROPERTY)) {
//                            event.getWorkspace().getAttributes().put(org.eclipse.che.api.account.server.Constants.RESOURCES_LOCKED_PROPERTY, "true");
//                        }
//
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
