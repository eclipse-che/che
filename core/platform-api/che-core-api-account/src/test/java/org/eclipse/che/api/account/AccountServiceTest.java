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
package org.eclipse.che.api.account;

import org.eclipse.che.api.account.server.AccountService;
import org.eclipse.che.api.account.server.Constants;
import org.eclipse.che.api.account.server.dao.Account;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Member;
import org.eclipse.che.api.account.shared.dto.AccountDescriptor;
import org.eclipse.che.api.account.shared.dto.AccountUpdate;
import org.eclipse.che.api.account.shared.dto.MemberDescriptor;
import org.eclipse.che.api.account.shared.dto.NewMembership;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.tools.DependencySupplierImpl;
import org.everrest.core.tools.ResourceLauncher;
import org.everrest.core.tools.SimplePrincipal;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.POST;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests for Account Service
 *
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 * @see org.eclipse.che.api.account.server.AccountService
 */
@Listeners(value = {MockitoTestNGListener.class})
public class AccountServiceTest {

    private final String BASE_URI     = "http://localhost/service";
    private final String SERVICE_PATH = BASE_URI + "/account";
    private final String USER_ID      = "user123abc456def";
    private final String ACCOUNT_ID   = "account0xffffffffff";
    private final String ACCOUNT_NAME = "codenvy";
    private final String USER_NAME    = "account";
    private final User   user         = new User().withId(USER_ID).withName(USER_NAME);

    @Mock
    private AccountDao accountDao;

    @Mock
    private UserDao userDao;

    @Mock
    private WorkspaceManager workspaceManager;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private EnvironmentContext environmentContext;

    private Account           account;
    private ArrayList<Member> memberships;

    protected ProviderBinder     providers;
    protected ResourceBinderImpl resources;
    protected ResourceLauncher   launcher;

    @BeforeMethod
    public void setUp() throws Exception {
        resources = new ResourceBinderImpl();
        providers = new ApplicationProviderBinder();
        DependencySupplierImpl dependencies = new DependencySupplierImpl();
        dependencies.addComponent(UserDao.class, userDao);
        dependencies.addComponent(AccountDao.class, accountDao);
        dependencies.addComponent(WorkspaceManager.class, workspaceManager);
        resources.addResource(AccountService.class, null);
        EverrestProcessor processor = new EverrestProcessor(resources, providers, dependencies, new EverrestConfiguration(), null);
        launcher = new ResourceLauncher(processor);
        ApplicationContextImpl.setCurrent(new ApplicationContextImpl(null, null, ProviderBinder.getInstance()));
        Map<String, String> attributes = new HashMap<>();
        attributes.put("secret", "bit secret");
        account = new Account(ACCOUNT_ID, ACCOUNT_NAME, null, attributes);

        memberships = new ArrayList<>(1);
        Member ownerMembership = new Member();
        ownerMembership.setAccountId(account.getId());
        ownerMembership.setUserId(USER_ID);
        ownerMembership.setRoles(Arrays.asList("account/owner"));
        memberships.add(ownerMembership);

        when(environmentContext.get(SecurityContext.class)).thenReturn(securityContext);
        when(securityContext.getUserPrincipal()).thenReturn(new SimplePrincipal(USER_NAME));

        org.eclipse.che.commons.env.EnvironmentContext.getCurrent().setUser(new org.eclipse.che.commons.user.User() {
            @Override
            public String getName() {
                return user.getName();
            }

            @Override
            public boolean isMemberOf(String role) {
                return false;
            }

            @Override
            public String getToken() {
                return "token";
            }

            @Override
            public String getId() {
                return user.getId();
            }

            @Override
            public boolean isTemporary() {
                return false;
            }
        });
    }

    @AfterMethod
    public void tearDown() throws Exception {
        org.eclipse.che.commons.env.EnvironmentContext.reset();
    }

    @Test
    public void shouldBeAbleToCreateAccount() throws Exception {
        when(userDao.getByName(USER_NAME)).thenReturn(user);
        when(accountDao.getByName(account.getName())).thenThrow(new NotFoundException("Account not found"));
        when(accountDao.getByOwner(USER_ID)).thenReturn(Collections.<Account>emptyList());
        String role = "user";
        prepareSecurityContext(role);

        ContainerResponse response = makeRequest(POST, SERVICE_PATH, MediaType.APPLICATION_JSON, account);

        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        AccountDescriptor created = (AccountDescriptor)response.getEntity();
        verifyLinksRel(created.getLinks(), generateRels(role));
        verify(accountDao).create(any(Account.class));
        Member expected = new Member().withAccountId(created.getId())
                                      .withUserId(USER_ID)
                                      .withRoles(Arrays.asList("account/owner"));
        verify(accountDao).addMember(expected);
    }

    @Test
    public void shouldNotBeAbleToCreateAccountWithNotValidAttributes() throws Exception {
        account.getAttributes().put("codenvy:god_mode", "true");

        ContainerResponse response = makeRequest(POST, SERVICE_PATH, MediaType.APPLICATION_JSON, account);
        assertEquals(response.getEntity().toString(), "Attribute name 'codenvy:god_mode' is not valid");
    }

    @Test
    public void shouldNotBeAbleToCreateAccountIfUserAlreadyHasOne() throws Exception {
        prepareSecurityContext("user");
        when(userDao.getByName(USER_NAME)).thenReturn(user);
        when(accountDao.getByOwner(USER_ID)).thenReturn(Arrays.asList(account));

        ContainerResponse response = makeRequest(POST, SERVICE_PATH, MediaType.APPLICATION_JSON, account);
        assertEquals(response.getEntity().toString(), "Account which owner is " + USER_ID + " already exists");
    }

    @Test
    public void shouldBeAbleToGetMemberships() throws Exception {
        when(userDao.getByName(USER_NAME)).thenReturn(user);
        when(accountDao.getByMember(USER_ID)).thenReturn(memberships);
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(account);

        ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH, null, null);

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        @SuppressWarnings("unchecked") List<MemberDescriptor> currentAccounts = (List<MemberDescriptor>)response.getEntity();
        assertEquals(currentAccounts.size(), 1);
        assertEquals(currentAccounts.get(0).getRoles().get(0), "account/owner");
        verify(accountDao).getByMember(USER_ID);
    }

    @Test
    public void shouldBeAbleToGetMembershipsOfSpecificUser() throws Exception {
        when(accountDao.getById("fake_id")).thenReturn(new Account("fake_id", "fake_name"));
        User user = new User().withId("ANOTHER_USER_ID").withEmail("ANOTHER_USER_EMAIL");
        ArrayList<Member> memberships = new ArrayList<>(1);
        Member am = new Member().withAccountId("fake_id")
                                .withUserId("ANOTHER_USER_ID")
                                .withRoles(Arrays.asList("account/member"));
        memberships.add(am);
        when(userDao.getById("ANOTHER_USER_ID")).thenReturn(user);
        when(accountDao.getByMember("ANOTHER_USER_ID")).thenReturn(memberships);

        ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "/memberships?userid=" + "ANOTHER_USER_ID", null, null);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        @SuppressWarnings("unchecked") List<MemberDescriptor> currentAccounts = (List<MemberDescriptor>)response.getEntity();
        assertEquals(currentAccounts.size(), 1);
        assertEquals(currentAccounts.get(0).getAccountReference().getId(), am.getAccountId());
        assertEquals(currentAccounts.get(0).getAccountReference().getName(), "fake_name");
        assertEquals(currentAccounts.get(0).getRoles(), am.getRoles());
    }

    @Test
    public void shouldBeAbleToGetAccountById() throws Exception {
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(account);
        String[] roles = getRoles(AccountService.class, "getById");

        for (String role : roles) {
            prepareSecurityContext(role);

            ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "/" + ACCOUNT_ID, null, null);

            assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
            AccountDescriptor actual = (AccountDescriptor)response.getEntity();
            verifyLinksRel(actual.getLinks(), generateRels(role));
        }
        verify(accountDao, times(roles.length)).getById(ACCOUNT_ID);
    }

    @Test
    public void shouldBeAbleToUpdateAccount() throws Exception {
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(account);
        AccountUpdate toUpdate = DtoFactory.getInstance().createDto(AccountUpdate.class)
                                           .withName("newName")
                                           .withAttributes(Collections.singletonMap("newAttribute", "someValue"));
        prepareSecurityContext("account/owner");

        ContainerResponse response = makeRequest(POST, SERVICE_PATH + "/" + ACCOUNT_ID, MediaType.APPLICATION_JSON, toUpdate);

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        AccountDescriptor actual = (AccountDescriptor)response.getEntity();
        assertEquals(actual.getAttributes().size(), 2);
        assertEquals(actual.getName(), "newName");
    }

    @Test
    public void shouldBeAbleToRewriteAttributesWhenUpdatingAccount() throws Exception {
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(account);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("newAttribute", "someValue");
        attributes.put("oldAttribute", "oldValue");
        account.setAttributes(attributes);

        Map<String, String> updates = new HashMap<>();
        updates.put("newAttribute", "OTHER_VALUE");
        updates.put("newAttribute2", "someValue2");
        AccountDescriptor toUpdate = DtoFactory.getInstance().createDto(AccountDescriptor.class).withAttributes(updates);

        prepareSecurityContext("account/owner");
        ContainerResponse response = makeRequest(POST, SERVICE_PATH + "/" + ACCOUNT_ID, MediaType.APPLICATION_JSON, toUpdate);

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        AccountDescriptor actual = (AccountDescriptor)response.getEntity();
        assertEquals(actual.getName(), ACCOUNT_NAME);
        assertEquals(actual.getAttributes().size(), 3);
        assertEquals(actual.getAttributes().get("newAttribute"), "OTHER_VALUE");
    }

    @Test
    public void shouldBeAbleToRemoveAttribute() throws Exception {
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(account);
        Map<String, String> attributes = new HashMap<>(1);
        attributes.put("test", "test");
        account.setAttributes(attributes);

        ContainerResponse response = makeRequest(HttpMethod.DELETE, SERVICE_PATH + "/" + ACCOUNT_ID + "/attribute?name=test", null, null);

        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
        assertNull(attributes.get("test"));
    }

    @Test
    public void shouldNotBeAbleToUpdateAccountWithAlreadyExistedName() throws Exception {
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(account);
        when(accountDao.getByName("TO_UPDATE")).thenReturn(new Account("id", "TO_UPDATE"));
        AccountDescriptor toUpdate = DtoFactory.getInstance().createDto(AccountDescriptor.class).withName("TO_UPDATE");
        prepareSecurityContext("account/owner");

        ContainerResponse response = makeRequest(POST, SERVICE_PATH + "/" + ACCOUNT_ID, MediaType.APPLICATION_JSON, toUpdate);
        assertNotEquals(response.getStatus(), Response.Status.OK);
        assertEquals(response.getEntity().toString(), "Account with name TO_UPDATE already exists");
    }

    @Test
    public void shouldBeAbleToGetAccountByName() throws Exception {
        when(accountDao.getByName(ACCOUNT_NAME)).thenReturn(account);
        String[] roles = getRoles(AccountService.class, "getByName");
        for (String role : roles) {
            prepareSecurityContext(role);

            ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "/find?name=" + ACCOUNT_NAME, null, null);

            assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
            AccountDescriptor actual = (AccountDescriptor)response.getEntity();
            verifyLinksRel(actual.getLinks(), generateRels(role));
        }
        verify(accountDao, times(roles.length)).getByName(ACCOUNT_NAME);
    }

    @DataProvider(name = "roleProvider")
    public Object[][] roleProvider() {
        return new String[][] {
                {"system/admin"},
                {"system/manager"},
                };
    }

    @Test
    public void shouldBeAbleToGetAccountMembers() throws Exception {
        when(accountDao.getById(account.getId())).thenReturn(account);
        when(accountDao.getMembers(account.getId()))
                .thenReturn(Arrays.asList(new Member().withRoles(Collections.<String>emptyList())
                                                      .withUserId(USER_ID)
                                                      .withAccountId(account.getId())));

        ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "/" + account.getId() + "/members", null, null);

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        verify(accountDao).getMembers(account.getId());
        @SuppressWarnings("unchecked") List<MemberDescriptor> members = (List<MemberDescriptor>)response.getEntity();
        assertEquals(members.size(), 1);
        MemberDescriptor member = members.get(0);
        assertEquals(member.getLinks().size(), 2);
    }

    @Test
    public void shouldBeAbleToAddMember() throws Exception {
        when(accountDao.getById(ACCOUNT_ID)).thenReturn(account);
        final NewMembership newMembership = DtoFactory.getInstance().createDto(NewMembership.class)
                                                      .withUserId(USER_ID)
                                                      .withRoles(singletonList("account/member"));

        final ContainerResponse response = makeRequest(POST,
                                                       SERVICE_PATH + "/" + account.getId() + "/members",
                                                       MediaType.APPLICATION_JSON,
                                                       newMembership);

        assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        final MemberDescriptor descriptor = (MemberDescriptor)response.getEntity();
        assertEquals(descriptor.getUserId(), newMembership.getUserId());
        assertEquals(descriptor.getAccountReference().getId(), ACCOUNT_ID);
        assertEquals(descriptor.getRoles(), newMembership.getRoles());
        verify(accountDao).addMember(any(Member.class));
    }

    @Test
    public void shouldBeAbleToRemoveMember() throws Exception {
        Member accountMember = new Member().withUserId(USER_ID)
                                           .withAccountId(ACCOUNT_ID)
                                           .withRoles(Arrays.asList("account/member"));
        Member accountOwner = new Member().withUserId("owner_holder")
                                          .withAccountId(ACCOUNT_ID)
                                          .withRoles(Arrays.asList("account/owner"));
        when(accountDao.getMembers(ACCOUNT_ID)).thenReturn(Arrays.asList(accountMember, accountOwner));

        ContainerResponse response = makeRequest(HttpMethod.DELETE, SERVICE_PATH + "/" + ACCOUNT_ID + "/members/" + USER_ID, null, null);

        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
        verify(accountDao).removeMember(accountMember);
    }

    @Test
    public void shouldNotBeAbleToRemoveLastAccountOwner() throws Exception {
        Member accountOwner = new Member().withUserId(USER_ID)
                                          .withAccountId(ACCOUNT_ID)
                                          .withRoles(Arrays.asList("account/owner"));
        Member accountMember = new Member().withUserId("member_holder")
                                           .withAccountId(ACCOUNT_ID)
                                           .withRoles(Arrays.asList("account/member"));
        when(accountDao.getMembers(ACCOUNT_ID)).thenReturn(Arrays.asList(accountOwner, accountMember));

        ContainerResponse response = makeRequest(HttpMethod.DELETE, SERVICE_PATH + "/" + ACCOUNT_ID + "/members/" + USER_ID, null, null);

        assertEquals(response.getEntity().toString(), "Account should have at least 1 owner");
    }

    @Test
    public void shouldBeAbleToRemoveAccountOwnerIfOtherOneExists() throws Exception {
        Member accountOwner = new Member().withUserId(USER_ID)
                                          .withAccountId(ACCOUNT_ID)
                                          .withRoles(Arrays.asList("account/owner"));
        Member accountOwner2 = new Member().withUserId("owner_holder")
                                           .withAccountId(ACCOUNT_ID)
                                           .withRoles(Arrays.asList("account/owner"));
        when(accountDao.getMembers(ACCOUNT_ID)).thenReturn(Arrays.asList(accountOwner, accountOwner2));

        ContainerResponse response = makeRequest(HttpMethod.DELETE, SERVICE_PATH + "/" + ACCOUNT_ID + "/members/" + USER_ID, null, null);

        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
        verify(accountDao).removeMember(accountOwner);
    }

    @Test
    public void workspaceShouldBeRegistered() throws Exception {
        WorkspaceImpl workspace = spy(createUsersWorkspace());
        Account account = new Account("account123");
        when(workspace.getId()).thenReturn("workspace123");
        when(workspaceManager.getWorkspace(any())).thenReturn(workspace);
        when(accountDao.getById(account.getId())).thenReturn(account);
        when(accountDao.getByWorkspace(workspace.getId())).thenThrow(new NotFoundException(""));

        ContainerResponse response = makeRequest(POST, SERVICE_PATH + '/' + account.getId() + '/' + workspace.getId(), null, null);

        assertEquals(response.getStatus(), 200);
        AccountDescriptor descriptor = (AccountDescriptor)response.getEntity();
        assertEquals(descriptor.getWorkspaces().size(), 1);
        assertEquals(descriptor.getWorkspaces().get(0).getId(), workspace.getId());
        verify(accountDao).update(account);
    }

    @Test
    public void shouldFailWorkspaceRegistrationWhenWorkspaceIsAlreadyRegistered() throws Exception {
        WorkspaceImpl workspace = mock(WorkspaceImpl.class);
        Account account = new Account("account123");
        when(workspace.getId()).thenReturn("workspace123");
        when(workspaceManager.getWorkspace(any())).thenReturn(workspace);
        when(accountDao.getById(account.getId())).thenReturn(account);
        when(accountDao.isWorkspaceRegistered(workspace.getId())).thenReturn(true);

        ContainerResponse response = makeRequest(POST, SERVICE_PATH + '/' + account.getId() + '/' + workspace.getId(), null, null);

        assertEquals(response.getEntity().toString(), "Workspace 'workspace123' already registered in another account");
    }

    @Test
    public void shouldFailWorkspaceRegistrationWhenAccountAlreadyContainsGivenWorkspace() throws Exception {
        WorkspaceImpl workspace = mock(WorkspaceImpl.class);
        Account account = new Account("account123");
        account.setWorkspaces(singletonList(workspace));
        when(workspace.getId()).thenReturn("workspace123");
        when(workspaceManager.getWorkspace(any())).thenReturn(workspace);
        when(accountDao.getById(account.getId())).thenReturn(account);

        ContainerResponse response = makeRequest(POST, SERVICE_PATH + '/' + account.getId() + '/' + workspace.getId(), null, null);

        assertEquals(response.getEntity().toString(), "Workspace 'workspace123' is already registered in this account");
    }

    @Test
    public void workspaceShouldBeUnregistered() throws Exception {
        WorkspaceImpl workspace = mock(WorkspaceImpl.class);
        Account account = new Account("account123");
        account.setWorkspaces(new ArrayList<>(singletonList(workspace)));
        when(workspace.getId()).thenReturn("workspace123");
        when(workspaceManager.getWorkspace(any())).thenReturn(workspace);
        when(accountDao.getById(account.getId())).thenReturn(account);

        ContainerResponse response = makeRequest(DELETE, SERVICE_PATH + '/' + account.getId() + '/' + workspace.getId(), null, null);

        assertEquals(response.getStatus(), 200);
        AccountDescriptor descriptor = (AccountDescriptor)response.getEntity();
        assertTrue(descriptor.getWorkspaces().isEmpty());
        verify(accountDao).update(account);
    }

    @Test
    public void shouldFailWorkspaceUnRegistrationWhenWorkspaceIsNotRegistered() throws Exception {
        WorkspaceImpl workspace = mock(WorkspaceImpl.class);
        Account account = new Account("account123");
        when(workspace.getId()).thenReturn("workspace123");
        when(workspaceManager.getWorkspace(any())).thenReturn(workspace);
        when(accountDao.getById(account.getId())).thenReturn(account);

        ContainerResponse response = makeRequest(DELETE, SERVICE_PATH + '/' + account.getId() + '/' + workspace.getId(), null, null);

        assertEquals(response.getEntity().toString(), "Workspace 'workspace123' is not registered in account 'account123'");
    }

    protected void verifyLinksRel(List<Link> links, List<String> rels) {
        assertEquals(links.size(), rels.size());
        for (String rel : rels) {
            boolean linkPresent = false;
            int i = 0;
            for (; i < links.size() && !linkPresent; i++) {
                linkPresent = links.get(i).getRel().equals(rel);
            }
            if (!linkPresent) {
                fail(String.format("Given links do not contain link with rel = %s", rel));
            }
        }
    }

    private String[] getRoles(Class<? extends Service> clazz, String methodName) {
        for (Method one : clazz.getMethods()) {
            if (one.getName().equals(methodName)) {
                if (one.isAnnotationPresent(RolesAllowed.class)) {
                    return one.getAnnotation(RolesAllowed.class).value();
                } else {
                    return new String[0];
                }
            }
        }
        throw new IllegalArgumentException(String.format("Class %s does not have method with name %s", clazz.getName(), methodName));
    }

    private List<String> generateRels(String role) {
        final List<String> rels = new LinkedList<>();
        rels.add(Constants.LINK_REL_GET_MEMBERS);
        rels.add(Constants.LINK_REL_GET_ACCOUNTS);
        rels.add(Constants.LINK_REL_GET_ACCOUNT_BY_ID);
        switch (role) {
            case "system/admin":
                rels.add(Constants.LINK_REL_REMOVE_ACCOUNT);
            case "system/manager":
                rels.add(Constants.LINK_REL_GET_ACCOUNT_BY_NAME);
                break;
        }
        return rels;
    }

    protected ContainerResponse makeRequest(String method, String path, String contentType, Object toSend) throws Exception {
        Map<String, List<String>> headers = null;
        if (contentType != null) {
            headers = new HashMap<>();
            headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(contentType));
        }
        byte[] data = null;
        if (toSend != null) {
            data = JsonHelper.toJson(toSend).getBytes();
        }
        return launcher.service(method, path, BASE_URI, headers, data, null, environmentContext);
    }

    protected void prepareSecurityContext(String role) {
        when(securityContext.isUserInRole(anyString())).thenReturn(false);
        if (!role.equals("system/admin") && !role.equals("system/manager")) {
            when(securityContext.isUserInRole("user")).thenReturn(true);
        }
        when(securityContext.isUserInRole(role)).thenReturn(true);
    }

    private WorkspaceImpl createUsersWorkspace() {
        final EnvironmentImpl environment = new EnvironmentImpl("name",
                                                                new RecipeImpl(),
                                                                singletonList(new MachineConfigImpl(true,
                                                                                                    "name",
                                                                                                    "type",
                                                                                                    new MachineSourceImpl("type",
                                                                                                                          "location"),
                                                                                                    null,
                                                                                                    null,
                                                                                                    null)));
        return new WorkspaceImpl("id123", "owner1234", new WorkspaceConfigImpl("name",
                                                                               "desc",
                                                                               "defEnv",
                                                                               null,
                                                                               null,
                                                                               singletonList(environment)));
    }
}
