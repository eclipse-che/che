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
package org.eclipse.che.api.factory.server.impl;

import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Member;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.factory.server.FactoryEditValidator;
import org.eclipse.che.api.factory.shared.dto.Author;
import org.eclipse.che.api.factory.shared.dto.Factory;

import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.User;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link FactoryEditValidator}
 * @author Florent Benoit
 */
@Listeners(value = {MockitoTestNGListener.class})
public class FactoryEditValidatorImplTest {

    @Mock
    private AccountDao accountDao;

    @Mock
    private Factory factory;

    @InjectMocks
    private FactoryEditValidator factoryEditValidator = new FactoryEditValidatorImpl();

    /**
     * Check missing author data
     * @throws ApiException
     */
    @Test(expectedExceptions = ServerException.class)
    public void testNoAuthor() throws ApiException {
        setCurrentUser("");
        factoryEditValidator.validate(factory);
    }

    /**
     * Check when user is the same than the one than create the factory
     * @throws ApiException
     */
    @Test
    public void testUserIsTheAuthor() throws ApiException {
        String userId = "florent";
        setCurrentUser(userId);
        Author author = mock(Author.class);
        doReturn(author).when(factory)
                        .getCreator();
        doReturn(userId).when(author)
                        .getUserId();

        factoryEditValidator.validate(factory);
    }

    /**
     * Check when factory has no account id and user is not the same
     * @throws ApiException
     */
    @Test(expectedExceptions = ForbiddenException.class)
    public void testWithoutAccountID() throws ApiException {
        setCurrentUser("toto");
        String userIdFactory = "florent";
        Author author = mock(Author.class);
        doReturn(author).when(factory)
                        .getCreator();
        doReturn(userIdFactory).when(author)
                               .getUserId();

        factoryEditValidator.validate(factory);
    }


    /**
     * Check when factory has account id without members
     * @throws ApiException
     */
    @Test(expectedExceptions = ForbiddenException.class)
    public void testUserWithNoMembersInAccountID() throws ApiException {
        String userIdFactory = "florent";
        String accountId = "myAccount";
        setCurrentUser("toto");
        Author author = mock(Author.class);
        doReturn("123").when(factory).getId();
        doReturn(author).when(factory).getCreator();
        doReturn(userIdFactory).when(author).getUserId();
        doReturn(accountId).when(author).getAccountId();

        factoryEditValidator.validate(factory);
    }


    /**
     * Check when user is not the same and is not account owner
     * @throws ApiException
     */
    @Test(expectedExceptions = ForbiddenException.class)
    public void testUserNotInAccountOwner() throws ApiException {
        String currentUserId = "florent";
        setCurrentUser(currentUserId);
        String userIdFactory = "johndoe";
        String accountId = "myAccount";
        Author author = mock(Author.class);
        doReturn("123").when(factory).getId();
        doReturn(author).when(factory).getCreator();
        doReturn(userIdFactory).when(author).getUserId();
        doReturn(accountId).when(author).getAccountId();

        Member member = mock(Member.class);
        doReturn(currentUserId).when(member).getUserId();
        List<Member> members = new ArrayList<>();
        members.add(member);
        doReturn(members).when(accountDao).getMembers(eq(accountId));

        factoryEditValidator.validate(factory);
    }

    /**
     * Check when user is not the same but is an account owner
     * @throws ApiException
     */
    @Test
    public void testUserIsAccountOwner() throws ApiException {
        String currentUserId = "florent";
        setCurrentUser(currentUserId);
        String userIdFactory = "johndoe";
        String accountId = "myAccount";
        Author author = mock(Author.class);
        doReturn("123").when(factory).getId();
        doReturn(author).when(factory).getCreator();
        doReturn(userIdFactory).when(author).getUserId();
        doReturn(accountId).when(author).getAccountId();

        Member member = mock(Member.class);
        doReturn(currentUserId).when(member).getUserId();
        List<String> roles = Arrays.asList("account/owner");
        doReturn(roles).when(member).getRoles();
        List<Member> members = new ArrayList<>();
        members.add(member);
        doReturn(members).when(accountDao).getMembers(eq(accountId));

        factoryEditValidator.validate(factory);
    }


    private void setCurrentUser(String userId) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        EnvironmentContext.getCurrent().setUser(user);
    }
}
