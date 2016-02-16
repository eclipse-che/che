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
package org.eclipse.che.ide.ext.java.server;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;

import org.eclipse.che.api.auth.oauth.OAuthTokenProvider;
import org.eclipse.che.api.core.notification.WSocketEventBusClient;
import org.eclipse.che.api.core.rest.ApiInfoService;
import org.eclipse.che.api.core.rest.CoreRestModule;
import org.eclipse.che.api.local.LocalUserDaoImpl;
import org.eclipse.che.api.project.server.ProjectApiModule;
import org.eclipse.che.api.ssh.server.HttpSshServiceClient;
import org.eclipse.che.api.ssh.server.SshServiceClient;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.vfs.VirtualFileSystemModule;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.everrest.CodenvyAsynchronousJobPool;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.security.oauth.RemoteOAuthTokenProvider;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.guice.ServiceBindingHelper;

import javax.inject.Named;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Evgen Vidolob
 */
@DynaModule
public class MachineModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ApiInfoService.class);

//        bind(LocalFileSystemRegistryPlugin.class);

        //TODO it's temporary solution. Ext war should not have binding for DAO.
        bind(UserDao.class).to(LocalUserDaoImpl.class);
        bind(PreferenceDao.class).to(org.eclipse.che.api.local.RemotePreferenceDao.class);

//        bind(LocalFSMountStrategy.class).to(MachineFSMountStrategy.class);
//        bind(VirtualFileSystemRegistry.class).to(AutoMountVirtualFileSystemRegistry.class);
        bind(OAuthTokenProvider.class).to(RemoteOAuthTokenProvider.class);
        bind(SshServiceClient.class).to(HttpSshServiceClient.class);

//        bind(org.eclipse.che.git.impl.nativegit.ssh.SshKeyProvider.class)
//                .to(org.eclipse.che.git.impl.nativegit.ssh.SshKeyProviderImpl.class);

        install(new CoreRestModule());
//        install(new BaseProjectModule());
        install(new ProjectApiModule());
        install(new VirtualFileSystemModule());
//        install(new VirtualFileSystemFSModule());
//        install(new MavenModule());
//        install(new ArchetypeGeneratorModule());
//        install(new GitHubModule());

//        bind(ArchetypeGenerator.class);
//        bind(DebuggerService.class);
        
//        bind(GitUserResolver.class).to(LocalGitUserResolver.class);
//        bind(GitConnectionFactory.class).to(NativeGitConnectionFactory.class);

        bind(AsynchronousJobPool.class).to(CodenvyAsynchronousJobPool.class);
        bind(ServiceBindingHelper.bindingKey(AsynchronousJobService.class, "/async/{ws-id}")).to(AsynchronousJobService.class);

        bind(String.class).annotatedWith(Names.named("api.endpoint")).toProvider(ApiEndpointProvider.class);
        bind(URI.class).annotatedWith(Names.named("api.endpoint")).toProvider(UriApiEndpointProvider.class);
        bind(String.class).annotatedWith(Names.named("user.token")).toProvider(UserTokenProvider.class);
        bind(WSocketEventBusClient.class).asEagerSingleton();

        bind(String.class).annotatedWith(Names.named("event.bus.url")).toProvider(EventBusURLProvider.class);
        bind(org.eclipse.che.ide.ext.java.server.ApiEndpointAccessibilityChecker.class);
    }

    //it's need for WSocketEventBusClient and in the future will be replaced with the property
    @Named("notification.client.event_subscriptions")
    @Provides
    @SuppressWarnings("unchecked")
    Pair<String, String>[] eventSubscriptionsProvider(@Named("event.bus.url") String eventBusURL) {
        return new Pair[] {Pair.of(eventBusURL, "")};
    }

    //it's need for EventOriginClientPropagationPolicy and in the future will be replaced with the property
    @Named("notification.client.propagate_events")
    @Provides
    @SuppressWarnings("unchecked")
    Pair<String, String>[] propagateEventsProvider(@Named("event.bus.url") String eventBusURL) {
        return new Pair[] {Pair.of(eventBusURL, "")};
    }

    @Provides
    @Named("codenvy.local.infrastructure.users")
    Set<User> users() {
        final Set<User> users = new HashSet<>(1);
        final User user = new User().withId("codenvy")
                                    .withName("codenvy")
                                    .withEmail("che@eclipse.org")
                                    .withPassword("secret");
        user.getAliases().add("che@eclipse.org");
        users.add(user);
        return users;
    }
}
