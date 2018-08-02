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
package org.eclipse.che.multiuser.organization.spi.tck;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.multiuser.organization.spi.MemberDao;
import org.eclipse.che.multiuser.organization.spi.impl.MemberImpl;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;
import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link MemberDao} contract.
 *
 * @author Sergii Leschenko
 */
@Listeners(TckListener.class)
@Test(suiteName = MemberDaoTest.SUITE_NAME)
public class MemberDaoTest {

  public static final String SUITE_NAME = "MemberDaoTck";

  private MemberImpl[] members;
  private OrganizationImpl[] orgs;
  private UserImpl[] users;

  @Mock private EventService eventService;

  @Inject private MemberDao memberDao;

  @Inject private TckRepository<MemberImpl> memberRepo;
  @Inject private TckRepository<UserImpl> userRepo;
  @Inject private TckRepository<OrganizationImpl> organizationRepo;

  @BeforeMethod
  private void setUp() throws TckRepositoryException {
    users = new UserImpl[3];
    users[0] = new UserImpl("user1-id", "user1@test.com", "user1-name");
    users[1] = new UserImpl("user2-id", "user2@test.com", "user2-name");
    users[2] = new UserImpl("user3-id", "user3@test.com", "user3-name");
    userRepo.createAll(asList(users));

    orgs = new OrganizationImpl[3];
    orgs[0] = new OrganizationImpl("org1-id", "org1-name", null);
    orgs[1] = new OrganizationImpl("org2-id", "org2-name", null);
    orgs[2] = new OrganizationImpl("org3-id", "org3-name", null);
    organizationRepo.createAll(asList(orgs));

    members = new MemberImpl[5];
    members[0] = new MemberImpl(users[0].getId(), orgs[0].getId(), asList("read", "update"));
    members[1] = new MemberImpl(users[1].getId(), orgs[0].getId(), asList("read", "update"));
    members[2] = new MemberImpl(users[2].getId(), orgs[0].getId(), asList("read", "update"));
    members[3] = new MemberImpl(users[1].getId(), orgs[1].getId(), asList("read", "update"));
    members[4] = new MemberImpl(users[1].getId(), orgs[2].getId(), asList("read", "update"));

    memberRepo.createAll(Stream.of(members).map(MemberImpl::new).collect(Collectors.toList()));
  }

  @AfterMethod
  private void cleanup() throws TckRepositoryException {
    memberRepo.removeAll();
    userRepo.removeAll();
    organizationRepo.removeAll();
  }

  @Test(dependsOnMethods = {"shouldGetMember", "shouldRemoveMember"})
  public void shouldCreateNewMemberOnMemberStoring() throws Exception {
    final MemberImpl member = members[0];
    memberDao.remove(member.getUserId(), member.getOrganizationId());

    memberDao.store(member);

    assertEquals(
        memberDao.getMember(member.getOrganizationId(), member.getUserId()),
        new MemberImpl(member));
  }

  @Test(dependsOnMethods = {"shouldGetMember"})
  public void shouldUpdateMemberOnMemberStoring() throws Exception {
    final MemberImpl member =
        new MemberImpl(
            members[0].getUserId(), members[0].getOrganizationId(), asList("read", "remove"));

    memberDao.store(member);

    assertEquals(member, memberDao.getMember(member.getOrganizationId(), member.getUserId()));
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldThrowServerExceptionOnStoringMemberForNonExistenceUser() throws Exception {
    final MemberImpl toCreate =
        new MemberImpl("non-existence", members[0].getOrganizationId(), singletonList("read"));

    memberDao.store(toCreate);
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldThrowServerExceptionOnStoringMemberForNonExistenceOrganization()
      throws Exception {
    final MemberImpl toCreate =
        new MemberImpl(members[0].getUserId(), "non-existence", singletonList("read"));

    memberDao.store(toCreate);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeOnStoringNullableMember() throws Exception {
    memberDao.store(null);
  }

  @Test(
    expectedExceptions = NotFoundException.class,
    dependsOnMethods = "shouldThrowNotFoundExceptionOnGettingNonExistingMember"
  )
  public void shouldRemoveMember() throws Exception {
    final MemberImpl member = members[0];

    memberDao.remove(member.getOrganizationId(), member.getUserId());

    memberDao.getMember(member.getUserId(), member.getOrganizationId());
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeOnRemovingMemberByNullUser() throws Exception {
    memberDao.remove("organization1234567", null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeOnRemovingMemberByNullOrganization() throws Exception {
    memberDao.remove(null, "user1234567");
  }

  @Test
  public void shouldNotThrowAnyExceptionOnRemovingNonExistingMember() throws Exception {
    memberDao.remove("organization12345", "user12345");
  }

  @Test
  public void shouldGetMember() throws Exception {
    final MemberImpl existedMember = members[0];

    final MemberImpl fetchedMember =
        memberDao.getMember(existedMember.getOrganizationId(), existedMember.getUserId());

    assertEquals(existedMember, fetchedMember);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionOnGettingNonExistingMember() throws Exception {
    memberDao.getMember("org12345678", "user12345678");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeOnGettingMemberByNullOrganization() throws Exception {
    memberDao.getMember(null, "user1234567");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeOnGettingMemberByNullUser() throws Exception {
    memberDao.getMember("organization12345", null);
  }

  @Test
  public void shouldGetMembersByOrganization() throws Exception {
    final Page<MemberImpl> membersPage = memberDao.getMembers(members[0].getOrganizationId(), 1, 1);
    final List<MemberImpl> fetchedMembers = membersPage.getItems();

    assertEquals(membersPage.getTotalItemsCount(), 3);
    assertEquals(membersPage.getItemsCount(), 1);
    assertTrue(
        fetchedMembers.contains(members[0])
            ^ fetchedMembers.contains(members[1])
            ^ fetchedMembers.contains(members[2]));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeOnGettingMembersByNullOrganization() throws Exception {
    memberDao.getMembers(null, 1, 0);
  }

  @Test
  public void shouldReturnEmptyListIfThereAreNotAnyMembersForSpecifiedOrganization()
      throws Exception {
    final Page<MemberImpl> fetchedMembers = memberDao.getMembers("organization1234567", 30, 0);

    assertTrue(fetchedMembers.isEmpty());
    assertEquals(fetchedMembers.getTotalItemsCount(), 0);
  }

  @Test
  public void shouldGetMembershipsByUser() throws Exception {
    final List<MemberImpl> fetchedMembers = memberDao.getMemberships(members[0].getUserId());

    assertEquals(fetchedMembers.size(), 1);
    assertEquals(fetchedMembers.get(0), members[0]);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeOnGettingMembershipsByNullUser() throws Exception {
    memberDao.getMemberships(null);
  }

  @Test
  public void shouldReturnEmptyListIfThereAreNotAnyMembershipsForSpecifiedUser() throws Exception {
    final List<MemberImpl> fetchedMembers = memberDao.getMemberships("user1234567");

    assertTrue(fetchedMembers.isEmpty());
  }

  @Test
  public void shouldGetOrganizationsByUser() throws Exception {
    final Page<OrganizationImpl> fetchedMembers =
        memberDao.getOrganizations(members[1].getUserId(), 1, 1);

    assertEquals(fetchedMembers.getItemsCount(), 1);
    assertEquals(fetchedMembers.getTotalItemsCount(), 3);
    assertTrue(
        fetchedMembers.getItems().contains(orgs[0])
            ^ fetchedMembers.getItems().contains(orgs[1])
            ^ fetchedMembers.getItems().contains(orgs[2]));
  }

  @Test
  public void shouldReturnEmptyListIfThereAreNotAnyOrganizationsForSpecifiedUser()
      throws Exception {
    final Page<OrganizationImpl> organizations = memberDao.getOrganizations("user1234567", 30, 0);

    assertTrue(organizations.isEmpty());
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeOnGettingOrganizationByNullUserId() throws Exception {
    memberDao.getOrganizations(null, 30, 0);
  }
}
