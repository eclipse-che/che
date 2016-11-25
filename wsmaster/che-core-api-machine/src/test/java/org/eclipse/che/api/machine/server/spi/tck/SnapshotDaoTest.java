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
package org.eclipse.che.api.machine.server.spi.tck;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceRuntime;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.machine.server.exception.SnapshotException;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * Tests {@link SnapshotDao} contract.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(TckListener.class)
@Test(suiteName = SnapshotDaoTest.SUITE_NAME)
public class SnapshotDaoTest {

    public static final String SUITE_NAME = "SnapshotDaoTest";

    private static final int SNAPSHOTS_SIZE = 6;

    private SnapshotImpl[]  snapshots;
    private TestWorkspace[] workspaces;

    @Inject
    private SnapshotDao snapshotDao;

    @Inject
    private TckRepository<SnapshotImpl> snaphotRepo;

    @Inject
    private TckRepository<Workspace> workspaceRepo;

    @Inject
    private TckRepository<AccountImpl> accountRepo;

    @BeforeMethod
    private void createSnapshots() throws TckRepositoryException {
        // one account for all the workspaces
        final AccountImpl account = new AccountImpl("account1", "name", "type");

        // workspaces
        workspaces = new TestWorkspace[SNAPSHOTS_SIZE / 3];
        for (int i = 0; i < workspaces.length; i++) {
            workspaces[i] = new TestWorkspace("workspace-" + i, account.getId());
        }

        // snapshots
        snapshots = new SnapshotImpl[SNAPSHOTS_SIZE];
        for (int i = 0; i < SNAPSHOTS_SIZE; i++) {
            snapshots[i] = createSnapshot("snapshot-" + i,
                                          workspaces[i / 3].getId(), // 3 snapshot share the same workspace id
                                          "environment-" + i / 2, // 2 snapshots share the same env name
                                          "machine-" + i);
        }

        accountRepo.createAll(singletonList(account));
        workspaceRepo.createAll(asList(workspaces));
        snaphotRepo.createAll(asList(snapshots));
    }

    @AfterMethod
    private void removeSnapshots() throws TckRepositoryException {
        snaphotRepo.removeAll();
        workspaceRepo.removeAll();
        accountRepo.removeAll();
    }

    @Test
    public void shouldGetSnapshotById() throws Exception {
        final SnapshotImpl snapshot = snapshots[0];

        assertEquals(snapshotDao.getSnapshot(snapshot.getId()), snapshot);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenGettingNonExistingSnapshot() throws Exception {
        snapshotDao.getSnapshot("non-existing-snapshot");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGettingSnapshotByNullId() throws Exception {
        snapshotDao.getSnapshot(null);
    }

    @Test
    public void shouldGetSnapshotByWorkspaceEnvironmentAndMachineName() throws Exception {
        final SnapshotImpl snapshot = snapshots[0];

        assertEquals(snapshotDao.getSnapshot(snapshot.getWorkspaceId(),
                                             snapshot.getEnvName(),
                                             snapshot.getMachineName()), snapshot);
    }

    @Test(expectedExceptions = NotFoundException.class, dataProvider = "missingSnapshots")
    public void shouldThrowNotFoundExceptionWhenSnapshotMissing(String wsId, String envName, String machineName) throws Exception {
        snapshotDao.getSnapshot(wsId, envName, machineName);
    }

    @Test(expectedExceptions = NullPointerException.class, dataProvider = "nullParameterVariations")
    public void shouldThrowNpeWhenAnyOfGetSnapshotParametersIsNull(String wsId, String envName, String machineName) throws Exception {
        snapshotDao.getSnapshot(wsId, envName, machineName);
    }

    @Test
    public void shouldFindSnapshotsByWorkspaceAndNamespace() throws Exception {
        final SnapshotImpl snapshot = snapshots[0];

        final List<SnapshotImpl> found = snapshotDao.findSnapshots(snapshot.getWorkspaceId());

        assertEquals(new HashSet<>(found), new HashSet<>(asList(snapshots[0], snapshots[1], snapshots[2])));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenSearchingSnapshotsByNullWorkspaceId() throws Exception {
        snapshotDao.findSnapshots(null);
    }

    @Test(dependsOnMethods = "shouldGetSnapshotById")
    public void shouldSaveSnapshot() throws Exception {
        final SnapshotImpl newSnapshot = createSnapshot("new-snapshot",
                                                        workspaces[0].getId(),
                                                        "env-name",
                                                        "machine-name");

        snapshotDao.saveSnapshot(newSnapshot);

        assertEquals(snapshotDao.getSnapshot(newSnapshot.getId()), new SnapshotImpl(newSnapshot));
    }

    @Test(expectedExceptions = SnapshotException.class)
    public void shouldNotSaveSnapshotWithReservedId() throws Exception {
        final SnapshotImpl snapshot = snapshots[0];
        snapshot.setWorkspaceId("new-workspace");
        snapshot.setEnvName("new-env");
        snapshot.setMachineName("new-machine");

        snapshotDao.saveSnapshot(snapshot);
    }

    @Test(expectedExceptions = SnapshotException.class)
    public void shouldNotSaveSnapshotForMachineIfSnapshotForSuchMachineAlreadyExists() throws Exception {
        final SnapshotImpl snapshot = snapshots[0];
        snapshot.setId("new-id");

        snapshotDao.saveSnapshot(snapshot);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenSavingNull() throws Exception {
        snapshotDao.saveSnapshot(null);
    }

    @Test(expectedExceptions = NotFoundException.class,
          dependsOnMethods = "shouldThrowNotFoundExceptionWhenGettingNonExistingSnapshot")
    public void shouldRemoveSnapshot() throws Exception {
        final SnapshotImpl snapshot = snapshots[0];

        try {
            snapshotDao.removeSnapshot(snapshot.getId());
        } catch (NotFoundException x) {
            fail("Should remove snapshot");
        }

        snapshotDao.getSnapshot(snapshot.getId());
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenRemovingNonExistingSnapshot() throws Exception {
        snapshotDao.removeSnapshot("non-existing-id");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenRemovingNull() throws Exception {
        snapshotDao.removeSnapshot(null);
    }

    @Test(dependsOnMethods = "shouldFindSnapshotsByWorkspaceAndNamespace")
    public void replacesSnapshots() throws Exception {
        final SnapshotImpl newSnapshot = createSnapshot("new-snapshot",
                                                        snapshots[0].getWorkspaceId(),
                                                        snapshots[0].getEnvName(),
                                                        snapshots[0].getMachineName());

        final List<SnapshotImpl> replaced = snapshotDao.replaceSnapshots(newSnapshot.getWorkspaceId(),
                                                                         newSnapshot.getEnvName(),
                                                                         singletonList(newSnapshot));

        assertEquals(new HashSet<>(replaced), Sets.newHashSet(snapshots[0], snapshots[1]));
        final HashSet<SnapshotImpl> actual = new HashSet<>(snapshotDao.findSnapshots(this.snapshots[0].getWorkspaceId()));
        final HashSet<SnapshotImpl> expected = Sets.newHashSet(newSnapshot, this.snapshots[2]);
        assertEquals(actual, expected);
    }

    @DataProvider(name = "missingSnapshots")
    public Object[][] missingSnapshots() {
        final SnapshotImpl snapshot = snapshots[0];
        return new Object[][] {
                {"non-existing-workspace-id", snapshot.getEnvName(), snapshot.getMachineName()},
                {snapshot.getWorkspaceId(), "non-existing-env", snapshot.getMachineName()},
                {snapshot.getWorkspaceId(), snapshot.getEnvName(), "non-existing-machine-name"}
        };
    }

    @DataProvider(name = "nullParameterVariations")
    public Object[][] nullParameterVariations() {
        final SnapshotImpl snapshot = snapshots[0];
        return new Object[][] {
                {null, snapshot.getEnvName(), snapshot.getMachineName()},
                {snapshot.getWorkspaceId(), null, snapshot.getMachineName()},
                {snapshot.getWorkspaceId(), snapshot.getEnvName(), null}
        };
    }

    private static SnapshotImpl createSnapshot(String id,
                                               String workspaceId,
                                               String envName,
                                               String machineName) {
        return SnapshotImpl.builder()
                           .setId(id)
                           .setType(id + "type")
                           .setMachineSource(new MachineSourceImpl(id + "source-type",
                                                                   id + "source-location",
                                                                   id + "source-content"))
                           .setCreationDate(System.currentTimeMillis())
                           .setDev(true)
                           .setWorkspaceId(workspaceId)
                           .setEnvName(envName)
                           .setMachineName(machineName)
                           .build();
    }

    private static class TestWorkspace implements Workspace {

        private final String id;
        private final String accountId;

        public TestWorkspace(String id, String accountId) {
            this.id = id;
            this.accountId = accountId;
        }

        @Override
        public String getId() { return id; }

        @Override
        public String getNamespace() { return accountId; }

        @Override
        public WorkspaceStatus getStatus() { return null; }

        @Override
        public Map<String, String> getAttributes() { return null; }

        @Override
        public boolean isTemporary() { return false; }

        @Override
        public WorkspaceConfig getConfig() { return null; }

        @Override
        public WorkspaceRuntime getRuntime() { return null; }
    }
}
