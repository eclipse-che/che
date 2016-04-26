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
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.factory.server.FactoryConstants;
import org.eclipse.che.api.factory.shared.dto.Action;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.factory.shared.dto.Ide;
import org.eclipse.che.api.factory.shared.dto.OnAppLoaded;
import org.eclipse.che.api.factory.shared.dto.OnProjectsLoaded;
import org.eclipse.che.api.factory.shared.dto.Policies;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;

/**
 * Validates values of factory parameters.
 *
 * @author Alexander Garagatyi
 * @author Valeriy Svydenko
 */
public abstract class FactoryBaseValidator {
    private static final Pattern PROJECT_NAME_VALIDATOR = Pattern.compile("^[\\\\\\w\\\\\\d]+[\\\\\\w\\\\\\d_.-]*$");

    private final AccountDao    accountDao;
    private final PreferenceDao preferenceDao;

    public FactoryBaseValidator(AccountDao accountDao,
                                PreferenceDao preferenceDao) {
        this.accountDao = accountDao;
        this.preferenceDao = preferenceDao;
    }

    /**
     * Validates source parameter of factory.
     *
     * @param factory
     *         factory to validate
     * @throws BadRequestException
     *         when source projects in the factory is invalid
     */
    protected void validateProjects(Factory factory) throws BadRequestException {
        for (ProjectConfigDto project : factory.getWorkspace().getProjects()) {
            final String projectName = project.getName();
            if (null != projectName && !PROJECT_NAME_VALIDATOR.matcher(projectName)
                                                              .matches()) {
                throw new BadRequestException("Project name must contain only Latin letters, " +
                                              "digits or these following special characters -._.");
            }

            if (project.getPath().indexOf('/', 1) == -1) {

                final String location = project.getSource().getLocation();
                final String parameterLocationName = "project.source.location";

                if (isNullOrEmpty(location)) {
                    throw new BadRequestException(format(FactoryConstants.PARAMETRIZED_ILLEGAL_PARAMETER_VALUE_MESSAGE,
                                                         parameterLocationName,
                                                         location));
                }

                try {
                    URLDecoder.decode(location, "UTF-8");
                } catch (IllegalArgumentException | UnsupportedEncodingException e) {
                    throw new BadRequestException(format(FactoryConstants.PARAMETRIZED_ILLEGAL_PARAMETER_VALUE_MESSAGE,
                                                         parameterLocationName,
                                                         location));
                }
            }
        }
    }

    /**
     * Validates that creator of factory is really owner of account specified in it.
     *
     * @param factory
     *         factory to validate
     * @throws ServerException
     *         when any server errors occurs
     * @throws ForbiddenException
     *         when user does not have required rights
     */
    protected void validateAccountId(Factory factory) throws ServerException, ForbiddenException {
        // TODO do we need check if user is temporary?
        final String accountId = factory.getCreator() != null ? emptyToNull(factory.getCreator().getAccountId()) : null;
        final String userId = factory.getCreator() != null ? factory.getCreator().getUserId() : null;

        if (accountId == null || userId == null) {
            return;
        }

        final Map<String, String> preferences = preferenceDao.getPreferences(userId);
        if (parseBoolean(preferences.get("temporary"))) {
            throw new ForbiddenException("Current user is not allowed to use this method.");
        }

        final List<Member> members = accountDao.getMembers(accountId);
        if (members.isEmpty()) {
            throw new ForbiddenException(format(FactoryConstants.PARAMETRIZED_ILLEGAL_ACCOUNTID_PARAMETER_MESSAGE, accountId));
        }

        if (members.stream().noneMatch(member -> member.getUserId()
                                                       .equals(userId) && member.getRoles()
                                                                                .contains("account/owner"))) {
            throw new ForbiddenException("You are not authorized to use this accountId.");
        }
    }

    /**
     * Validates that factory can be used at present time (used on accept)
     *
     * @param factory
     *         factory to validate
     * @throws BadRequestException
     *         if since date greater than current date<br/>
     * @throws BadRequestException
     *         if until date less than current date<br/>
     */
    protected void validateCurrentTimeBetweenSinceUntil(Factory factory) throws BadRequestException {
        final Policies policies = factory.getPolicies();
        if (policies == null) {
            return;
        }

        final Long since = policies.getSince() == null ? 0L : policies.getSince();
        final Long until = policies.getUntil() == null ? 0L : policies.getUntil();

        if (since != 0 && currentTimeMillis() < since) {
            throw new BadRequestException(FactoryConstants.ILLEGAL_FACTORY_BY_SINCE_MESSAGE);
        }

        if (until != 0 && currentTimeMillis() > until) {
            throw new BadRequestException(FactoryConstants.ILLEGAL_FACTORY_BY_UNTIL_MESSAGE);
        }
    }

    /**
     * Validates correct valid since and until times are used (on factory creation)
     *
     * @param factory
     *         factory to validate
     * @throws BadRequestException
     *         if since date greater or equal than until date<br/>
     * @throws BadRequestException
     *         if since date less than current date<br/>
     * @throws BadRequestException
     *         if until date less than current date<br/>
     */
    protected void validateCurrentTimeAfterSinceUntil(Factory factory) throws BadRequestException {
        final Policies policies = factory.getPolicies();
        if (policies == null) {
            return;
        }

        final Long since = policies.getSince() == null ? 0L : policies.getSince();
        final Long until = policies.getUntil() == null ? 0L : policies.getUntil();

        if (since != 0 && until != 0 && since >= until) {
            throw new BadRequestException(FactoryConstants.INVALID_SINCEUNTIL_MESSAGE);
        }

        if (since != 0 && currentTimeMillis() > since) {
            throw new BadRequestException(FactoryConstants.INVALID_SINCE_MESSAGE);
        }

        if (until != 0 && currentTimeMillis() > until) {
            throw new BadRequestException(FactoryConstants.INVALID_UNTIL_MESSAGE);
        }
    }


    /**
     * Validates IDE actions
     *
     * @param factory
     *         factory to validate
     * @throws BadRequestException
     *         when factory actions is invalid
     */
    protected void validateProjectActions(Factory factory) throws BadRequestException {
        final Ide ide = factory.getIde();
        if (ide == null) {
            return;
        }

        final List<Action> applicationActions = new ArrayList<>();
        if (ide.getOnAppClosed() != null) {
            applicationActions.addAll(ide.getOnAppClosed().getActions());
        }
        if (ide.getOnAppLoaded() != null) {
            applicationActions.addAll(ide.getOnAppLoaded().getActions());
        }

        for (Action applicationAction : applicationActions) {
            String id = applicationAction.getId();
            if ("openFile".equals(id) || "findReplace".equals(id) || "runCommand".equals(id) || "newTerminal".equals(id)) {
                throw new BadRequestException(format(FactoryConstants.INVALID_ACTION_SECTION, id));
            }
        }

        final OnAppLoaded onAppLoaded = ide.getOnAppLoaded();
        if (onAppLoaded != null) {
            for (Action action : onAppLoaded.getActions()) {
                final Map<String, String> properties = action.getProperties();
                if ("openWelcomePage".equals(action.getId()) && isNullOrEmpty(properties.get("greetingContentUrl"))) {
                    throw new BadRequestException(FactoryConstants.INVALID_WELCOME_PAGE_ACTION);
                }
            }
        }

        final OnProjectsLoaded onLoaded = ide.getOnProjectsLoaded();
        if (onLoaded != null) {
            final List<Action> onProjectOpenedActions = onLoaded.getActions();
            for (Action applicationAction : onProjectOpenedActions) {
                final String id = applicationAction.getId();
                final Map<String, String> properties = applicationAction.getProperties();

                switch (id) {
                    case "openFile":
                        if (isNullOrEmpty(properties.get("file"))) {
                            throw new BadRequestException(FactoryConstants.INVALID_OPENFILE_ACTION);
                        }
                        break;
                        
                    case "runCommand":
                        if (isNullOrEmpty(properties.get("name"))) {
                            throw new BadRequestException(FactoryConstants.INVALID_RUNCOMMAND_ACTION);
                        }
                        break;

                    case "findReplace":
                        if (isNullOrEmpty(properties.get("in")) ||
                            isNullOrEmpty(properties.get("find")) ||
                            isNullOrEmpty(properties.get("replace"))) {
                            throw new BadRequestException(FactoryConstants.INVALID_FIND_REPLACE_ACTION);
                        }
                        break;
                }
            }
        }
    }
}
