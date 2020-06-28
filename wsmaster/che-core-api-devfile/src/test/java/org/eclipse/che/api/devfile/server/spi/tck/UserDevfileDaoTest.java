/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.devfile.server.spi.tck;

import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.devfile.server.TestObjectGenerator.createUserDevfile;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.devfile.server.spi.UserDevfileDao;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ActionImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.MetadataImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ProjectImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.SourceImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TckListener.class)
@Test(suiteName = UserDevfileDaoTest.SUITE_NAME)
public class UserDevfileDaoTest {
  public static final String CURRENT_USER_ID =
      EnvironmentContext.getCurrent().getSubject().getUserId();
  public static final String SUITE_NAME = "DevfileDaoTck";
  private static final int ENTRY_COUNT = 10;

  private UserDevfileImpl[] devfiles;
  private UserImpl[] users;

  @Inject private UserDevfileDao userDevfileDaoDao;

  @Inject private TckRepository<UserDevfileImpl> devfileTckRepository;

  @Inject private TckRepository<UserImpl> userTckRepository;

  @BeforeMethod
  public void setUp() throws Exception {
    devfiles = new UserDevfileImpl[ENTRY_COUNT];
    users = new UserImpl[ENTRY_COUNT];
    for (int i = 0; i < ENTRY_COUNT; i++) {
      users[i] = new UserImpl("userId_" + i, "email_" + i, "name" + i);
    }
    for (int i = 0; i < ENTRY_COUNT; i++) {
      devfiles[i] =
          createUserDevfile(
              NameGenerator.generate("id-" + i + "-", 6),
              NameGenerator.generate("devfileName-" + i, 6));
    }
    userTckRepository.createAll(Arrays.asList(users));
    devfileTckRepository.createAll(Stream.of(devfiles).map(UserDevfileImpl::new).collect(toList()));
  }

  @AfterMethod
  public void cleanUp() throws Exception {
    devfileTckRepository.removeAll();
    userTckRepository.removeAll();
  }

  @Test
  public void shouldGetUserDevfileById() throws Exception {
    final UserDevfileImpl devfile = devfiles[0];

    assertEquals(userDevfileDaoDao.getById(devfile.getId()), devfile);
  }

  @Test(dependsOnMethods = "shouldGetUserDevfileById")
  public void shouldCreateUserDevfile() throws Exception {
    final UserDevfileImpl devfile = createUserDevfile();
    userDevfileDaoDao.create(devfile);

    assertEquals(userDevfileDaoDao.getById(devfile.getId()), new UserDevfileImpl(devfile));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenCreateNullDevfile() throws Exception {
    userDevfileDaoDao.create(null);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionWhenCreatingUserDevfileWithExistingId() throws Exception {
    // given
    final UserDevfileImpl devfile = createUserDevfile();
    final UserDevfileImpl existing = devfiles[0];
    devfile.setId(existing.getId());
    // when
    userDevfileDaoDao.create(devfile);
    // then
  }

  @Test
  public void shouldUpdateUserDevfile() throws Exception {
    // given
    final UserDevfileImpl update = devfiles[0];
    update.setApiVersion("V15.0");
    update.setProjects(
        ImmutableList.of(
            new ProjectImpl(
                "projectUp2",
                new SourceImpl(
                    "typeUp2",
                    "http://location",
                    "branch2",
                    "point2",
                    "tag2",
                    "commit2",
                    "sparseCheckoutDir2"),
                "path2")));
    update.setComponents(ImmutableList.of(new ComponentImpl("type3", "id54")));
    update.setCommands(
        ImmutableList.of(
            new CommandImpl(
                new CommandImpl(
                    "cmd1",
                    singletonList(
                        new ActionImpl(
                            "exe44", "compo2nent2", "run.sh", "/home/user/2", null, null)),
                    singletonMap("attr1", "value1"),
                    null))));
    update.setAttributes(ImmutableMap.of("key2", "val34"));
    update.setMetadata(new MetadataImpl("myNewName"));
    // when
    userDevfileDaoDao.update(update);
    // then
    assertEquals(userDevfileDaoDao.getById(update.getId()), update);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldNotUpdateWorkspaceWhichDoesNotExist() throws Exception {
    // given
    final UserDevfileImpl userDevfile = devfiles[0];
    userDevfile.setId("non-existing-devfile");
    // when
    userDevfileDaoDao.update(userDevfile);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenUpdatingNull() throws Exception {
    userDevfileDaoDao.update(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenGetByIdNull() throws Exception {
    userDevfileDaoDao.getById(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenDeleteNull() throws Exception {
    userDevfileDaoDao.getById(null);
  }

  @Test(expectedExceptions = NotFoundException.class, dependsOnMethods = "shouldGetUserDevfileById")
  public void shouldRemoveDevfile() throws Exception {
    final String userDevfileId = devfiles[0].getId();
    userDevfileDaoDao.remove(userDevfileId);
    userDevfileDaoDao.getById(userDevfileId);
  }

  @Test
  public void shouldDoNothingWhenRemovingNonExistingUserDevfile() throws Exception {
    userDevfileDaoDao.remove("non-existing");
  }

  @Test
  public void shouldBeAbleToGetAvailableToUserDevfiles() throws ServerException {
    // given
    // when
    final Page<UserDevfileImpl> result =
        userDevfileDaoDao.getDevfiles(
            CURRENT_USER_ID, 30, 0, Collections.emptyList(), Collections.emptyList());
    // then
    assertEquals(new HashSet<>(result.getItems()), new HashSet<>(asList(devfiles)));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldThrowIllegalStateExceptionOnNegativeLimit() throws Exception {
    userDevfileDaoDao.getDevfiles(
        CURRENT_USER_ID, 0, -2, Collections.emptyList(), Collections.emptyList());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldThrowIllegalStateExceptionOnNegativeSkipCount() throws Exception {
    userDevfileDaoDao.getDevfiles(
        CURRENT_USER_ID, -2, 0, Collections.emptyList(), Collections.emptyList());
  }

  @Test
  public void shouldBeAbleToGetAvailableToUserDevfilesWithFilter() throws ServerException {
    // given
    // when
    final Page<UserDevfileImpl> result =
        userDevfileDaoDao.getDevfiles(
            CURRENT_USER_ID,
            30,
            0,
            ImmutableList.of(new Pair<>("devfile.metadata.name", "like:devfileName%")),
            Collections.emptyList());
    // then
    assertEquals(new HashSet<>(result.getItems()), new HashSet<>(asList(devfiles)));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldNotAllowSearchWithInvalidFilter() throws ServerException {
    // given
    // when
    final Page<UserDevfileImpl> result =
        userDevfileDaoDao.getDevfiles(
            CURRENT_USER_ID,
            30,
            0,
            ImmutableList.of(
                new Pair<>("id", "like:dev%"),
                new Pair<>("devfile.metadata.something", "like:devfileName%")),
            Collections.emptyList());
    // then
  }

  @Test
  public void shouldBeAbleToGetAvailableToUserDevfilesWithFilter2()
      throws ServerException, NotFoundException, ConflictException {
    // given
    final UserDevfileImpl update = devfiles[0];
    update.setName("New345Name");
    userDevfileDaoDao.update(update);
    // when
    final Page<UserDevfileImpl> result =
        userDevfileDaoDao.getDevfiles(
            CURRENT_USER_ID,
            30,
            0,
            ImmutableList.of(new Pair<>("devfile.metadata.name", "like:%w345N%")),
            Collections.emptyList());
    // then
    assertEquals(new HashSet<>(result.getItems()), ImmutableSet.of(update));
  }

  @Test
  public void shouldBeAbleToGetAvailableToUserDevfilesWithFilterAndLimit()
      throws ServerException, NotFoundException, ConflictException {
    // given
    // when
    final Page<UserDevfileImpl> result =
        userDevfileDaoDao.getDevfiles(
            CURRENT_USER_ID,
            5,
            0,
            ImmutableList.of(new Pair<>("devfile.metadata.name", "like:devfileName%")),
            Collections.emptyList());
    // then
    assertEquals(result.getItems().size(), 5);
  }

  @Test
  public void shouldBeAbleToGetDevfilesSortedById()
      throws ServerException, NotFoundException, ConflictException {
    // given
    UserDevfileImpl[] expected =
        Arrays.stream(devfiles)
            .sorted(Comparator.comparing(UserDevfileImpl::getId))
            .toArray(UserDevfileImpl[]::new);
    // when
    final Page<UserDevfileImpl> result =
        userDevfileDaoDao.getDevfiles(
            CURRENT_USER_ID,
            devfiles.length,
            0,
            Collections.emptyList(),
            ImmutableList.of(new Pair<>("id", "asc")));
    // then
    assertEquals(result.getItems().stream().toArray(UserDevfileImpl[]::new), expected);
  }

  @Test
  public void shouldBeAbleToGetDevfilesSortedByIdReverse()
      throws ServerException, NotFoundException, ConflictException {
    // given
    UserDevfileImpl[] expected =
        Arrays.stream(devfiles)
            .sorted(Comparator.comparing(UserDevfileImpl::getId).reversed())
            .toArray(UserDevfileImpl[]::new);
    // when
    final Page<UserDevfileImpl> result =
        userDevfileDaoDao.getDevfiles(
            CURRENT_USER_ID,
            devfiles.length,
            0,
            Collections.emptyList(),
            ImmutableList.of(new Pair<>("id", "desc")));
    // then
    assertEquals(result.getItems().stream().toArray(UserDevfileImpl[]::new), expected);
  }

  @Test
  public void shouldBeAbleToGetDevfilesSortedByName()
      throws ServerException, NotFoundException, ConflictException {
    // given
    UserDevfileImpl[] expected =
        Arrays.stream(devfiles)
            .sorted(Comparator.comparing(UserDevfileImpl::getName))
            .toArray(UserDevfileImpl[]::new);
    // when
    final Page<UserDevfileImpl> result =
        userDevfileDaoDao.getDevfiles(
            CURRENT_USER_ID,
            devfiles.length,
            0,
            Collections.emptyList(),
            ImmutableList.of(new Pair<>("devfile.metadata.name", "asc")));
    // then
    assertEquals(result.getItems().stream().toArray(UserDevfileImpl[]::new), expected);
  }

  @Test(dataProvider = "boundsdataprovider")
  public void shouldBeAbleToGetDevfilesSortedByNameWithSpecificMaxItemsAndSkipCount(
      int maxitems, int skipCount) throws ServerException, NotFoundException, ConflictException {
    // given
    UserDevfileImpl[] expected =
        Arrays.stream(
                Arrays.copyOfRange(devfiles, skipCount, min(devfiles.length, skipCount + maxitems)))
            .sorted(Comparator.comparing(UserDevfileImpl::getId))
            .toArray(UserDevfileImpl[]::new);
    // when
    final Page<UserDevfileImpl> result =
        userDevfileDaoDao.getDevfiles(
            CURRENT_USER_ID,
            maxitems,
            skipCount,
            Collections.emptyList(),
            ImmutableList.of(new Pair<>("id", "asc")));
    // then
    assertEquals(result.getItems().stream().toArray(UserDevfileImpl[]::new), expected);
  }

  @DataProvider
  public Object[][] boundsdataprovider() {
    return new Object[][] {
      {1, 1},
      {1, 4},
      {4, 5},
      {6, 8},
      {1, ENTRY_COUNT},
      {ENTRY_COUNT, ENTRY_COUNT},
      {ENTRY_COUNT, 1},
      {ENTRY_COUNT, 8}
    };
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "The number of items has to be positive.")
  public void shouldNotAllowZeroMaxItemsToSearch()
      throws ServerException, NotFoundException, ConflictException {
    // given
    // when
    userDevfileDaoDao.getDevfiles(
        CURRENT_USER_ID, 0, 0, Collections.emptyList(), Collections.emptyList());
    // then
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "The number of items has to be positive.")
  public void shouldNotAllowNegativeMaxItemsToSearch()
      throws ServerException, NotFoundException, ConflictException {
    // given
    // when
    userDevfileDaoDao.getDevfiles(
        CURRENT_USER_ID, -5, 0, Collections.emptyList(), Collections.emptyList());
    // then
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp =
          "The number of items to skip can't be negative or greater than 2147483647")
  public void shouldNotAllowNegativeItemsToSkipToSearch()
      throws ServerException, NotFoundException, ConflictException {
    // given
    // when
    userDevfileDaoDao.getDevfiles(
        CURRENT_USER_ID, 5, -1, Collections.emptyList(), Collections.emptyList());
    // then
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp =
          "Invalid sort order direction\\. Possible values 'asc' or 'desc'\\."
              + " But got: \\[\\{first=meta, second=ddd}, \\{first=id, second=bla}]")
  public void shouldFailOnInvalidSortOrder()
      throws ServerException, NotFoundException, ConflictException {
    // given
    // when
    userDevfileDaoDao.getDevfiles(
        CURRENT_USER_ID,
        5,
        4,
        Collections.emptyList(),
        ImmutableList.of(
            new Pair<>("devfile.metadata.name", "asc"),
            new Pair<>("meta", "ddd"),
            new Pair<>("meta", "DesC"),
            new Pair<>("id", "bla")));
    // then
  }
}
