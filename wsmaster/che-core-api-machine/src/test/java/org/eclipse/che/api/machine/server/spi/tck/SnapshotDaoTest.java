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

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.machine.server.exception.SnapshotException;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.commons.test.tck.TckModuleFactory;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * Tests {@link SnapshotDao} contract.
 *
 * @author Yevhenii Voevodin
 */
@Guice(moduleFactory = TckModuleFactory.class)
@Test(suiteName = SnapshotDaoTest.SUITE_NAME)
public class SnapshotDaoTest {

    public static final String SUITE_NAME = "SnapshotDaoTest";

    private static final int SNAPSHOTS_SIZE = 6;

    private SnapshotImpl[] snapshots;

    @Inject
    private SnapshotDao snapshotDao;

    @Inject
    private TckRepository<SnapshotImpl> snaphotRepo;

    @BeforeMethod
    private void createSnapshots() throws TckRepositoryException {
        snapshots = new SnapshotImpl[SNAPSHOTS_SIZE];
        for (int i = 0; i < SNAPSHOTS_SIZE; i++) {
            snapshots[i] = createSnapshot("snapshot-" + i,
                                          "workspace-" + i / 3, // 3 snapshot share the same workspace id
                                          "environment-" + i / 2, // 2 snapshots share the same env name
                                          "machine-" + i);
        }
        snaphotRepo.createAll(asList(snapshots));
    }

    @AfterMethod
    private void removeSnapshots() throws TckRepositoryException {
        snaphotRepo.removeAll();
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
                                                        "workspace-id",
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
        assertEquals(new HashSet<>(snapshotDao.findSnapshots(snapshots[0].getWorkspaceId())),
                     Sets.newHashSet(newSnapshot, snapshots[2]));
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
}
