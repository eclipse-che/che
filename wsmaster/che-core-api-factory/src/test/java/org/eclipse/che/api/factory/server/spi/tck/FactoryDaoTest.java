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
package org.eclipse.che.api.factory.server.spi.tck;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.factory.Button;
import org.eclipse.che.api.factory.server.FactoryImage;
import org.eclipse.che.api.factory.server.model.impl.ActionImpl;
import org.eclipse.che.api.factory.server.model.impl.AuthorImpl;
import org.eclipse.che.api.factory.server.model.impl.ButtonAttributesImpl;
import org.eclipse.che.api.factory.server.model.impl.ButtonImpl;
import org.eclipse.che.api.factory.server.model.impl.FactoryImpl;
import org.eclipse.che.api.factory.server.model.impl.IdeImpl;
import org.eclipse.che.api.factory.server.model.impl.OnAppClosedImpl;
import org.eclipse.che.api.factory.server.model.impl.OnAppLoadedImpl;
import org.eclipse.che.api.factory.server.model.impl.OnProjectsLoadedImpl;
import org.eclipse.che.api.factory.server.model.impl.PoliciesImpl;
import org.eclipse.che.api.factory.server.spi.FactoryDao;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.test.tck.TckModuleFactory;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

/**
 * Tests {@link FactoryDao} contract.
 *
 * @author Anton Korneta
 */
@Guice(moduleFactory = TckModuleFactory.class)
@Test(suiteName = FactoryDaoTest.SUITE_NAME)
public class FactoryDaoTest {

    public static final String SUITE_NAME = "FactoryDaoTck";

    private static final int ENTRY_COUNT = 5;

    private FactoryImpl[] factories;
    private UserImpl[]    users;

    @Inject
    private FactoryDao factoryDao;

    @Inject
    private TckRepository<FactoryImpl> factoryTckRepository;

    @Inject
    private TckRepository<UserImpl> userTckRepository;

    @BeforeMethod
    public void setUp() throws Exception {
        factories = new FactoryImpl[ENTRY_COUNT];
        users = new UserImpl[ENTRY_COUNT];
        for (int i = 0; i < ENTRY_COUNT; i++) {
            users[i] = new UserImpl("userId_" + i, "email_" + i, "name" + i);
        }
        for (int i = 0; i < ENTRY_COUNT; i++) {
            factories[i] = createFactory(i, users[i].getId());
        }
        userTckRepository.createAll(asList(users));
        factoryTckRepository.createAll(asList(factories));
    }

    @AfterMethod
    public void cleanUp() throws Exception {
        factoryTckRepository.removeAll();
        userTckRepository.removeAll();
    }

    @Test(dependsOnMethods = "shouldGetFactoryById")
    public void shouldCreateFactory() throws Exception {
        final FactoryImpl factory = createFactory(10, users[0].getId());
        factory.getCreator().setUserId(factories[0].getCreator().getUserId());
        factoryDao.create(factory);

        assertEquals(factoryDao.getById(factory.getId()), factory);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenCreateNullFactory() throws Exception {
        factoryDao.create(null);
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionWhenCreatingFactoryWithExistingId() throws Exception {
        final FactoryImpl factory = createFactory(10, users[0].getId());
        final FactoryImpl existing = factories[0];
        factory.getCreator().setUserId(existing.getCreator().getUserId());
        factory.setId(existing.getId());
        factoryDao.create(factory);
    }

    // TODO fix after issue: https://github.com/eclipse/che/issues/2110
//    @Test(expectedExceptions = ConflictException.class)
//    public void shouldThrowConflictExceptionWhenCreatingFactoryWithExistingNameAndUserId() throws Exception {
//        final FactoryImpl factory = createFactory(10, users[0].getId());
//        final FactoryImpl existing = factories[0];
//        factory.getCreator().setUserId(existing.getCreator().getUserId());
//        factory.setName(existing.getName());
//        factoryDao.create(factory);
//    }

    @Test
    public void shouldUpdateFactory() throws Exception {
        final FactoryImpl update = factories[0];
        final String userId = update.getCreator().getUserId();
        update.setName("new-name");
        update.setV("5_0");
        final long currentTime = System.currentTimeMillis();
        update.setPolicies(new PoliciesImpl("ref", "match", "per-click", currentTime, currentTime + 1000));
        update.setCreator(new AuthorImpl(userId, currentTime));
        update.setButton(new ButtonImpl(new ButtonAttributesImpl("green", "icon", "opacity 0.9", true),
                                        Button.Type.NOLOGO));
        update.getIde().getOnAppClosed().getActions().add(new ActionImpl("remove file", ImmutableMap.of("file1", "/che/core/pom.xml")));
        update.getIde().getOnAppLoaded().getActions().add(new ActionImpl("edit file", ImmutableMap.of("file2", "/che/core/pom.xml")));
        update.getIde().getOnProjectsLoaded().getActions().add(new ActionImpl("open file", ImmutableMap.of("file2", "/che/pom.xml")));
        factoryDao.update(update);

        assertEquals(factoryDao.getById(update.getId()), update);
    }

// TODO fix after issue: https://github.com/eclipse/che/issues/2110
//    @Test(expectedExceptions = ConflictException.class)
//    public void shouldThrowConflictExceptionWhenUpdateFactoryWithExistingNameAndUserId() throws Exception {
//        final FactoryImpl update = factories[0];
//        update.setName(factories[1].getName());
//        update.getCreator().setUserId(factories[1].getCreator().getUserId());
//        factoryDao.update(update);
//    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenFactoryUpdateIsNull() throws Exception {
        factoryDao.update(null);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenUpdatingNonExistingFactory() throws Exception {
        factoryDao.update(createFactory(10, users[0].getId()));
    }

    @Test
    public void shouldGetFactoryById() throws Exception {
        final FactoryImpl factory = factories[0];

        assertEquals(factoryDao.getById(factory.getId()), factory);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGettingFactoryByNullId() throws Exception {
        factoryDao.getById(null);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenFactoryWithGivenIdDoesNotExist() throws Exception {
        factoryDao.getById("non-existing");
    }

    @Test
    public void shouldGetFactoryByIdAttribute() throws Exception {
        final FactoryImpl factory = factories[0];
        final List<Pair<String, String>> attributes = ImmutableList.of(Pair.of("id", factory.getId()));
        final List<FactoryImpl> result = factoryDao.getByAttribute(1, 0, attributes);

        assertEquals(new HashSet<>(result), ImmutableSet.of(factory));
    }

    @Test(dependsOnMethods = "shouldUpdateFactory")
    public void shouldFindFactoryByEmbeddedAttributes() throws Exception {
        final List<Pair<String, String>> attributes = ImmutableList.of(Pair.of("policies.match", "match"),
                                                                       Pair.of("policies.create", "perClick"),
                                                                       Pair.of("workspace.defaultEnv", "env1"));
        final FactoryImpl factory1 = factories[1];
        final FactoryImpl factory3 = factories[3];
        factory1.getPolicies().setCreate("perAccount");
        factory3.getPolicies().setMatch("update");
        factoryDao.update(factory1);
        factoryDao.update(factory3);
        final List<FactoryImpl> result = factoryDao.getByAttribute(factories.length, 0, attributes);

        assertEquals(new HashSet<>(result), ImmutableSet.of(factories[0], factories[2], factories[4]));
    }

    @Test
    public void shouldFindAllFactoriesWhenAttributesNotSpecified() throws Exception {
        final List<Pair<String, String>> attributes = emptyList();
        final List<FactoryImpl> result = factoryDao.getByAttribute(factories.length, 0, attributes);

        assertEquals(new HashSet<>(result), new HashSet<>(asList(factories)));
    }

    @Test(expectedExceptions = NotFoundException.class, dependsOnMethods = "shouldGetFactoryById")
    public void shouldRemoveFactory() throws Exception {
        final String factoryId = factories[0].getId();
        factoryDao.remove(factoryId);
        factoryDao.getById(factoryId);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenRemovingNullFactory() throws Exception {
        factoryDao.remove(null);
    }

    @Test
    public void shouldDoNothingWhenRemovingNonExistingFactory() throws Exception {
        factoryDao.remove("non-existing");
    }

    private static FactoryImpl createFactory(int index, String userId) {
        final long timeMs = System.currentTimeMillis();
        final ButtonImpl factoryButton = new ButtonImpl(new ButtonAttributesImpl("red", "logo", "style", true),
                                                        Button.Type.LOGO);
        final AuthorImpl creator = new AuthorImpl(userId , timeMs);
        final PoliciesImpl policies = new PoliciesImpl("referrer", "match", "perClick", timeMs, timeMs + 1000);
        final Set<FactoryImage> images = new HashSet<>();
        final List<ActionImpl> a1 = new ArrayList<>(singletonList(new ActionImpl("id" + index, ImmutableMap.of("key1", "value1"))));
        final OnAppLoadedImpl onAppLoaded = new OnAppLoadedImpl(a1);
        final List<ActionImpl> a2 = new ArrayList<>(singletonList(new ActionImpl("id" + index, ImmutableMap.of("key2", "value2"))));
        final OnProjectsLoadedImpl onProjectsLoaded = new OnProjectsLoadedImpl(a2);
        final List<ActionImpl> a3 = new ArrayList<>(singletonList(new ActionImpl("id" + index, ImmutableMap.of("key3", "value3"))));
        final OnAppClosedImpl onAppClosed = new OnAppClosedImpl(a3);
        final IdeImpl ide = new IdeImpl(onAppLoaded, onProjectsLoaded, onAppClosed);
        return FactoryImpl.builder()
                          .generateId()
                          .setVersion("4_0")
                          .setName("factoryName" + index)
                          .setWorkspace(createWorkspaceConfig(index))
                          .setButton(factoryButton)
                          .setCreator(creator)
                          .setPolicies(policies)
                          .setImages(images)
                          .setIde(ide)
                          .build();
    }

    public static WorkspaceConfigImpl createWorkspaceConfig(int index) {
        // Project Sources configuration
        final SourceStorageImpl source1 = new SourceStorageImpl();
        source1.setType("type1");
        source1.setLocation("location1");
        source1.setParameters(new HashMap<>(ImmutableMap.of("param1", "value1")));
        final SourceStorageImpl source2 = new SourceStorageImpl();
        source2.setType("type2");
        source2.setLocation("location2");
        source2.setParameters(new HashMap<>(ImmutableMap.of("param4", "value1")));

        // Project Configuration
        final ProjectConfigImpl pCfg1 = new ProjectConfigImpl();
        pCfg1.setPath("/path1");
        pCfg1.setType("type1");
        pCfg1.setName("project1");
        pCfg1.setDescription("description1");
        pCfg1.getMixins().addAll(asList("mixin1", "mixin2"));
        pCfg1.setSource(source1);
        pCfg1.getAttributes().putAll(ImmutableMap.of("key1", asList("v1", "v2"), "key2", asList("v1", "v2")));

        final ProjectConfigImpl pCfg2 = new ProjectConfigImpl();
        pCfg2.setPath("/path2");
        pCfg2.setType("type2");
        pCfg2.setName("project2");
        pCfg2.setDescription("description2");
        pCfg2.getMixins().addAll(asList("mixin3", "mixin4"));
        pCfg2.setSource(source2);
        pCfg2.getAttributes().putAll(ImmutableMap.of("key3", asList("v1", "v2"), "key4", asList("v1", "v2")));

        final List<ProjectConfigImpl> projects = new ArrayList<>(asList(pCfg1, pCfg2));

        // Commands
        final CommandImpl cmd1 = new CommandImpl("name1", "cmd1", "type1");
        cmd1.getAttributes().putAll(ImmutableMap.of("key1", "value1"));
        final CommandImpl cmd2 = new CommandImpl("name2", "cmd2", "type2");
        cmd2.getAttributes().putAll(ImmutableMap.of("key4", "value4"));
        final List<CommandImpl> commands = new ArrayList<>(asList(cmd1, cmd2));

        // Machine configs
        final MachineConfigImpl mCfg1 = new MachineConfigImpl();
        mCfg1.setName("name1");
        mCfg1.setDev(true);
        mCfg1.setType("type1");
        mCfg1.setLimits(new LimitsImpl(2048));
        mCfg1.getEnvVariables().putAll(ImmutableMap.of("env", "XTERM"));
        mCfg1.getServers().addAll(singleton(new ServerConfImpl("ref1", "port1", "protocol1", "path1")));
        mCfg1.setSource(new MachineSourceImpl("type1", "location1", "content1"));

        final MachineConfigImpl mCfg2 = new MachineConfigImpl();
        mCfg2.setName("name2");
        mCfg2.setDev(false);
        mCfg2.setType("type2");
        mCfg2.setLimits(new LimitsImpl(512));
        mCfg2.getEnvVariables().putAll(ImmutableMap.of("env1", "value"));
        mCfg2.getServers().add(new ServerConfImpl("ref2", "port2", "protocol2", "path2"));
        mCfg2.setSource(new MachineSourceImpl("type2", "location2", "content2"));

        final List<MachineConfigImpl> machineConfigs = new ArrayList<>(asList(mCfg1, mCfg2));

        // Environments
        final EnvironmentImpl env1 = new EnvironmentImpl();
        env1.setName("env1");
        env1.setMachineConfigs(machineConfigs);

        final EnvironmentImpl env2 = new EnvironmentImpl();
        env2.setName("env2");
        env2.setMachineConfigs(machineConfigs.stream()
                                             .map(MachineConfigImpl::new)
                                             .collect(Collectors.toList()));

        final List<EnvironmentImpl> environments = new ArrayList<>(asList(env1, env2));

        // Workspace configuration
        final WorkspaceConfigImpl wCfg = new WorkspaceConfigImpl();
        wCfg.setDefaultEnv(env1.getName());
        wCfg.setName("cfgName_" + index);
        wCfg.setDescription("description");
        wCfg.setCommands(commands);
        wCfg.setProjects(projects);
        wCfg.setEnvironments(environments);
        return wCfg;
    }
}
