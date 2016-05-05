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

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.factory.server.FactoryConstants;
import org.eclipse.che.api.factory.server.builder.FactoryBuilder;
import org.eclipse.che.api.factory.shared.dto.Action;
import org.eclipse.che.api.factory.shared.dto.Author;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.factory.shared.dto.Ide;
import org.eclipse.che.api.factory.shared.dto.OnAppClosed;
import org.eclipse.che.api.factory.shared.dto.OnAppLoaded;
import org.eclipse.che.api.factory.shared.dto.OnProjectsLoaded;
import org.eclipse.che.api.factory.shared.dto.Policies;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Mockito.when;

@Listeners(value = {MockitoTestNGListener.class})
public class FactoryBaseValidatorTest {
    private static final String VALID_REPOSITORY_URL = "http://github.com/codenvy/cloudide";
    private static final String VALID_PROJECT_PATH   = "/cloudide";
    private static final String ID                   = "id";


    @Mock
    private UserDao userDao;

    @Mock
    private PreferenceDao preferenceDao;

    @Mock
    private FactoryBuilder builder;

    @Mock
    private HttpServletRequest request;

    private TesterFactoryBaseValidator validator;

    private Factory factory;

    @BeforeMethod
    public void setUp() throws ParseException, NotFoundException, ServerException {
        factory = newDto(Factory.class)
                .withV("4.0")
                .withCreator(newDto(Author.class)
                                     .withAccountId(ID)
                                     .withUserId("userid"));

        User user = new User().withId("userid");

        when(userDao.getById("userid")).thenReturn(user);
        validator = new TesterFactoryBaseValidator(preferenceDao);
    }

    @Test
    public void shouldBeAbleToValidateFactoryUrlObject() throws ApiException {
        factory = prepareFactoryWithGivenStorage("git", VALID_REPOSITORY_URL, VALID_PROJECT_PATH);
        validator.validateProjects(factory);
        validator.validateProjects(factory);
        validator.validateAccountId(factory);
    }

    @Test
    public void shouldBeAbleToValidateFactoryUrlObjectIfStorageIsESBWSO2() throws ApiException {
        factory = prepareFactoryWithGivenStorage("esbwso2", VALID_REPOSITORY_URL, VALID_PROJECT_PATH);
        validator.validateProjects(factory);
        validator.validateProjects(factory);
        validator.validateAccountId(factory);
    }

    @Test(expectedExceptions = ApiException.class,
          expectedExceptionsMessageRegExp =
                  "The parameter project.source.location has a value submitted http://codenvy.com/git/04%2 with a value that is " +
                  "unexpected. " +
                  "For more information, please visit http://docs.codenvy.com/user/project-lifecycle/#configuration-reference")
    public void shouldNotValidateIfStorageLocationContainIncorrectEncodedSymbol() throws ApiException {
        // given
        factory = prepareFactoryWithGivenStorage("git", "http://codenvy.com/git/04%2", VALID_PROJECT_PATH);

        // when, then
        validator.validateProjects(factory);
    }


    @Test
    public void shouldValidateIfStorageLocationIsCorrectSsh() throws ApiException {
        // given
        factory =
                prepareFactoryWithGivenStorage("git", "ssh://codenvy@review.gerrithub.io:29418/codenvy/exampleProject", "example-project");

        // when, then
        validator.validateProjects(factory);
    }

    @Test
    public void shouldValidateIfStorageLocationIsCorrectHttps() throws ApiException {
        // given
        factory = prepareFactoryWithGivenStorage("git", "https://github.com/codenvy/example.git", "/example");

        // when, then
        validator.validateProjects(factory);
    }

    @Test
    public void shouldValidateSubProjectWithNoLocation() throws ApiException {
        // given
        factory = prepareFactoryWithGivenStorage("git", "null", "/cloudide/core");

        // when, then
        validator.validateProjects(factory);
    }

    @Test(dataProvider = "badAdvancedFactoryUrlProvider", expectedExceptions = ApiException.class)
    public void shouldNotValidateIfStorageOrStorageLocationIsInvalid(Factory factory) throws ApiException {
        validator.validateProjects(factory);
    }

    @DataProvider(name = "badAdvancedFactoryUrlProvider")
    public Object[][] invalidParametersFactoryUrlProvider() throws UnsupportedEncodingException {
        Factory adv1 = prepareFactoryWithGivenStorage("notagit", VALID_REPOSITORY_URL, VALID_PROJECT_PATH);
        Factory adv2 = prepareFactoryWithGivenStorage("git", null, VALID_PROJECT_PATH);
        Factory adv3 = prepareFactoryWithGivenStorage("git", "", VALID_PROJECT_PATH);
        return new Object[][]{
                {adv1},// invalid vcs
                {adv2},// invalid vcsurl
                {adv3}// invalid vcsurl
        };
    }

    @Test(dataProvider = "invalidProjectNamesProvider", expectedExceptions = ApiException.class,
          expectedExceptionsMessageRegExp = "Project name must contain only Latin letters, " +
                                            "digits or these following special characters -._.")
    public void shouldThrowFactoryUrlExceptionIfProjectNameInvalid(String projectName) throws Exception {
        // given
        factory.withWorkspace(newDto(WorkspaceConfigDto.class).withProjects(Collections.singletonList(newDto(ProjectConfigDto.class)
                                                                                                              .withType("type")
                                                                                                              .withName(projectName))));
        // when, then
        validator.validateProjects(factory);
    }

    @Test(dataProvider = "validProjectNamesProvider")
    public void shouldBeAbleToValidateValidProjectName(String projectName) throws Exception {
        // given
        prepareFactoryWithGivenStorage("git", VALID_REPOSITORY_URL, VALID_PROJECT_PATH);
        factory.withWorkspace(newDto(WorkspaceConfigDto.class).withProjects(
                Collections.singletonList(newDto(ProjectConfigDto.class).withType("type")
                                                                        .withName(projectName)
                                                                        .withSource(newDto(SourceStorageDto.class)
                                                                                            .withType("git")
                                                                                            .withLocation(VALID_REPOSITORY_URL))
                                                                        .withPath(VALID_PROJECT_PATH))));
        // when, then
        validator.validateProjects(factory);
    }

    @DataProvider(name = "validProjectNamesProvider")
    public Object[][] validProjectNames() {
        return new Object[][]{
                {"untitled"},
                {"Untitled"},
                {"untitled.project"},
                {"untitled-project"},
                {"untitled_project"},
                {"untitled01"},
                {"000011111"},
                {"0untitled"},
                {"UU"},
                {"untitled-proj12"},
                {"untitled.pro....111"},
                {"SampleStruts"}
        };
    }

    @DataProvider(name = "invalidProjectNamesProvider")
    public Object[][] invalidProjectNames() {
        return new Object[][]{
                {"-untitled"},
                {"untitled->3"},
                {"untitled__2%"},
                {"untitled_!@#$%^&*()_+?><"}
        };
    }

    @Test
    public void shouldBeAbleToValidateIfOrgIdIsValid() throws ApiException, ParseException {
        validator.validateAccountId(factory);
    }

    @Test
    public void shouldBeAbleToValidateIfOrgIdAndOwnerAreValid()
            throws ApiException, ParseException {
        // when, then
        validator.validateAccountId(factory);
    }

    @Test
    public void shouldValidateIfCurrentTimeBeforeSinceUntil() throws Exception {
        Long currentTime = new Date().getTime();

        factory.withPolicies(newDto(Policies.class)
                                     .withSince(currentTime + 10000L)
                                     .withUntil(currentTime + 20000L));
        validator.validateCurrentTimeAfterSinceUntil(factory);
    }

    @Test(expectedExceptions = ApiException.class,
          expectedExceptionsMessageRegExp = FactoryConstants.INVALID_SINCE_MESSAGE)
    public void shouldNotValidateIfSinceBeforeCurrent() throws ApiException {
        factory.withPolicies(newDto(Policies.class)
                                     .withSince(1L));
        validator.validateCurrentTimeAfterSinceUntil(factory);
    }

    @Test(expectedExceptions = ApiException.class,
          expectedExceptionsMessageRegExp = FactoryConstants.INVALID_UNTIL_MESSAGE)
    public void shouldNotValidateIfUntilBeforeCurrent() throws ApiException {
        factory.withPolicies(newDto(Policies.class)
                                     .withUntil(1L));
        validator.validateCurrentTimeAfterSinceUntil(factory);
    }

    @Test(expectedExceptions = ApiException.class,
          expectedExceptionsMessageRegExp = FactoryConstants.INVALID_SINCEUNTIL_MESSAGE)
    public void shouldNotValidateIfUntilBeforeSince() throws ApiException {
        factory.withPolicies(newDto(Policies.class)
                                     .withSince(2L)
                                     .withUntil(1L));

        validator.validateCurrentTimeAfterSinceUntil(factory);
    }

    @Test(expectedExceptions = ApiException.class,
          expectedExceptionsMessageRegExp = FactoryConstants.ILLEGAL_FACTORY_BY_UNTIL_MESSAGE)
    public void shouldNotValidateIfUntilBeforeCurrentTime() throws ApiException {
        Long currentTime = new Date().getTime();
        factory.withPolicies(newDto(Policies.class)
                                     .withUntil(currentTime - 10000L));

        validator.validateCurrentTimeBetweenSinceUntil(factory);
    }

    @Test
    public void shouldValidateIfCurrentTimeBetweenUntilSince() throws ApiException {
        Long currentTime = new Date().getTime();

        factory.withPolicies(newDto(Policies.class)
                                     .withSince(currentTime - 10000L)
                                     .withUntil(currentTime + 10000L));

        validator.validateCurrentTimeBetweenSinceUntil(factory);
    }

    @Test(expectedExceptions = ApiException.class,
          expectedExceptionsMessageRegExp = FactoryConstants.ILLEGAL_FACTORY_BY_SINCE_MESSAGE)
    public void shouldNotValidateIfUntilSinceAfterCurrentTime() throws ApiException {
        Long currentTime = new Date().getTime();
        factory.withPolicies(newDto(Policies.class)
                                     .withSince(currentTime + 10000L));

        validator.validateCurrentTimeBetweenSinceUntil(factory);
    }


    @Test
    public void shouldValidateTrackedParamsIfOrgIdIsMissingButOnPremisesTrue() throws Exception {
        final DtoFactory dtoFactory = DtoFactory.getInstance();
        Factory factory = dtoFactory.createDto(Factory.class);
        factory.withV("4.0")
               .withPolicies(dtoFactory.createDto(Policies.class)
                                       .withSince(System.currentTimeMillis() + 1_000_000)
                                       .withUntil(System.currentTimeMillis() + 10_000_000)
                                       .withReferer("codenvy.com"));
        validator = new TesterFactoryBaseValidator(preferenceDao);

        validator.validateAccountId(factory);
    }


    @Test(expectedExceptions = BadRequestException.class)
    public void shouldNotValidateOpenfileActionIfInWrongSectionOnAppClosed() throws Exception {
        //given
        validator = new TesterFactoryBaseValidator(preferenceDao);
        List<Action> actions = Arrays.asList(newDto(Action.class)
                                                     .withId("openFile"));
        Ide ide = newDto(Ide.class)
                .withOnAppClosed(newDto(OnAppClosed.class)
                                         .withActions(actions));
        Factory factoryWithAccountId = DtoFactory.getInstance().clone(factory).withIde(ide);
        //when
        validator.validateProjectActions(factoryWithAccountId);
    }

    @Test(expectedExceptions = BadRequestException.class)
    public void shouldNotValidateFindReplaceActionIfInWrongSectionOnAppLoaded() throws Exception {
        //given
        validator = new TesterFactoryBaseValidator(preferenceDao);
        List<Action> actions = Arrays.asList(newDto(Action.class)
                                                     .withId("findReplace"));
        Ide ide = newDto(Ide.class)
                .withOnAppLoaded(newDto(OnAppLoaded.class)
                                         .withActions(actions));
        Factory factoryWithAccountId = DtoFactory.getInstance().clone(factory).withIde(ide);
        //when
        validator.validateProjectActions(factoryWithAccountId);
    }

    @Test(expectedExceptions = BadRequestException.class)
    public void shouldNotValidateIfOpenfileActionInsufficientParams() throws Exception {
        //given
        validator = new TesterFactoryBaseValidator(preferenceDao);
        List<Action> actions = Arrays.asList(newDto(Action.class)
                                                     .withId("openFile"));
        Ide ide = newDto(Ide.class)
                .withOnProjectsLoaded(newDto(OnProjectsLoaded.class)
                                              .withActions(actions));
        Factory factoryWithAccountId = DtoFactory.getInstance().clone(factory).withIde(ide);
        //when
        validator.validateProjectActions(factoryWithAccountId);
    }

    @Test(expectedExceptions = BadRequestException.class)
    public void shouldNotValidateIfrunCommandActionInsufficientParams() throws Exception {
        //given
        validator = new TesterFactoryBaseValidator(preferenceDao);
        List<Action> actions = Arrays.asList(newDto(Action.class)
                                                     .withId("openFile"));
        Ide ide = newDto(Ide.class)
                .withOnProjectsLoaded(newDto(OnProjectsLoaded.class)
                                              .withActions(actions));
        Factory factoryWithAccountId = DtoFactory.getInstance().clone(factory).withIde(ide);
        //when
        validator.validateProjectActions(factoryWithAccountId);
    }

    @Test(expectedExceptions = BadRequestException.class)
    public void shouldNotValidateIfOpenWelcomePageActionInsufficientParams() throws Exception {
        //given
        validator = new TesterFactoryBaseValidator(preferenceDao);
        List<Action> actions = Arrays.asList(newDto(Action.class)
                                                     .withId("openWelcomePage"));
        Ide ide = newDto(Ide.class)
                .withOnAppLoaded((newDto(OnAppLoaded.class)
                        .withActions(actions)));
        Factory factoryWithAccountId = DtoFactory.getInstance().clone(factory).withIde(ide);
        //when
        validator.validateProjectActions(factoryWithAccountId);
    }

    @Test(expectedExceptions = BadRequestException.class)
    public void shouldNotValidateIfFindReplaceActionInsufficientParams() throws Exception {
        //given
        validator = new TesterFactoryBaseValidator(preferenceDao);
        Map<String, String> params = new HashMap<>();
        params.put("in", "pom.xml");
        // find is missing!
        params.put("replace", "123");
        List<Action> actions = Arrays.asList(newDto(Action.class)
                                                     .withId("findReplace")
                                                     .withProperties(params));
        Ide ide = newDto(Ide.class)
                .withOnProjectsLoaded(newDto(OnProjectsLoaded.class)
                                              .withActions(actions));
        Factory factoryWithAccountId = DtoFactory.getInstance().clone(factory).withIde(ide);
        //when
        validator.validateProjectActions(factoryWithAccountId);
    }

    @Test
    public void shouldValidateFindReplaceAction() throws Exception {
        //given
        validator = new TesterFactoryBaseValidator(preferenceDao);
        Map<String, String> params = new HashMap<>();
        params.put("in", "pom.xml");
        params.put("find", "123");
        params.put("replace", "456");
        List<Action> actions = Arrays.asList(newDto(Action.class)
                                                     .withId("findReplace")
                                                     .withProperties(params));
        Ide ide = newDto(Ide.class)
                .withOnProjectsLoaded(newDto(OnProjectsLoaded.class)
                                              .withActions(actions));
        Factory factoryWithAccountId = DtoFactory.getInstance().clone(factory).withIde(ide);
        //when
        validator.validateProjectActions(factoryWithAccountId);
    }

    @Test
    public void shouldValidateOpenfileAction() throws Exception {
        //given
        validator = new TesterFactoryBaseValidator(preferenceDao);
        Map<String, String> params = new HashMap<>();
        params.put("file", "pom.xml");
        List<Action> actions = Arrays.asList(newDto(Action.class)
                                                     .withId("openFile")
                                                     .withProperties(params));
        Ide ide = newDto(Ide.class)
                .withOnProjectsLoaded(newDto(OnProjectsLoaded.class)
                                              .withActions(actions));
        Factory factoryWithAccountId = DtoFactory.getInstance().clone(factory).withIde(ide);
        //when
        validator.validateProjectActions(factoryWithAccountId);
    }


    @DataProvider(name = "trackedFactoryParameterWithoutValidAccountId")
    public Object[][] trackedFactoryParameterWithoutValidAccountId() throws URISyntaxException, IOException, NoSuchMethodException {
        return new Object[][]{
                {
                        newDto(Factory.class)
                                .withV("4.0")
                                .withIde(newDto(Ide.class)
                                                 .withOnAppLoaded(newDto(OnAppLoaded.class)
                                                                          .withActions(singletonList(newDto(Action.class)
                                                                                                             .withId("openWelcomePage")
                                                                                                             .withProperties(
                                                                                                                     ImmutableMap
                                                                                                                             .<String,
                                                                                                                                     String>builder()
                                                                                                                             .put("authenticatedTitle",
                                                                                                                                  "title")
                                                                                                                             .put("authenticatedIconUrl",
                                                                                                                                  "url")
                                                                                                                             .put("authenticatedContentUrl",
                                                                                                                                  "url")
                                                                                                                             .put("nonAuthenticatedTitle",
                                                                                                                                  "title")
                                                                                                                             .put("nonAuthenticatedIconUrl",
                                                                                                                                  "url")
                                                                                                                             .put("nonAuthenticatedContentUrl",
                                                                                                                                  "url")
                                                                                                                             .build()))
                                                                                      )))},

                {newDto(Factory.class)
                         .withV("4.0")
                         .withPolicies(newDto(Policies.class)
                                               .withSince(10000L))},
                {newDto(Factory.class)
                         .withV("4.0")
                         .withPolicies(newDto(Policies.class)
                                               .withUntil(10000L))},
                {newDto(Factory.class)
                         .withV("4.0")
                         .withPolicies(newDto(Policies.class)
                                               .withReferer("host"))}
        };
    }

    private Factory prepareFactoryWithGivenStorage(String type, String location, String path) {
        return factory.withWorkspace(newDto(WorkspaceConfigDto.class)
                                             .withProjects(Collections.singletonList(newDto(ProjectConfigDto.class)
                                                                                             .withSource(newDto(SourceStorageDto.class)
                                                                                                                 .withType(type)
                                                                                                                 .withLocation(
                                                                                                                         location))
                                                                                             .withPath(path))));
    }
}
