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
package org.eclipse.che.api.account.server;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.eclipse.che.api.account.server.dao.Account;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Member;
import org.eclipse.che.api.account.shared.dto.AccountDescriptor;
import org.eclipse.che.api.account.shared.dto.AccountReference;
import org.eclipse.che.api.account.shared.dto.AccountUpdate;
import org.eclipse.che.api.account.shared.dto.MemberDescriptor;
import org.eclipse.che.api.account.shared.dto.NewAccount;
import org.eclipse.che.api.account.shared.dto.NewMembership;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.core.rest.annotations.Required;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.util.LinksHelper;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.UsersWorkspaceImpl;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.dto.server.DtoFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Account API
 *
 * @author Eugene Voevodin
 * @author Alex Garagatyi
 */
@Api(value = "/account",
     description = "Account manager")
@Path("/account")
public class AccountService extends Service {
    private static final Logger LOG = LoggerFactory.getLogger(AccountService.class);

    @Context
    private SecurityContext securityContext;

    private final AccountDao       accountDao;
    private final UserDao          userDao;
    private final WorkspaceManager workspaceManager;

    @Inject
    public AccountService(AccountDao accountDao, UserDao userDao, WorkspaceManager workspaceManager) {
        this.accountDao = accountDao;
        this.userDao = userDao;
        this.workspaceManager = workspaceManager;
    }

    /**
     * Creates new account and adds current user as member to created account
     * with role <i>"account/owner"</i>. Returns status <b>201 CREATED</b>
     * and {@link AccountDescriptor} of created account if account has been created successfully.
     * Each new account should contain at least name.
     *
     * @param newAccount
     *         new account
     * @return descriptor of created account
     * @throws NotFoundException
     *         when some error occurred while retrieving account
     * @throws ConflictException
     *         when new account is {@code null}
     *         or new account name is {@code null}
     *         or when any of new account attributes is not valid
     * @throws ServerException
     * @see AccountDescriptor
     * @see #getById(String, SecurityContext)
     */
    @ApiOperation(value = "Create a new account",
                  notes = "Create a new account",
                  response = Account.class,
                  position = 1)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "CREATED"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 409, message = "Conflict Error"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @POST
    @GenerateLink(rel = Constants.LINK_REL_CREATE_ACCOUNT)
    @RolesAllowed({"user", "system/admin"})
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response create(@Context SecurityContext securityContext,
                           @Required NewAccount newAccount) throws NotFoundException,
                                                                   ConflictException,
                                                                   ServerException {
        requiredNotNull(newAccount, "New account");
        requiredNotNull(newAccount.getName(), "Account name");
        if (newAccount.getAttributes() != null) {
            for (String attributeName : newAccount.getAttributes().keySet()) {
                validateAttributeName(attributeName);
            }
        }
        User current = null;
        if (securityContext.isUserInRole("user")) {
            current = userDao.getByName(securityContext.getUserPrincipal().getName());
            //for now account <-One to One-> user
            if (accountDao.getByOwner(current.getId()).size() != 0) {
                throw new ConflictException(format("Account which owner is %s already exists", current.getId()));
            }
        }

        try {
            accountDao.getByName(newAccount.getName());
            throw new ConflictException(format("Account with name %s already exists", newAccount.getName()));
        } catch (NotFoundException ignored) {
        }
        final String accountId = NameGenerator.generate(Account.class.getSimpleName().toLowerCase(), Constants.ID_LENGTH);
        final Account account = new Account(accountId, newAccount.getName(), null, newAccount.getAttributes());

        accountDao.create(account);
        if (current != null) {
            final Member owner = new Member().withAccountId(accountId)
                                             .withUserId(current.getId())
                                             .withRoles(Arrays.asList("account/owner"));
            accountDao.addMember(owner);
            LOG.info("EVENT#account-add-member# ACCOUNT-ID#{}# USER-ID#{}# ROLES#{}#",
                     accountId,
                     current.getId(),
                     Arrays.asList("account/owner").toString());
        }
        return Response.status(Response.Status.CREATED)
                       .entity(toDescriptor(account))
                       .build();
    }

    /**
     * Returns all accounts memberships for current user.
     *
     * @return accounts memberships of current user
     * @throws NotFoundException
     *         when any of memberships contains account that doesn't exist
     * @throws ServerException
     *         when some error occurred while retrieving accounts or memberships
     * @see MemberDescriptor
     */
    @ApiOperation(value = "Get current user memberships",
                  notes = "This API call returns a JSON with all user membership in a single or multiple accounts",
                  response = MemberDescriptor.class,
                  responseContainer = "List",
                  position = 2)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @GenerateLink(rel = Constants.LINK_REL_GET_ACCOUNTS)
    @RolesAllowed("user")
    @Produces(APPLICATION_JSON)
    public List<MemberDescriptor> getMemberships(@Context SecurityContext securityContext) throws NotFoundException, ServerException {
        final Principal principal = securityContext.getUserPrincipal();
        final User current = userDao.getByName(principal.getName());
        final List<Member> memberships = accountDao.getByMember(current.getId());
        final List<MemberDescriptor> result = new ArrayList<>(memberships.size());
        for (Member membership : memberships) {
            result.add(toDescriptor(membership, accountDao.getById(membership.getAccountId()), securityContext));
        }
        return result;
    }

    /**
     * Returns all accounts memberships for user with given identifier.
     *
     * @param userId
     *         user identifier to search memberships
     * @return accounts memberships
     * @throws ConflictException
     *         when user identifier is {@code null}
     * @throws NotFoundException
     *         when user with given identifier doesn't exist
     * @throws ServerException
     *         when some error occurred while retrieving user or memberships
     * @see MemberDescriptor
     */
    @ApiOperation(value = "Get memberships of a specific user",
                  notes = "ID of a user should be specified as a query parameter. JSON with membership details is returned. For this API call system/admin or system/manager role is required",
                  response = MemberDescriptor.class,
                  responseContainer = "List",
                  position = 3)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 409, message = "No User ID specified"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/memberships")
    @GenerateLink(rel = Constants.LINK_REL_GET_ACCOUNTS)
    @RolesAllowed({"system/admin", "system/manager"})
    @Produces(APPLICATION_JSON)
    public List<MemberDescriptor> getMembershipsOfSpecificUser(@ApiParam(value = "User ID", required = true)
                                                               @Required @QueryParam("userid") String userId,
                                                               @Context SecurityContext securityContext) throws NotFoundException,
                                                                                                                ServerException,
                                                                                                                ConflictException {
        requiredNotNull(userId, "User identifier");
        final User user = userDao.getById(userId);
        final List<Member> memberships = accountDao.getByMember(user.getId());
        final List<MemberDescriptor> result = new ArrayList<>(memberships.size());
        for (Member membership : memberships) {
            result.add(toDescriptor(membership, accountDao.getById(membership.getAccountId()), securityContext));
        }
        return result;
    }

    /**
     * Removes attribute with given name from certain account.
     *
     * @param accountId
     *         account identifier
     * @param attributeName
     *         attribute name to remove attribute
     * @throws ConflictException
     *         if attribute name is not valid
     * @throws NotFoundException
     *         if account with given identifier doesn't exist
     * @throws ServerException
     *         when some error occurred while getting/updating account
     */
    @ApiOperation(value = "Delete account attribute",
                  notes = "Remove attribute from an account. Attribute name is used as a quary parameter. For this API request account/owner, system/admin or system/manager role is required",
                  position = 4)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "OK"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 409, message = "Invalid attribute name"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @DELETE
    @Path("/{id}/attribute")
    @RolesAllowed({"account/owner", "system/admin", "system/manager"})
    public void removeAttribute(@ApiParam(value = "Account ID", required = true)
                                @PathParam("id") String accountId,
                                @ApiParam(value = "Attribute name to be removed", required = true)
                                @QueryParam("name") String attributeName) throws ConflictException, NotFoundException, ServerException {
        validateAttributeName(attributeName);
        final Account account = accountDao.getById(accountId);
        account.getAttributes().remove(attributeName);
        accountDao.update(account);
    }

    /**
     * Searches for account with given identifier and returns {@link AccountDescriptor} for it.
     *
     * @param id
     *         account identifier
     * @return descriptor of found account
     * @throws NotFoundException
     *         when account with given identifier doesn't exist
     * @throws ServerException
     *         when some error occurred while retrieving account
     * @see AccountDescriptor
     */
    @ApiOperation(value = "Get account by ID",
                  notes = "Get account information by its ID. JSON with account details is returned. This API call requires account/owner, system/admin or system/manager role.",
                  response = AccountDescriptor.class,
                  position = 5)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/{id}")
    @RolesAllowed({"account/owner", "system/admin", "system/manager"})
    @Produces(APPLICATION_JSON)
    public AccountDescriptor getById(@ApiParam(value = "Account ID", required = true)
                                     @PathParam("id") String id,
                                     @Context SecurityContext securityContext) throws NotFoundException, ServerException {
        final Account account = accountDao.getById(id);
        return toDescriptor(account);
    }

    /**
     * Searches for account with given name and returns {@link AccountDescriptor} for it.
     *
     * @param name
     *         account name
     * @return descriptor of found account
     * @throws NotFoundException
     *         when account with given name doesn't exist
     * @throws ConflictException
     *         when account name is {@code null}
     * @throws ServerException
     *         when some error occurred while retrieving account
     * @see AccountDescriptor
     * @see #getById(String, SecurityContext)
     */
    @ApiOperation(value = "Get account by name",
                  notes = "Get account information by its name. JSON with account details is returned. This API call requires system/admin or system/manager role.",
                  response = AccountDescriptor.class,
                  position = 5)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 409, message = "No account name specified"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/find")
    @GenerateLink(rel = Constants.LINK_REL_GET_ACCOUNT_BY_NAME)
    @RolesAllowed({"system/admin", "system/manager"})
    @Produces(APPLICATION_JSON)
    public AccountDescriptor getByName(@ApiParam(value = "Account name", required = true)
                                       @Required @QueryParam("name") String name) throws NotFoundException,
                                                                                         ServerException,
                                                                                         ConflictException {
        requiredNotNull(name, "Account name");
        final Account account = accountDao.getByName(name);
        return toDescriptor(account);
    }

    /**
     * Creates new account member with role <i>"account/member"</i>.
     *
     * @param accountId
     *         account identifier
     * @param membership
     *         new membership
     * @return descriptor of created member
     * @throws ConflictException
     *         when user identifier is {@code null}
     * @throws NotFoundException
     *         when user or account with given identifier doesn't exist
     * @throws ServerException
     *         when some error occurred while getting user or adding new account member
     * @see MemberDescriptor
     * @see #removeMember(String, String)
     * @see #getMembers(String, SecurityContext)
     */
    @ApiOperation(value = "Add a new member to account",
                  notes = "Add a new user to an account. This user will have account/member role. This API call requires account/owner, system/admin or system/manager role.",
                  response = MemberDescriptor.class,
                  position = 6)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "OK"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 409, message = "No user ID specified"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @POST
    @Path("/{id}/members")
    @RolesAllowed({"account/owner", "system/admin"})
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response addMember(@ApiParam(value = "Account ID")
                              @PathParam("id")
                              String accountId,
                              @ApiParam(value = "New membership", required = true)
                              @Required
                              NewMembership membership,
                              @Context SecurityContext context) throws ConflictException,
                                                                       NotFoundException,
                                                                       ServerException {
        requiredNotNull(membership, "New membership");
        requiredNotNull(membership.getUserId(), "User ID");
        requiredNotNull(membership.getRoles(), "Roles");
        if (membership.getRoles().isEmpty()) {
            throw new ConflictException("Roles should not be empty");
        }
        userDao.getById(membership.getUserId());//check user exists
        final Member newMember = new Member().withAccountId(accountId)
                                             .withUserId(membership.getUserId())
                                             .withRoles(membership.getRoles());
        accountDao.addMember(newMember);
        LOG.info("EVENT#account-add-member# ACCOUNT-ID#{}# USER-ID#{}# ROLES#{}#",
                 accountId,
                 membership.getUserId(),
                 membership.getRoles().toString());
        return Response.status(Response.Status.CREATED)
                       .entity(toDescriptor(newMember, accountDao.getById(accountId), context))
                       .build();
    }

    /**
     * Returns all members of certain account.
     *
     * @param id
     *         account identifier
     * @return account members
     * @throws NotFoundException
     *         when account with given identifier doesn't exist
     * @throws ServerException
     *         when some error occurred while retrieving accounts or members
     * @see MemberDescriptor
     * @see #addMember(String, NewMembership, SecurityContext)
     * @see #removeMember(String, String)
     */
    @ApiOperation(value = "Get account members",
                  notes = "Get all members for a specific account. This API call requires account/owner, system/admin or system/manager role.",
                  response = MemberDescriptor.class,
                  responseContainer = "List",
                  position = 7)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Account ID not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/{id}/members")
    @RolesAllowed({"account/owner", "system/admin", "system/manager"})
    @Produces(APPLICATION_JSON)
    public List<MemberDescriptor> getMembers(@ApiParam(value = "Account ID")
                                             @PathParam("id") String id,
                                             @Context SecurityContext securityContext) throws NotFoundException, ServerException {
        final Account account = accountDao.getById(id);
        final List<Member> members = accountDao.getMembers(id);
        final List<MemberDescriptor> result = new ArrayList<>(members.size());
        for (Member member : members) {
            result.add(toDescriptor(member, account, securityContext));
        }
        return result;
    }

    /**
     * Removes user with given identifier as member from certain account.
     *
     * @param accountId
     *         account identifier
     * @param userId
     *         user identifier
     * @throws NotFoundException
     *         when user or account with given identifier doesn't exist
     * @throws ServerException
     *         when some error occurred while retrieving account members or removing certain member
     * @throws ConflictException
     *         when removal member is last <i>"account/owner"</i>
     * @see #addMember(String, NewMembership, SecurityContext)
     * @see #getMembers(String, SecurityContext)
     */
    @ApiOperation(value = "Remove user from account",
                  notes = "Remove user from a specific account. This API call requires account/owner, system/admin or system/manager role.",
                  position = 8)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "OK"),
            @ApiResponse(code = 404, message = "Account ID not found"),
            @ApiResponse(code = 409, message = "Account should have at least 1 owner"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @DELETE
    @Path("/{id}/members/{userid}")
    @RolesAllowed({"account/owner", "system/admin", "system/manager"})
    public void removeMember(@ApiParam(value = "Account ID", required = true)
                             @PathParam("id") String accountId,
                             @ApiParam(value = "User ID")
                             @PathParam("userid") String userId) throws NotFoundException, ServerException, ConflictException {
        final List<Member> members = accountDao.getMembers(accountId);
        //search for member
        Member target = null;
        int owners = 0;
        for (Member member : members) {
            if (member.getRoles().contains("account/owner")) owners++;
            if (member.getUserId().equals(userId)) target = member;
        }
        if (target == null) {
            throw new ConflictException(format("User %s doesn't have membership with account %s", userId, accountId));
        }
        //account should have at least 1 owner
        if (owners == 1 && target.getRoles().contains("account/owner")) {
            throw new ConflictException("Account should have at least 1 owner");
        }
        accountDao.removeMember(target);
        LOG.info("EVENT#account-remove-member# ACCOUNT-ID#{}# USER-ID#{}#",
                 accountId,
                 userId);
    }

    /**
     * <p>Updates account.</p>
     * <strong>Note:</strong> existed account attributes with same names as
     * update attributes will be replaced with update attributes.
     *
     * @param accountId
     *         account identifier
     * @param update
     *         account update
     * @return descriptor of updated account
     * @throws NotFoundException
     *         when account with given identifier doesn't exist
     * @throws ConflictException
     *         when account update is {@code null}
     *         or when account with given name already exists
     * @throws ServerException
     *         when some error occurred while retrieving/persisting account
     * @see AccountDescriptor
     */
    @ApiOperation(value = "Update account",
                  notes = "Update account. This API call requires account/owner role.",
                  response = AccountDescriptor.class,
                  position = 9)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Account ID not found"),
            @ApiResponse(code = 409, message = "Invalid account ID or account name already exists"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @POST
    @Path("/{id}")
    @RolesAllowed({"account/owner"})
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public AccountDescriptor update(@ApiParam(value = "Account ID", required = true)
                                    @PathParam("id") String accountId,
                                    AccountUpdate update,
                                    @Context SecurityContext securityContext) throws NotFoundException,
                                                                                     ConflictException,
                                                                                     ServerException {
        requiredNotNull(update, "Account update");
        final Account account = accountDao.getById(accountId);
        //current user should be account owner to update it
        if (update.getName() != null) {
            if (!account.getName().equals(update.getName()) && accountDao.getByName(update.getName()) != null) {
                throw new ConflictException(format("Account with name %s already exists", update.getName()));
            } else {
                account.setName(update.getName());
            }
        }
        if (update.getAttributes() != null) {
            for (String attributeName : update.getAttributes().keySet()) {
                validateAttributeName(attributeName);
            }
            account.getAttributes().putAll(update.getAttributes());
        }
        accountDao.update(account);
        return toDescriptor(account);
    }

    @ApiOperation(value = "Remove account",
                  notes = "Remove subscription from account. JSON with subscription details is sent. Can be performed only by system/admin.",
                  position = 16)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "OK"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "Invalid account ID"),
            @ApiResponse(code = 409, message = "Cannot delete account with associated workspaces"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @DELETE
    @Path("/{id}")
    @RolesAllowed("system/admin")
    public void remove(@ApiParam(value = "Account ID", required = true)
                       @PathParam("id") String id) throws NotFoundException, ServerException, ConflictException {
        accountDao.remove(id);
    }

    @POST
    @Path("/{accountId}/{workspaceId}")
    @Produces(APPLICATION_JSON)
    @RolesAllowed("account/owner")
    public AccountDescriptor registerWorkspace(@PathParam("accountId") String accountId, @PathParam("workspaceId") String workspaceId)
            throws NotFoundException, ServerException, BadRequestException, ConflictException {
        Account account = accountDao.getById(accountId);
        UsersWorkspace workspace = workspaceManager.getWorkspace(workspaceId);
        if (accountDao.isWorkspaceRegistered(workspaceId)) {
            throw new ConflictException("Workspace '" + workspaceId + "' already registered in another account");
        }
        if (account.getWorkspaces().contains(workspace)) {
            throw new ConflictException(format("Workspace '%s' is already registered in this account", workspaceId));
        }
        account.getWorkspaces().add(workspace);
        accountDao.update(account);
        return toDescriptor(account);
    }

    @DELETE
    @Path("/{accountId}/{workspaceId}")
    @Produces(APPLICATION_JSON)
    public AccountDescriptor unregisterWorkspace(@PathParam("accountId") String accountId, @PathParam("workspaceId") String workspaceId)
            throws NotFoundException, ServerException, BadRequestException, ConflictException {
        Account account = accountDao.getById(accountId);
        UsersWorkspaceImpl workspace = workspaceManager.getWorkspace(workspaceId);
        if (!account.getWorkspaces().remove(workspace)) {
            throw new ConflictException(format("Workspace '%s' is not registered in account '%s'", workspaceId, accountId));
        }
        accountDao.update(account);
        return toDescriptor(account);
    }

    private void validateAttributeName(String attributeName) throws ConflictException {
        if (attributeName == null || attributeName.isEmpty() || attributeName.toLowerCase().startsWith("codenvy")) {
            throw new ConflictException(format("Attribute name '%s' is not valid", attributeName));
        }
    }

    /**
     * Converts {@link Account} to {@link AccountDescriptor}
     */
    private AccountDescriptor toDescriptor(Account account) {
        final UriBuilder uriBuilder = getServiceContext().getServiceUriBuilder();
        final List<Link> links = new LinkedList<>();
        links.add(LinksHelper.createLink(HttpMethod.GET,
                                         uriBuilder.clone()
                                                   .path(getClass(), "getMemberships")
                                                   .build()
                                                   .toString(),
                                         null,
                                         APPLICATION_JSON,
                                         Constants.LINK_REL_GET_ACCOUNTS));

        links.add(LinksHelper.createLink(HttpMethod.GET,
                                         uriBuilder.clone()
                                                   .path(getClass(), "getMembers")
                                                   .build(account.getId())
                                                   .toString(),
                                         null,
                                         APPLICATION_JSON,
                                         Constants.LINK_REL_GET_MEMBERS));
        links.add(LinksHelper.createLink(HttpMethod.GET,
                                         uriBuilder.clone()
                                                   .path(getClass(), "getById")
                                                   .build(account.getId())
                                                   .toString(),
                                         null,
                                         APPLICATION_JSON,
                                         Constants.LINK_REL_GET_ACCOUNT_BY_ID));
        if (securityContext.isUserInRole("system/admin") || securityContext.isUserInRole("system/manager")) {
            links.add(LinksHelper.createLink(HttpMethod.GET,
                                             uriBuilder.clone()
                                                       .path(getClass(), "getByName")
                                                       .queryParam("name", account.getName())
                                                       .build()
                                                       .toString(),
                                             null,
                                             APPLICATION_JSON,
                                             Constants.LINK_REL_GET_ACCOUNT_BY_NAME));
        }
        if (securityContext.isUserInRole("system/admin")) {
            links.add(LinksHelper.createLink(HttpMethod.DELETE,
                                             uriBuilder.clone().path(getClass(), "remove")
                                                       .build(account.getId())
                                                       .toString(),
                                             null,
                                             null,
                                             Constants.LINK_REL_REMOVE_ACCOUNT));
        }

        if (!securityContext.isUserInRole("account/owner") &&
            !securityContext.isUserInRole("account/member") &&
            !securityContext.isUserInRole("system/admin") &&
            !securityContext.isUserInRole("system/manager")) {
            account.getAttributes().clear();
        }
        account.getAttributes().remove("codenvy:creditCardToken");
        account.getAttributes().remove("codenvy:billing.date");

        List<UsersWorkspaceDto> workspaces = account.getWorkspaces()
                                                    .stream()
                                                    .map(DtoConverter::asDto)
                                                    .collect(toList());
        return DtoFactory.getInstance().createDto(AccountDescriptor.class)
                         .withId(account.getId())
                         .withName(account.getName())
                         .withAttributes(account.getAttributes())
                         .withWorkspaces(workspaces)
                         .withLinks(links);
    }

    /**
     * Converts {@link Member} to {@link MemberDescriptor}
     */
    private MemberDescriptor toDescriptor(Member member, Account account, SecurityContext securityContext) {
        final UriBuilder uriBuilder = getServiceContext().getServiceUriBuilder();
        final Link removeMember = LinksHelper.createLink(HttpMethod.DELETE,
                                                         uriBuilder.clone()
                                                                   .path(getClass(), "removeMember")
                                                                   .build(account.getId(), member.getUserId())
                                                                   .toString(),
                                                         null,
                                                         null,
                                                         Constants.LINK_REL_REMOVE_MEMBER);
        final Link allMembers = LinksHelper.createLink(HttpMethod.GET,
                                                       uriBuilder.clone()
                                                                 .path(getClass(), "getMembers")
                                                                 .build(account.getId())
                                                                 .toString(),
                                                       null,
                                                       APPLICATION_JSON,
                                                       Constants.LINK_REL_GET_MEMBERS);
        final AccountReference accountRef = DtoFactory.getInstance().createDto(AccountReference.class)
                                                      .withId(account.getId())
                                                      .withName(account.getName());
        if (member.getRoles().contains("account/owner") ||
            securityContext.isUserInRole("system/admin") ||
            securityContext.isUserInRole("system/manager")) {
            accountRef.setLinks(singletonList(LinksHelper.createLink(HttpMethod.GET,
                                                                     uriBuilder.clone()
                                                                               .path(getClass(), "getById")
                                                                               .build(account.getId())
                                                                               .toString(),
                                                                     null,
                                                                     APPLICATION_JSON,
                                                                     Constants.LINK_REL_GET_ACCOUNT_BY_ID)));
        }
        return DtoFactory.getInstance().createDto(MemberDescriptor.class)
                         .withUserId(member.getUserId())
                         .withRoles(member.getRoles())
                         .withAccountReference(accountRef)
                         .withLinks(Arrays.asList(removeMember, allMembers));
    }

    /**
     * Checks object reference is not {@code null}
     *
     * @param object
     *         object reference to check
     * @param subject
     *         used as subject of exception message "{subject} required"
     * @throws ConflictException
     *         when object reference is {@code null}
     */
    private void requiredNotNull(Object object, String subject) throws ConflictException {
        if (object == null) {
            throw new ConflictException(subject + " required");
        }
    }
}
