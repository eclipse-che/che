/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.Recipe;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.workspace.server.spi.ValidationException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

/**
 * Validator for {@link Workspace}.
 * In all the cases when entity is not valid throws {@link ValidationException}.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class WorkspaceValidator {
    /* should contain [3, 20] characters, first and last character is letter or digit, available characters {A-Za-z0-9.-_}*/
    private static final Pattern WS_NAME = Pattern.compile("[a-zA-Z0-9][-_.a-zA-Z0-9]{1,18}[a-zA-Z0-9]");

    private final WorkspaceRuntimes runtimes;

    @Inject
    public WorkspaceValidator(WorkspaceRuntimes runtimes) {
        this.runtimes = runtimes;
    }

    public void validateWorkspace(Workspace workspace) throws BadRequestException,
                                                              ServerException {
//        validateAttributes(workspace.getAttributes());
//        validateConfig(workspace.getConfig());
    }

    public void validateConfig(WorkspaceConfig config) throws BadRequestException,
                                                              NotFoundException,
                                                              ServerException {
        // configuration object itself
        checkNotNull(config.getName(), "Workspace name required");
        checkArgument(WS_NAME.matcher(config.getName()).matches(),
                      "Incorrect workspace name, it must be between 3 and 20 characters and may contain digits, " +
                      "latin letters, underscores, dots, dashes and should start and end only with digits, " +
                      "latin letters or underscores");


        //environments
        checkArgument(!isNullOrEmpty(config.getDefaultEnv()), "Workspace default environment name required");
        checkNotNull(config.getEnvironments(), "Workspace should contain at least one environment");
        checkArgument(config.getEnvironments().containsKey(config.getDefaultEnv()),
                      "Workspace default environment configuration required");

        for (Map.Entry<String, ? extends Environment> envEntry : config.getEnvironments().entrySet()) {
            try {

                validateEnvironment(envEntry.getValue());

            } catch (IllegalArgumentException e) {
                throw new BadRequestException(e.getLocalizedMessage());
            }
        }

        //commands
        for (Command command : config.getCommands()) {
            checkArgument(!isNullOrEmpty(command.getName()),
                          "Workspace %s contains command with null or empty name",
                          config.getName());
            checkArgument(!isNullOrEmpty(command.getCommandLine()),
                          "Command line required for command '%s' in workspace '%s'",
                          command.getName(),
                          config.getName());
        }

        //projects
        //TODO
    }


    private void validateEnvironment(Environment environment) throws BadRequestException, NotFoundException, ServerException {

        checkNotNull(environment, "Environment should not be null");
        Recipe recipe = environment.getRecipe();
        checkNotNull(recipe, "Environment recipe should not be null");
        checkNotNull(recipe.getType(), "Environment recipe type should not be null");

        // TODO need that?
//        checkArgument(recipe.getContent() != null || recipe.getLocation() != null,
//                      "OldRecipe of environment must contain location or content");
//        checkArgument(recipe.getContent() != null && recipe.getLocation() != null,
//                      "OldRecipe of environment must contain either location or content but not both");

// FIXME: spi
//        runtimes.estimate(environment);
    }

    public void validateAttributes(Map<String, String> attributes) throws BadRequestException {
        for (String attributeName : attributes.keySet()) {
            //attribute name should not be empty and should not start with codenvy
            checkArgument(attributeName != null && !attributeName.trim().isEmpty() && !attributeName.toLowerCase().startsWith("codenvy"),
                          "Attribute name '%s' is not valid",
                          attributeName);
        }
    }

    /**
     * Checks that object reference is not null, throws {@link BadRequestException}
     * in the case of null {@code object} with given {@code message}.
     */
    private static void checkNotNull(Object object, String message) throws BadRequestException {
        if (object == null) {
            throw new BadRequestException(message);
        }
    }

    /**
     * Checks that expression is true, throws {@link BadRequestException} otherwise.
     *
     * <p>Exception uses error message built from error message template and error message parameters.
     */
    private static void checkArgument(boolean expression, String errorMessageTemplate, Object... errorMessageParams)
            throws BadRequestException {
        if (!expression) {
            throw new BadRequestException(format(errorMessageTemplate, errorMessageParams));
        }
    }

    /**
     * Checks that expression is true, throws {@link BadRequestException} otherwise.
     *
     * <p>Exception uses error message built from error message template and error message parameters.
     */
    private static void checkArgument(boolean expression, String errorMessage) throws BadRequestException {
        if (!expression) {
            throw new BadRequestException(errorMessage);
        }
    }
}
