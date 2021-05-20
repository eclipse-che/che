/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.devfile.server.TestObjectGenerator.createUserDevfile;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.devfile.UserDevfile;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.devfile.server.TestObjectGenerator;
import org.eclipse.che.api.devfile.server.event.BeforeDevfileRemovedEvent;
import org.eclipse.che.api.devfile.server.jpa.JpaUserDevfileDao;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.devfile.server.spi.UserDevfileDao;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ActionImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.MetadataImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ProjectImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.SourceImpl;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TckListener.class)
@Test(suiteName = UserDevfileDaoTest.SUITE_NAME)
public class UserDevfileDaoTest {

  public static final String SUITE_NAME = "DevfileDaoTck";
  private static final int ENTRY_COUNT = 10;
  private static final int COUNT_OF_ACCOUNTS = 6;

  private UserDevfileImpl[] devfiles;
  private AccountImpl[] accounts;

  @Inject private EventService eventService;

  @Inject private UserDevfileDao userDevfileDaoDao;

  @Inject private TckRepository<UserDevfileImpl> devfileTckRepository;

  @Inject private TckRepository<UserImpl> userTckRepository;

  @Inject private TckRepository<AccountImpl> accountRepo;

  @Inject private Provider<EntityManager> entityManagerProvider;

  @BeforeMethod
  public void setUp() throws Exception {
    accounts = new AccountImpl[COUNT_OF_ACCOUNTS];
    for (int i = 0; i < COUNT_OF_ACCOUNTS; i++) {
      accounts[i] = new AccountImpl("accountId" + i, "accountName" + i, "test");
    }

    devfiles = new UserDevfileImpl[ENTRY_COUNT];
    for (int i = 0; i < ENTRY_COUNT; i++) {
      AccountImpl account = accounts[i / 2];
      devfiles[i] =
          createUserDevfile(
              NameGenerator.generate("id-" + i + "-", 6),
              account,
              NameGenerator.generate("devfileName-" + i, 6));
    }
    accountRepo.createAll(Stream.of(accounts).map(AccountImpl::new).collect(toList()));
    devfileTckRepository.createAll(Stream.of(devfiles).map(UserDevfileImpl::new).collect(toList()));
  }

  @AfterMethod
  public void cleanUp() throws Exception {
    devfileTckRepository.removeAll();
    accountRepo.removeAll();
  }

  @Test
  public void shouldGetUserDevfileById() throws Exception {
    final UserDevfileImpl devfile = devfiles[0];

    assertEquals(userDevfileDaoDao.getById(devfile.getId()), Optional.of(devfile));
  }

  @Test(dependsOnMethods = "shouldGetUserDevfileById")
  public void shouldCreateUserDevfile() throws Exception {
    // given
    final UserDevfileImpl devfile = createUserDevfile(accounts[0]);
    // when
    userDevfileDaoDao.create(devfile);

    assertEquals(
        userDevfileDaoDao.getById(devfile.getId()), Optional.of(new UserDevfileImpl(devfile)));
  }

  @Test
  public void shouldCreateUserDevfileWithNullDescription() throws Exception {
    // given
    final UserDevfileImpl devfile = createUserDevfile(accounts[0]);
    devfile.setDescription(null);
    // when
    userDevfileDaoDao.create(devfile);

    Optional<UserDevfile> devfileOptional = userDevfileDaoDao.getById(devfile.getId());
    assertTrue(devfileOptional.isPresent());
    assertNull(devfileOptional.get().getDescription());
    assertEquals(devfileOptional, Optional.of(new UserDevfileImpl(devfile)));
  }

  @Test
  public void shouldCreateUserDevfileWithEmptyMataName() throws Exception {
    // given
    final UserDevfileImpl devfile = createUserDevfile(accounts[0]);
    DevfileImpl newDevfile = new DevfileImpl(devfile.getDevfile());
    MetadataImpl newMeta = new MetadataImpl();
    newMeta.setGenerateName("gener-");
    newDevfile.setMetadata(newMeta);
    devfile.setDevfile(newDevfile);
    // when
    userDevfileDaoDao.create(devfile);

    Optional<UserDevfile> devfileOptional = userDevfileDaoDao.getById(devfile.getId());
    assertTrue(devfileOptional.isPresent());
    UserDevfile actual = devfileOptional.get();
    assertNull(actual.getDevfile().getMetadata().getName());
    assertNotNull(actual.getDevfile().getMetadata().getGenerateName());
    assertEquals(devfileOptional, Optional.of(new UserDevfileImpl(devfile)));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenCreateNullDevfile() throws Exception {
    userDevfileDaoDao.create(null);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionWhenCreatingUserDevfileWithExistingId() throws Exception {
    // given
    final UserDevfileImpl devfile = createUserDevfile(accounts[0]);
    final UserDevfileImpl existing = devfiles[0];
    devfile.setId(existing.getId());
    // when
    userDevfileDaoDao.create(devfile);
    // then
  }

  @Test
  public void shouldUpdateUserDevfile() throws Exception {
    // given

    DevfileImpl newDevfile = TestObjectGenerator.createDevfile("newUpdate");
    newDevfile.setApiVersion("V15.0");
    newDevfile.setProjects(
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
    newDevfile.setComponents(ImmutableList.of(new ComponentImpl("type3", "id54")));
    newDevfile.setCommands(
        ImmutableList.of(
            new CommandImpl(
                new CommandImpl(
                    "cmd1",
                    Collections.singletonList(
                        new ActionImpl(
                            "exe44", "compo2nent2", "run.sh", "/home/user/2", null, null)),
                    Collections.singletonMap("attr1", "value1"),
                    null))));
    newDevfile.setAttributes(ImmutableMap.of("key2", "val34"));
    newDevfile.setMetadata(new MetadataImpl("myNewName"));
    final UserDevfileImpl update = devfiles[0];
    update.setDevfile(newDevfile);
    // when
    userDevfileDaoDao.update(update);
    // then
    assertEquals(userDevfileDaoDao.getById(update.getId()), Optional.of(update));
  }

  @Test
  public void shouldNotUpdateWorkspaceWhichDoesNotExist() throws Exception {
    // given
    final UserDevfileImpl userDevfile = devfiles[0];
    userDevfile.setId("non-existing-devfile");
    // when
    Optional<UserDevfile> result = userDevfileDaoDao.update(userDevfile);
    // then
    assertFalse(result.isPresent());
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

  @Test(dependsOnMethods = "shouldGetUserDevfileById")
  public void shouldRemoveDevfile() throws Exception {
    final String userDevfileId = devfiles[0].getId();
    userDevfileDaoDao.remove(userDevfileId);
    Optional<UserDevfile> result = userDevfileDaoDao.getById(userDevfileId);

    assertFalse(result.isPresent());
  }

  @Test
  public void shouldDoNothingWhenRemovingNonExistingUserDevfile() throws Exception {
    userDevfileDaoDao.remove("non-existing");
  }

  @Test
  public void shouldBeAbleToGetAvailableToUserDevfiles() throws ServerException {
    // given
    // when
    final Page<UserDevfile> result =
        userDevfileDaoDao.getDevfiles(30, 0, Collections.emptyList(), Collections.emptyList());
    // then
    assertEquals(new HashSet<>(result.getItems()), new HashSet<>(asList(devfiles)));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldThrowIllegalStateExceptionOnNegativeLimit() throws Exception {
    userDevfileDaoDao.getDevfiles(0, -2, Collections.emptyList(), Collections.emptyList());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldThrowIllegalStateExceptionOnNegativeSkipCount() throws Exception {
    userDevfileDaoDao.getDevfiles(-2, 0, Collections.emptyList(), Collections.emptyList());
  }

  @Test
  public void shouldBeAbleToGetAvailableToUserDevfilesWithFilter() throws ServerException {
    // given
    // when
    final Page<UserDevfile> result =
        userDevfileDaoDao.getDevfiles(
            30,
            0,
            ImmutableList.of(new Pair<>("name", "like:devfileName%")),
            Collections.emptyList());
    // then
    assertEquals(new HashSet<>(result.getItems()), new HashSet<>(asList(devfiles)));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldNotAllowSearchWithInvalidFilter() throws ServerException {
    // given
    // when
    final Page<UserDevfile> result =
        userDevfileDaoDao.getDevfiles(
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
    final Page<UserDevfile> result =
        userDevfileDaoDao.getDevfiles(
            30, 0, ImmutableList.of(new Pair<>("name", "like:%w345N%")), Collections.emptyList());
    // then
    assertEquals(new HashSet<>(result.getItems()), ImmutableSet.of(update));
  }

  @Test
  public void shouldBeAbleToGetAvailableToUserDevfilesWithFilterAndLimit()
      throws ServerException, NotFoundException, ConflictException {
    // given
    final UserDevfileImpl update = devfiles[0];
    update.setName("New345Name");
    userDevfileDaoDao.update(update);
    // when
    final Page<UserDevfile> result =
        userDevfileDaoDao.getDevfiles(
            12,
            0,
            ImmutableList.of(new Pair<>("name", "like:devfileName%")),
            Collections.emptyList());
    // then
    assertEquals(result.getItems().size(), 9);
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
    final Page<UserDevfile> result =
        userDevfileDaoDao.getDevfiles(
            devfiles.length, 0, Collections.emptyList(), ImmutableList.of(new Pair<>("id", "asc")));
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
    final Page<UserDevfile> result =
        userDevfileDaoDao.getDevfiles(
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
    final Page<UserDevfile> result =
        userDevfileDaoDao.getDevfiles(
            devfiles.length,
            0,
            Collections.emptyList(),
            ImmutableList.of(new Pair<>("name", "asc")));
    // then
    assertEquals(result.getItems().stream().toArray(UserDevfileImpl[]::new), expected);
  }

  @Test
  public void shouldSendDevfileDeletedEventOnRemoveUserDevfile() throws Exception {
    // given
    final String userDevfileId = devfiles[0].getId();
    final boolean[] isNotified = new boolean[] {false};
    eventService.subscribe(event -> isNotified[0] = true, BeforeDevfileRemovedEvent.class);
    // when
    userDevfileDaoDao.remove(userDevfileId);
    // then
    assertTrue(isNotified[0], "Event subscriber notified");
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
    final Page<UserDevfile> result =
        userDevfileDaoDao.getDevfiles(
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
    userDevfileDaoDao.getDevfiles(0, 0, Collections.emptyList(), Collections.emptyList());
    // then
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "The number of items has to be positive.")
  public void shouldNotAllowNegativeMaxItemsToSearch()
      throws ServerException, NotFoundException, ConflictException {
    // given
    // when
    userDevfileDaoDao.getDevfiles(-5, 0, Collections.emptyList(), Collections.emptyList());
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
    userDevfileDaoDao.getDevfiles(5, -1, Collections.emptyList(), Collections.emptyList());
    // then
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp =
          "Invalid sort order direction\\. Possible values are 'asc' or 'desc'"
              + " but got: \\[\\{first=name, second=ddd}, \\{first=id, second=bla}]")
  public void shouldFailOnInvalidSortOrder()
      throws ServerException, NotFoundException, ConflictException {
    // given
    // when
    userDevfileDaoDao.getDevfiles(
        5,
        4,
        Collections.emptyList(),
        ImmutableList.of(
            new Pair<>("id", "asc"),
            new Pair<>("name", "ddd"),
            new Pair<>("name", "DesC"),
            new Pair<>("id", "bla")));
    // then
  }

  @Test
  public void shouldGetDevfilesByNamespace() throws Exception {
    final UserDevfileImpl devfile1 = devfiles[0];
    final UserDevfileImpl devfile2 = devfiles[1];
    assertEquals(devfile1.getNamespace(), devfile2.getNamespace(), "Namespaces must be the same");

    final Page<UserDevfile> found = userDevfileDaoDao.getByNamespace(devfile1.getNamespace(), 6, 0);

    assertEquals(found.getTotalItemsCount(), 2);
    assertEquals(found.getItemsCount(), 2);
    assertEquals(new HashSet<>(found.getItems()), new HashSet<>(asList(devfile1, devfile2)));
  }

  @Test
  public void emptyListShouldBeReturnedWhenThereAreNoDevfilesInGivenNamespace() throws Exception {
    assertTrue(userDevfileDaoDao.getByNamespace("non-existing-namespace", 30, 0).isEmpty());
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenGettingDevfilesByNullNamespace() throws Exception {
    userDevfileDaoDao.getByNamespace(null, 30, 0);
  }

  @Test(dataProvider = "validOrderFiled")
  public void shouldTestValidOrderFileds(String filed) {
    JpaUserDevfileDao.UserDevfileSearchQueryBuilder queryBuilder =
        new JpaUserDevfileDao.UserDevfileSearchQueryBuilder(null);
    try {
      queryBuilder.withOrder(ImmutableList.of(new Pair<>(filed, "blah")));
    } catch (IllegalArgumentException e) {
      Assert.fail(filed + " is valid but failed");
    }
  }

  @Test(
      dataProvider = "invalidOrderField",
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Order allowed only by \\[id, name\\] but got: .*")
  public void shouldTestInvalidOrderFileds(String filed) {
    JpaUserDevfileDao.UserDevfileSearchQueryBuilder queryBuilder =
        new JpaUserDevfileDao.UserDevfileSearchQueryBuilder(null);
    queryBuilder.withOrder(ImmutableList.of(new Pair<>(filed, "blah")));
  }

  @Test(dataProvider = "validSearchFiled")
  public void shouldTestValidSearchFileds(String filed) {
    JpaUserDevfileDao.UserDevfileSearchQueryBuilder queryBuilder =
        new JpaUserDevfileDao.UserDevfileSearchQueryBuilder(null);
    try {
      queryBuilder.withFilter(ImmutableList.of(new Pair<>(filed, "blah")));
    } catch (IllegalArgumentException e) {
      Assert.fail(filed + " is valid but failed");
    }
  }

  @Test(
      dataProvider = "invalidSearchField",
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Filtering allowed only by \\[name\\] but got: .*")
  public void shouldTestInvalidSearchFileds(String filed) {
    JpaUserDevfileDao.UserDevfileSearchQueryBuilder queryBuilder =
        new JpaUserDevfileDao.UserDevfileSearchQueryBuilder(null);
    queryBuilder.withFilter(ImmutableList.of(new Pair<>(filed, "blah")));
  }

  @DataProvider
  public Object[][] validOrderFiled() {
    return new Object[][] {{"id"}, {"Id"}, {"name"}, {"nAmE"}};
  }

  @DataProvider
  public Object[][] invalidOrderField() {
    return new Object[][] {{"devfile"}, {"meta_name"}, {"description"}, {"meta_generated_name"}};
  }

  @DataProvider
  public Object[][] validSearchFiled() {
    return new Object[][] {
      {"name"}, {"NaMe"},
    };
  }

  @DataProvider
  public Object[][] invalidSearchField() {
    return new Object[][] {
      {"id"}, {"devfile"}, {"ID"}, {"meta_name"}, {"description"}, {"meta_generated_name"}
    };
  }
}
