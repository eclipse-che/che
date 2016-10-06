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
package org.eclipse.che.api.workspace.server.spi.tck;

import com.google.inject.Inject;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.workspace.server.event.StackPersistedEvent;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackSourceImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.server.stack.image.StackIcon;
import org.eclipse.che.commons.test.tck.TckModuleFactory;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static org.eclipse.che.api.workspace.server.spi.tck.WorkspaceDaoTest.createWorkspaceConfig;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests {@link SnapshotDao} contract.
 *
 * @author Yevhenii Voevodin
 */
@Guice(moduleFactory = TckModuleFactory.class)
@Test(suiteName = StackDaoTest.SUITE_NAME)
public class StackDaoTest {

    public static final String SUITE_NAME = "StackDaoTck";

    private static final int STACKS_SIZE = 5;

    private StackImpl[] stacks;

    @Inject
    private TckRepository<StackImpl> stackRepo;

    @Inject
    private StackDao stackDao;

    @Inject
    private EventService eventService;

    @BeforeMethod
    private void createStacks() throws TckRepositoryException {
        stacks = new StackImpl[STACKS_SIZE];
        for (int i = 0; i < STACKS_SIZE; i++) {
            stacks[i] = createStack("stack-" + i, "name-" + i);
        }
        stackRepo.createAll(asList(stacks));
    }

    @AfterMethod
    private void removeStacks() throws TckRepositoryException {
        stackRepo.removeAll();
    }

    @Test
    public void shouldGetById() throws Exception {
        final StackImpl stack = stacks[0];

        assertEquals(stackDao.getById(stack.getId()), stack);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenGettingNonExistingStack() throws Exception {
        stackDao.getById("non-existing-stack");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGettingStackByNullKey() throws Exception {
        stackDao.getById(null);
    }

    @Test(dependsOnMethods = "shouldGetById")
    public void shouldCreateStack() throws Exception {
        final StackImpl stack = createStack("new-stack", "new-stack-name");

        stackDao.create(stack);

        assertEquals(stackDao.getById(stack.getId()), stack);
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionWhenCreatingStackWithIdThatAlreadyExists() throws Exception {
        final StackImpl stack = createStack(stacks[0].getId(), "new-name");

        stackDao.create(stack);
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionWhenCreatingStackWithNameThatAlreadyExists() throws Exception {
        final StackImpl stack = createStack("new-stack-id", stacks[0].getName());

        stackDao.create(stack);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenCreatingNullStack() throws Exception {
        stackDao.create(null);
    }

    @Test(expectedExceptions = NotFoundException.class,
          dependsOnMethods = "shouldThrowNotFoundExceptionWhenGettingNonExistingStack")
    public void shouldRemoveStack() throws Exception {
        final StackImpl stack = stacks[0];

        stackDao.remove(stack.getId());

        // Should throw an exception
        stackDao.getById(stack.getId());
    }

    @Test
    public void shouldNotThrowAnyExceptionWhenRemovingNonExistingStack() throws Exception {
        stackDao.remove("non-existing");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenRemovingNull() throws Exception {
        stackDao.remove(null);
    }

    @Test(dependsOnMethods = "shouldGetById")
    public void shouldUpdateStack() throws Exception {
        final StackImpl stack = stacks[0];

        stack.setName("new-name");
        stack.setCreator("new-creator");
        stack.setDescription("new-description");
        stack.setScope("new-scope");
        stack.getTags().clear();
        stack.getTags().add("new-tag");

        // Remove an existing component
        stack.getComponents().remove(1);

        // Add a new component
        stack.getComponents().add(new StackComponentImpl("component3", "component3-version"));

        // Update an existing component
        final StackComponentImpl component = stack.getComponents().get(0);
        component.setName("new-name");
        component.setVersion("new-version");

        // Updating source
        final StackSourceImpl source = stack.getSource();
        source.setType("new-type");
        source.setOrigin("new-source");

        // Set a new icon
        stack.setStackIcon(new StackIcon("new-name", "new-media", "new-data".getBytes()));

        stackDao.update(stack);

        assertEquals(stackDao.getById(stack.getId()), new StackImpl(stack));
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldNotUpdateStackIfNewNameIsReserved() throws Exception {
        final StackImpl stack = stacks[0];
        stack.setName(stacks[1].getName());

        stackDao.update(stack);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenUpdatingNonExistingStack() throws Exception {
        stackDao.update(createStack("new-stack", "new-stack-name"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenUpdatingNullStack() throws Exception {
        stackDao.update(null);
    }

    @Test(dependsOnMethods = "shouldUpdateStack")
    public void shouldFindStacksWithSpecifiedTags() throws Exception {
        stacks[0].getTags().addAll(asList("search-tag1", "search-tag2"));
        stacks[1].getTags().addAll(asList("search-tag1", "non-search-tag"));
        stacks[2].getTags().addAll(asList("non-search-tag", "search-tag2"));
        stacks[3].getTags().addAll(asList("search-tag1", "search-tag2", "another-tag"));
        updateAll();

        final List<StackImpl> found = stackDao.searchStacks(null, asList("search-tag1", "search-tag2"), 0, 0);
        found.forEach(s -> Collections.sort(s.getTags()));
        for (StackImpl stack : stacks) {
            Collections.sort(stack.getTags());
        }

        assertEquals(new HashSet<>(found), new HashSet<>(asList(stacks[0], stacks[3])));
    }

    @Test
    public void shouldReturnAllStacksWhenSearchingWithoutTags() throws Exception {
        final List<StackImpl> found = stackDao.searchStacks(null, null, 0, 0);
        found.forEach(s -> Collections.sort(s.getTags()));
        for (StackImpl stack : stacks) {
            Collections.sort(stack.getTags());
        }

        assertEquals(new HashSet<>(found), new HashSet<>(asList(stacks)));
    }

    @Test
    public void shouldPublishStackPersistedEventAfterStackIsPersisted() throws Exception {
        final boolean[] isNotified = new boolean[] { false };
        eventService.subscribe(event -> isNotified[0] = true, StackPersistedEvent.class);

        stackDao.create(createStack("test", "test"));

        assertTrue(isNotified[0], "Event subscriber notified");
    }

    private void updateAll() throws ConflictException, NotFoundException, ServerException {
        for (StackImpl stack : stacks) {
            stackDao.update(stack);
        }
    }

    private static StackImpl createStack(String id, String name) {
        final StackImpl stack = StackImpl.builder()
                                     .setId(id)
                                     .setName(name)
                                     .setCreator("user123")
                                     .setDescription(id + "-description")
                                     .setScope(id + "-scope")
                                     .setTags(asList(id + "-tag1", id + "-tag2"))
                                     .setComponents(asList(new StackComponentImpl(id + "-component1", id + "-component1-version"),
                                                           new StackComponentImpl(id + "-component2", id + "-component2-version")))
                                     .setSource(new StackSourceImpl(id + "-type", id + "-origin"))
                                     .setStackIcon(new StackIcon(id + "-icon",
                                                                 id + "-media-type",
                                                                 "0x1234567890abcdef".getBytes()))
                                     .build();
        final WorkspaceConfigImpl config = createWorkspaceConfig("test");
        stack.setWorkspaceConfig(config);
        return stack;
    }
}
