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
package org.eclipse.che.multiuser.organization.api;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;
import org.eclipse.che.account.event.BeforeAccountRemovedEvent;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.organization.api.event.BeforeOrganizationRemovedEvent;
import org.eclipse.che.multiuser.organization.api.event.OrganizationPersistedEvent;
import org.eclipse.che.multiuser.organization.api.permissions.OrganizationDomain;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDto;
import org.eclipse.che.multiuser.organization.shared.model.Member;
import org.eclipse.che.multiuser.organization.shared.model.Organization;
import org.eclipse.che.multiuser.organization.spi.MemberDao;
import org.eclipse.che.multiuser.organization.spi.OrganizationDao;
import org.eclipse.che.multiuser.organization.spi.impl.MemberImpl;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.eclipse.che.multiuser.organization.api.OrganizationManager}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class OrganizationManagerTest {
  @Captor private ArgumentCaptor<OrganizationImpl> organizationCaptor;
  @Captor private ArgumentCaptor<OrganizationPersistedEvent> persistEventCaptor;

  private static final String USER_NAME = "user-name";
  private static final String USER_ID = "user-id";

  @Mock private OrganizationDao organizationDao;

  @Mock private MemberDao memberDao;

  @Mock private EventService eventService;

  private OrganizationManager manager;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    manager =
        spy(
            new OrganizationManager(
                eventService, organizationDao, memberDao, new String[] {"reserved"}));

    when(eventService.publish(any())).thenAnswer(invocation -> invocation.getArguments()[0]);
    EnvironmentContext.getCurrent()
        .setSubject(new SubjectImpl(USER_NAME, USER_ID, "userToken", false));
  }

  @AfterMethod
  public void tearDown() throws Exception {
    EnvironmentContext.reset();
  }

  @Test
  public void shouldCreateOrganization() throws Exception {
    final Organization toCreate = DtoFactory.newDto(OrganizationDto.class).withName("newOrg");

    manager.create(toCreate);

    verify(organizationDao).create(organizationCaptor.capture());
    final OrganizationImpl createdOrganization = organizationCaptor.getValue();
    assertEquals(createdOrganization.getName(), toCreate.getName());
    assertEquals(createdOrganization.getQualifiedName(), toCreate.getName());
    assertEquals(createdOrganization.getParent(), toCreate.getParent());
    verify(eventService).publish(persistEventCaptor.capture());
    assertEquals(persistEventCaptor.getValue().getOrganization(), createdOrganization);
    verify(memberDao)
        .store(
            new MemberImpl(USER_ID, createdOrganization.getId(), OrganizationDomain.getActions()));
  }

  @Test
  public void shouldCreateSuborganization() throws Exception {
    final OrganizationImpl parentOrganization = new OrganizationImpl("org123", "parentOrg", null);
    when(organizationDao.getById(anyString())).thenReturn(parentOrganization);
    final Organization toCreate = new OrganizationImpl(null, "orgName", parentOrganization.getId());

    manager.create(toCreate);

    verify(organizationDao).create(organizationCaptor.capture());
    final OrganizationImpl createdOrganization = organizationCaptor.getValue();
    assertEquals(createdOrganization.getName(), toCreate.getName());
    assertEquals(
        createdOrganization.getQualifiedName(),
        parentOrganization.getQualifiedName() + "/" + toCreate.getName());
    assertEquals(createdOrganization.getParent(), toCreate.getParent());
    verify(eventService).publish(persistEventCaptor.capture());
    assertEquals(persistEventCaptor.getValue().getOrganization(), createdOrganization);
    verify(memberDao)
        .store(
            new MemberImpl(USER_ID, createdOrganization.getId(), OrganizationDomain.getActions()));
  }

  @Test
  public void shouldGenerateIdentifierWhenCreatingOrganization() throws Exception {
    final Organization organization =
        DtoFactory.newDto(OrganizationDto.class).withName("newOrg").withId("identifier");

    manager.create(organization);

    verify(organizationDao).create(organizationCaptor.capture());
    final String id = organizationCaptor.getValue().getId();
    assertNotNull(id);
    assertNotEquals(id, "identifier");
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionOnCreationIfOrganizationNameIsReserved()
      throws Exception {
    final Organization organization =
        DtoFactory.newDto(OrganizationDto.class).withName("reserved").withParent(null);

    manager.create(organization);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenCreatingNullableOrganization() throws Exception {
    manager.create(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenUpdatingOrganizationWithNullEntity() throws Exception {
    manager.update("organizationId", null);
  }

  @Test
  public void shouldUpdateOrganizationAndIgnoreNewIdAndParentFields() throws Exception {
    final OrganizationImpl existing = new OrganizationImpl("org123", "oldName", "parent123");
    final OrganizationImpl expectedExistingToUpdate = new OrganizationImpl(existing);
    expectedExistingToUpdate.setQualifiedName("newName");

    final OrganizationImpl suborganization =
        new OrganizationImpl("org321", "oldName/suborgName", "org123");
    final OrganizationImpl expectedSuborganizationToUpdate = new OrganizationImpl(suborganization);
    expectedSuborganizationToUpdate.setQualifiedName(
        expectedExistingToUpdate.getQualifiedName() + "/" + suborganization.getName());

    when(organizationDao.getById(any())).thenReturn(existing);
    doReturn(new Page<>(singletonList(suborganization), 0, 1, 1))
        .when(organizationDao)
        .getSuborganizations(anyString(), anyInt(), anyLong());
    final OrganizationImpl update = new OrganizationImpl("newId", "newName", "newParentId");

    final Organization updated = manager.update("organizationId", update);

    verify(organizationDao).getById("organizationId");
    verify(organizationDao, times(2)).update(organizationCaptor.capture());
    List<OrganizationImpl> updatedOrganizations = organizationCaptor.getAllValues();
    assertEquals(updatedOrganizations.get(0), expectedExistingToUpdate);
    assertEquals(updatedOrganizations.get(1), expectedSuborganizationToUpdate);
    verify(organizationDao).getSuborganizations(eq("oldName"), anyInt(), anyLong());
    assertEquals(updated, expectedExistingToUpdate);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionOnUpdatingIfOrganizationNameIsReserved()
      throws Exception {
    when(organizationDao.getById("id")).thenReturn(new OrganizationImpl("id", "oldName", null));

    manager.update("id", new OrganizationImpl("id", "reserved", null));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenUpdatingOrganizationByNullId() throws Exception {
    manager.update(null, new OrganizationImpl());
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenRemovingOrganizationByNullId() throws Exception {
    manager.remove(null);
  }

  @Test
  public void shouldRemoveOrganization() throws Exception {
    doNothing().when(manager).removeSuborganizations(anyString());
    final List<Member> members = Collections.singletonList(mock(Member.class));
    doReturn(members).when(manager).removeMembers(anyString());
    OrganizationImpl toRemove = new OrganizationImpl("org123", "toRemove", null);
    when(organizationDao.getById(anyString())).thenReturn(toRemove);
    BeforeAccountRemovedEvent beforeAccountRemovedEvent = mock(BeforeAccountRemovedEvent.class);
    BeforeOrganizationRemovedEvent beforeOrganizationRemovedEvent =
        mock(BeforeOrganizationRemovedEvent.class);
    doReturn(beforeAccountRemovedEvent)
        .doReturn(beforeOrganizationRemovedEvent)
        .when(eventService)
        .publish(any());

    manager.remove(toRemove.getId());

    verify(organizationDao).remove(toRemove.getId());
    verify(manager).removeMembers(eq(toRemove.getId()));
    verify(manager).removeSuborganizations(eq(toRemove.getId()));
    verify(eventService, times(3)).publish(anyObject());
    verify(beforeAccountRemovedEvent).propagateException();
    verify(beforeOrganizationRemovedEvent).propagateException();
  }

  @Test
  public void shouldRemoveMembersByOrganizationId() throws Exception {
    MemberImpl member1 = new MemberImpl("user1", "org1", singletonList("read"));
    MemberImpl member2 = new MemberImpl("user2", "org1", singletonList("read"));
    doReturn(new Page<>(singletonList(member1), 0, 1, 2))
        .doReturn(new Page<>(singletonList(member2), 1, 1, 2))
        .when(memberDao)
        .getMembers(anyString(), anyInt(), anyLong());

    manager.removeMembers("org1");

    verify(memberDao, times(2)).getMembers("org1", 100, 0);
    verify(memberDao).remove("user1", "org1");
    verify(memberDao).remove("user2", "org1");
  }

  @Test
  public void shouldRemoveSuborganizationsByParentOrganizationId() throws Exception {
    doNothing().when(manager).remove(any());
    OrganizationImpl subOrg1 = new OrganizationImpl("subOrg1", "subOrg1", "org1");
    OrganizationImpl subOrg2 = new OrganizationImpl("subOrg2", "subOrg2", "org1");
    doReturn(new Page<>(singletonList(subOrg1), 0, 1, 2))
        .doReturn(new Page<>(singletonList(subOrg2), 1, 1, 2))
        .when(organizationDao)
        .getByParent(anyString(), anyInt(), anyLong());

    manager.removeSuborganizations("org1");

    verify(organizationDao, times(2)).getByParent("org1", 100, 0);
    verify(manager).remove("subOrg1");
    verify(manager).remove("subOrg2");
  }

  @Test
  public void shouldNotTryToRemoveOrganizationWhenItIsNotExistRemoveOrganization()
      throws Exception {
    when(organizationDao.getById(anyString())).thenThrow(new NotFoundException("not found"));

    manager.remove("id");

    verify(organizationDao, never()).remove(anyString());
    verify(eventService, never()).publish(any());
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenGettingOrganizationByNullName() throws Exception {
    manager.getById(null);
  }

  @Test
  public void shouldGetOrganizationByName() throws Exception {
    final OrganizationImpl toFetch = new OrganizationImpl("org123", "toFetchOrg", "org321");
    when(organizationDao.getByName(eq("org123"))).thenReturn(toFetch);

    final Organization fetched = manager.getByName("org123");

    assertEquals(fetched, toFetch);
    verify(organizationDao).getByName("org123");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenGettingOrganizationByNullId() throws Exception {
    manager.getById(null);
  }

  @Test
  public void shouldGetOrganizationById() throws Exception {
    final OrganizationImpl toFetch = new OrganizationImpl("org123", "toFetchOrg", "org321");
    when(organizationDao.getById(eq("org123"))).thenReturn(toFetch);

    final Organization fetched = manager.getById("org123");

    assertEquals(fetched, toFetch);
    verify(organizationDao).getById("org123");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenGettingSuborganizationsByNullParent() throws Exception {
    manager.getByParent(null, 30, 0);
  }

  @Test
  public void shouldGetOrganizationsByParent() throws Exception {
    final OrganizationImpl toFetch = new OrganizationImpl("org321", "toFetchOrg", "org123");
    when(organizationDao.getByParent(eq("org123"), anyInt(), anyLong()))
        .thenReturn(new Page<>(singletonList(toFetch), 0, 1, 1));

    final Page<? extends Organization> organizations = manager.getByParent("org123", 30, 0);

    assertEquals(organizations.getItemsCount(), 1);
    assertEquals(organizations.getItems().get(0), toFetch);
    verify(organizationDao).getByParent("org123", 30, 0);
  }

  @Test
  public void shouldGetSuborganizations() throws Exception {
    final OrganizationImpl toFetch = new OrganizationImpl("org321", "parent/toFetchOrg", "org123");
    when(organizationDao.getSuborganizations(eq("parent"), anyInt(), anyLong()))
        .thenReturn(new Page<>(singletonList(toFetch), 0, 1, 1));

    final Page<? extends Organization> organizations = manager.getSuborganizations("parent", 30, 0);

    assertEquals(organizations.getItemsCount(), 1);
    assertEquals(organizations.getItems().get(0), toFetch);
    verify(organizationDao).getSuborganizations("parent", 30, 0);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeOnGettingSuborganizationsByNullParentQualifiedName() throws Exception {
    manager.getSuborganizations(null, 30, 0);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenGettingOrganizationsByNullUserId() throws Exception {
    manager.getByMember(null, 30, 0);
  }

  @Test
  public void shouldGetOrganizationsByMember() throws Exception {
    final OrganizationImpl toFetch = new OrganizationImpl("org123", "toFetchOrg", "org321");
    when(memberDao.getOrganizations(eq("org123"), anyInt(), anyLong()))
        .thenReturn(new Page<>(singletonList(toFetch), 0, 1, 1));

    final Page<? extends Organization> organizations = manager.getByMember("org123", 30, 0);

    assertEquals(organizations.getItemsCount(), 1);
    assertEquals(organizations.getItems().get(0), toFetch);
    verify(memberDao).getOrganizations("org123", 30, 0);
  }
}
