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
package org.eclipse.che.multiuser.api.permission.server;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain.SET_PERMISSIONS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
import org.eclipse.che.multiuser.api.permission.server.spi.PermissionsDao;
import org.eclipse.che.multiuser.api.permission.shared.dto.PermissionsDto;
import org.eclipse.che.multiuser.api.permission.shared.model.Permissions;
import org.eclipse.che.multiuser.api.permission.shared.model.PermissionsDomain;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link PermissionsManager}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class PermissionsManagerTest {

  @Mock private PermissionsDao<TestPermissionsImpl> permissionsDao;
  @Mock private EventService eventService;

  private PermissionsManager permissionsManager;

  @BeforeMethod
  public void setUp() throws Exception {
    when(permissionsDao.getDomain()).thenReturn(new TestDomain());

    permissionsManager = new PermissionsManager(eventService, ImmutableSet.of(permissionsDao));
  }

  @Test(
    expectedExceptions = ServerException.class,
    expectedExceptionsMessageRegExp =
        "Permissions Domain 'test' should be stored in only one storage. "
            + "Duplicated in class org.eclipse.che.multiuser.api.permission.server.spi.PermissionsDao.* and class org.eclipse.che.multiuser.api.permission.server.spi.PermissionsDao.*"
  )
  public void shouldThrowExceptionIfThereAreTwoStoragesWhichServeOneDomain() throws Exception {
    @SuppressWarnings("unchecked")
    final PermissionsDao anotherStorage = mock(PermissionsDao.class);
    when(anotherStorage.getDomain()).thenReturn(new TestDomain());

    permissionsManager =
        new PermissionsManager(eventService, ImmutableSet.of(permissionsDao, anotherStorage));
  }

  @Test
  public void shouldBeAbleToStorePermissions() throws Exception {
    final Permissions permissions =
        DtoFactory.newDto(PermissionsDto.class)
            .withUserId("user")
            .withDomainId("test")
            .withInstanceId("test123")
            .withActions(singletonList(SET_PERMISSIONS));
    when(permissionsDao.store(any(TestPermissionsImpl.class))).thenReturn(Optional.empty());

    permissionsManager.storePermission(permissions);

    verify(permissionsDao)
        .store(
            new TestDomain()
                .doCreateInstance(
                    permissions.getUserId(), permissions.getDomainId(), permissions.getActions()));
  }

  @Test(
    expectedExceptions = ConflictException.class,
    expectedExceptionsMessageRegExp =
        "Domain with id 'test' doesn't support following action\\(s\\): unsupported"
  )
  public void shouldNotStorePermissionsWhenItHasUnsupportedAction() throws Exception {
    final Permissions permissions =
        DtoFactory.newDto(PermissionsDto.class)
            .withUserId("user")
            .withDomainId("test")
            .withInstanceId("test123")
            .withActions(singletonList("unsupported"));
    permissionsManager.storePermission(permissions);
  }

  @Test(
    expectedExceptions = ConflictException.class,
    expectedExceptionsMessageRegExp =
        "Can't edit permissions because there is not any another user with permission 'setPermissions'"
  )
  public void shouldNotStorePermissionsWhenItRemoveLastSetPermissions() throws Exception {
    final TestPermissionsImpl foreignPermissions =
        new TestPermissionsImpl("user1", "test", "test123", singletonList("read"));
    final TestPermissionsImpl ownPermissions =
        new TestPermissionsImpl("user", "test", "test123", asList("read", "setPermissions"));

    when(permissionsDao.exists("user", "test123", SET_PERMISSIONS)).thenReturn(true);
    doReturn(new Page<>(singletonList(foreignPermissions), 0, 1, 2))
        .doReturn(new Page<>(singletonList(ownPermissions), 1, 1, 2))
        .when(permissionsDao)
        .getByInstance(nullable(String.class), nullable(Integer.class), nullable(Long.class));

    permissionsManager.storePermission(
        new TestPermissionsImpl("user", "test", "test123", singletonList("delete")));
  }

  @Test
  public void shouldStorePermissionsWhenItRemoveSetPermissionsButThereIsAnotherOne()
      throws Exception {
    final TestPermissionsImpl foreignPermissions =
        new TestPermissionsImpl("user1", "test", "test123", singletonList("setPermissions"));
    final TestPermissionsImpl ownPermissions =
        new TestPermissionsImpl("user", "test", "test123", asList("read", "setPermissions"));

    when(permissionsDao.exists("user", "test123", SET_PERMISSIONS)).thenReturn(true);
    when(permissionsDao.store(any(TestPermissionsImpl.class))).thenReturn(Optional.empty());
    doReturn(new Page<>(singletonList(ownPermissions), 0, 30, 31))
        .doReturn(new Page<>(singletonList(foreignPermissions), 1, 30, 31))
        .when(permissionsDao)
        .getByInstance(nullable(String.class), nullable(Integer.class), nullable(Long.class));

    permissionsManager.storePermission(
        new TestPermissionsImpl("user", "test", "test123", singletonList("delete")));

    verify(permissionsDao).getByInstance(eq("test123"), anyInt(), eq(0L));
    verify(permissionsDao).getByInstance(eq("test123"), anyInt(), eq(30L));
  }

  @Test
  public void shouldNotCheckExistingSetPermissionsIfUserDoesNotHaveItAtAllOnStoring()
      throws Exception {
    when(permissionsDao.exists("user", "test123", SET_PERMISSIONS)).thenReturn(false);
    when(permissionsDao.store(any(TestPermissionsImpl.class))).thenReturn(Optional.empty());

    permissionsManager.storePermission(
        new TestPermissionsImpl("user", "test", "test123", singletonList("delete")));

    verify(permissionsDao, never()).getByInstance(anyString(), anyInt(), anyInt());
  }

  @Test
  public void shouldBeAbleToDeletePermissions() throws Exception {
    permissionsManager.remove("user", "test", "test123");

    verify(permissionsDao).remove(eq("user"), eq("test123"));
  }

  @Test(
    expectedExceptions = ConflictException.class,
    expectedExceptionsMessageRegExp =
        "Can't remove permissions because there is not any another user with permission 'setPermissions'"
  )
  public void shouldNotRemovePermissionsWhenItContainsLastSetPermissionsAction() throws Exception {
    final TestPermissionsImpl firstPermissions =
        new TestPermissionsImpl("user1", "test", "test123", singletonList("read"));
    final TestPermissionsImpl secondPermissions =
        new TestPermissionsImpl("user", "test", "test123", asList("read", "setPermissions"));

    when(permissionsDao.exists("user", "test123", SET_PERMISSIONS)).thenReturn(true);
    doReturn(new Page<>(singletonList(firstPermissions), 0, 1, 2))
        .doReturn(new Page<>(singletonList(secondPermissions), 1, 1, 2))
        .when(permissionsDao)
        .getByInstance(nullable(String.class), nullable(Integer.class), nullable(Long.class));

    permissionsManager.remove("user", "test", "test123");
  }

  @Test
  public void shouldNotCheckExistingSetPermissionsIfUserDoesNotHaveItAtAllOnRemove()
      throws Exception {
    when(permissionsDao.exists("user", "test123", SET_PERMISSIONS)).thenReturn(false);

    permissionsManager.remove("user", "test", "test123");

    verify(permissionsDao, never()).getByInstance(eq("test123"), anyInt(), anyInt());
  }

  @Test
  public void shouldBeAbleToGetPermissionsByUserAndDomainAndInstance() throws Exception {
    final TestPermissionsImpl permissions =
        new TestPermissionsImpl("user", "test", "test123", singletonList("read"));
    when(permissionsDao.get("user", "test123")).thenReturn(permissions);

    final Permissions fetchedPermissions = permissionsManager.get("user", "test", "test123");

    assertEquals(permissions, fetchedPermissions);
  }

  @Test
  public void shouldBeAbleToGetPermissionsByInstance() throws Exception {
    final TestPermissionsImpl firstPermissions =
        new TestPermissionsImpl("user", "test", "test123", singletonList("read"));
    final TestPermissionsImpl secondPermissions =
        new TestPermissionsImpl("user1", "test", "test123", singletonList("read"));

    doReturn(new Page<>(asList(firstPermissions, secondPermissions), 1, 2, 4))
        .when(permissionsDao)
        .getByInstance(nullable(String.class), nullable(Integer.class), nullable(Long.class));

    final Page<AbstractPermissions> permissionsPage =
        permissionsManager.getByInstance("test", "test123", 2, 1);
    final List<AbstractPermissions> fetchedPermissions = permissionsPage.getItems();

    verify(permissionsDao).getByInstance("test123", 2, 1);
    assertEquals(permissionsPage.getTotalItemsCount(), 4);
    assertEquals(permissionsPage.getItemsCount(), 2);
    assertTrue(fetchedPermissions.contains(firstPermissions));
    assertTrue(fetchedPermissions.contains(secondPermissions));
  }

  @Test
  public void shouldBeAbleToCheckPermissionExistence() throws Exception {
    when(permissionsDao.exists("user", "test123", "use")).thenReturn(true);
    when(permissionsDao.exists("user", "test123", "update")).thenReturn(false);

    assertTrue(permissionsManager.exists("user", "test", "test123", "use"));
    assertFalse(permissionsManager.exists("user", "test", "test123", "update"));
  }

  @Test
  public void shouldBeAbleToDomains() throws Exception {
    final List<AbstractPermissionsDomain> domains = permissionsManager.getDomains();

    assertEquals(domains.size(), 1);
    assertTrue(domains.contains(new TestDomain()));
  }

  @Test
  public void shouldBeAbleToDomainActions() throws Exception {
    final PermissionsDomain testDomain = permissionsManager.getDomain("test");
    final List<String> allowedActions = testDomain.getAllowedActions();

    assertEquals(allowedActions.size(), 5);
    assertTrue(
        allowedActions.containsAll(
            ImmutableSet.of(SET_PERMISSIONS, "read", "write", "use", "delete")));
  }

  @Test(
    expectedExceptions = NotFoundException.class,
    expectedExceptionsMessageRegExp = "Requested unsupported domain 'unsupported'"
  )
  public void shouldThrowExceptionWhenRequestedUnsupportedDomain() throws Exception {
    permissionsManager.getDomain("unsupported");
  }

  @Test
  public void shouldDoNothingOnActionSupportingCheckingWhenListDoesNotContainUnsupportedAction()
      throws Exception {
    permissionsManager.checkActionsSupporting("test", Arrays.asList("write", "use"));
  }

  @Test(
    expectedExceptions = ConflictException.class,
    expectedExceptionsMessageRegExp =
        "Domain with id 'test' doesn't support following action\\(s\\): unsupported"
  )
  public void
      shouldThrowConflictExceptionOnActionSupportingCheckingWhenListContainsUnsupportedAction()
          throws Exception {
    permissionsManager.checkActionsSupporting("test", Arrays.asList("write", "use", "unsupported"));
  }

  public class TestDomain extends AbstractPermissionsDomain<TestPermissionsImpl> {

    public TestDomain() {
      super("test", asList("read", "write", "use", "delete"));
    }

    @Override
    protected TestPermissionsImpl doCreateInstance(
        String userId, String instanceId, List<String> allowedActions) {
      return new TestPermissionsImpl("user", "test", "test123", allowedActions);
    }
  }

  public class TestPermissionsImpl extends AbstractPermissions {

    private String domainId;

    private String instanceId;

    List<String> actions;

    @Override
    public String getInstanceId() {
      return instanceId;
    }

    @Override
    public String getDomainId() {
      return domainId;
    }

    public TestPermissionsImpl(
        String userId, String domainId, String instanceId, List<String> actions) {
      super(userId);
      this.domainId = domainId;
      this.instanceId = instanceId;
      this.actions = actions;
    }

    @Override
    public List<String> getActions() {
      return actions;
    }
  }
}
