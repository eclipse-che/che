/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
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
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.local.LocalPreferenceDaoImpl;
import org.eclipse.che.api.local.LocalUserDaoImpl;
import org.eclipse.che.api.project.server.BaseProjectModule;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.vfs.server.VirtualFileSystemModule;
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.everrest.CodenvyAsynchronousJobPool;
import org.eclipse.che.generator.archetype.ArchetypeGenerator;
import org.eclipse.che.generator.archetype.ArchetypeGeneratorModule;
import org.eclipse.che.git.impl.nativegit.NativeGitConnectionFactory;
import org.eclipse.che.ide.ext.github.server.inject.GitHubModule;
import org.eclipse.che.ide.ext.java.jdi.server.DebuggerService;
import org.eclipse.che.ide.ext.openshift.server.inject.OpenshiftModule;
import org.eclipse.che.ide.ext.ssh.server.SshKeyStore;
import org.eclipse.che.ide.extension.maven.server.inject.MavenModule;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.security.oauth.RemoteOAuthTokenProvider;
import org.eclipse.che.vfs.impl.fs.AutoMountVirtualFileSystemRegistry;
import org.eclipse.che.vfs.impl.fs.LocalFSMountStrategy;
import org.eclipse.che.vfs.impl.fs.LocalFileSystemRegistryPlugin;
import org.eclipse.che.vfs.impl.fs.MachineFSMountStrategy;
import org.eclipse.che.vfs.impl.fs.VirtualFileSystemFSModule;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.guice.ServiceBindingHelper;

import javax.inject.Named;
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

        bind(LocalFileSystemRegistryPlugin.class);

        //TODO it's temporary solution. Ext war should not have binding for DAO.
        bind(UserDao.class).to(LocalUserDaoImpl.class);
        bind(PreferenceDao.class).to(LocalPreferenceDaoImpl.class);

        bind(LocalFSMountStrategy.class).to(MachineFSMountStrategy.class);
        bind(VirtualFileSystemRegistry.class).to(AutoMountVirtualFileSystemRegistry.class);
        bind(OAuthTokenProvider.class).to(RemoteOAuthTokenProvider.class);
        bind(org.eclipse.che.ide.ext.ssh.server.SshKeyStore.class)
                .to(org.eclipse.che.ide.ext.ssh.server.UserProfileSshKeyStore.class);
        bind(org.eclipse.che.git.impl.nativegit.ssh.SshKeyProvider.class)
                .to(org.eclipse.che.git.impl.nativegit.ssh.SshKeyProviderImpl.class);

        install(new CoreRestModule());
        install(new BaseProjectModule());
        install(new VirtualFileSystemModule());
        install(new VirtualFileSystemFSModule());
        install(new MavenModule());
        install(new ArchetypeGeneratorModule());
        install(new GitHubModule());
        install(new OpenshiftModule());

        bind(ArchetypeGenerator.class);
        bind(DebuggerService.class);

        bind(GitConnectionFactory.class).to(NativeGitConnectionFactory.class);

        bind(AsynchronousJobPool.class).to(CodenvyAsynchronousJobPool.class);
        bind(ServiceBindingHelper.bindingKey(AsynchronousJobService.class, "/async/{ws-id}")).to(AsynchronousJobService.class);

        bind(String.class).annotatedWith(Names.named("api.endpoint")).toProvider(ApiEndpointProvider.class);
        bind(String.class).annotatedWith(Names.named("user.token")).toProvider(UserTokenProvider.class);
        bind(WSocketEventBusClient.class).asEagerSingleton();

        bind(String.class).annotatedWith(Names.named("event.bus.url")).toProvider(EventBusURLProvider.class);
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
