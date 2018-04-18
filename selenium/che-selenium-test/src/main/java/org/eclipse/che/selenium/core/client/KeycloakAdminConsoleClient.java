/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.client;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.CheSeleniumSuiteModule.DOCKER_INFRASTRUCTURE;
import static org.eclipse.che.selenium.core.CheSeleniumSuiteModule.OPENSHIFT_INFRASTRUCTURE;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Singleton;
import org.eclipse.che.selenium.core.provider.AdminTestUserProvider;
import org.eclipse.che.selenium.core.provider.RemovableUserProvider;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.user.TestUserFactory;
import org.eclipse.che.selenium.core.user.TestUserImpl;
import org.eclipse.che.selenium.core.utils.DockerUtil;
import org.eclipse.che.selenium.core.utils.process.ProcessAgent;
import org.eclipse.che.selenium.core.utils.process.ProcessAgentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is java-client of 'keycloak/bin/kcadm.sh' command line application of keycloak container
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class KeycloakAdminConsoleClient {
  private static final Logger LOG = LoggerFactory.getLogger(KeycloakAdminConsoleClient.class);
  private static final Pattern EXTRACT_USER_ID_PATTERN =
      Pattern.compile("^.*Created new user with id '(.*)'.*$", Pattern.DOTALL);

  // we need to inject AdminTestUser separately to avoid circular dependency error
  @Inject private AdminTestUserProvider adminTestUserProvider;

  private final DockerUtil dockerUtil;
  private final TestUserFactory<DefaultTestUser> defaultTestUserFactory;
  private final TestUserFactory<TestUserImpl> testUserFactory;
  private final ProcessAgent processAgent;
  private final String keycloakContainerId;
  private final String cheInfrastructure;

  @Inject
  public KeycloakAdminConsoleClient(
      DockerUtil dockerUtil,
      TestUserFactory<TestUserImpl> testUserFactory,
      TestUserFactory<DefaultTestUser> defaultTestUserFactory,
      ProcessAgent processAgent,
      @Named("che.infrastructure") String cheInfrastructure)
      throws ProcessAgentException {
    this.dockerUtil = dockerUtil;
    this.testUserFactory = testUserFactory;
    this.defaultTestUserFactory = defaultTestUserFactory;
    this.processAgent = processAgent;
    this.cheInfrastructure = cheInfrastructure;

    // obtain id of keycloak docker container
    switch (cheInfrastructure) {
      case OPENSHIFT_INFRASTRUCTURE:
        this.keycloakContainerId =
            processAgent.execute("echo $(docker ps | grep 'keycloak_keycloak-' | cut -d ' ' -f1)");
        break;

      case DOCKER_INFRASTRUCTURE:
      default:
        this.keycloakContainerId =
            processAgent.execute("echo $(docker ps | grep che_keycloak | cut -d ' ' -f1)");
        break;
    }
  }

  public TestUserImpl createUser(RemovableUserProvider testUserProvider) throws IOException {
    if (!dockerUtil.isCheRunLocally()) {
      throw new IOException(
          "It's impossible to create test user because of Che is running on the different host.");
    }

    long currentTimeInMillisec = System.currentTimeMillis();
    String username = "user" + currentTimeInMillisec;
    String email = username + "@1.com";
    String password = String.valueOf(currentTimeInMillisec);

    String userId = doCreateUser(username, email, password);

    LOG.info("Test user with name='{}' and id='{}' has been created.", username, userId);

    return testUserFactory.create(username, email, password, "", testUserProvider);
  }

  public DefaultTestUser createDefaultUser(RemovableUserProvider testUserProvider)
      throws IOException {
    if (!dockerUtil.isCheRunLocally()) {
      throw new IOException(
          "It's impossible to create test user because of Che is running on the different host.");
    }

    long currentTimeInMillisec = System.currentTimeMillis();
    String username = "user" + currentTimeInMillisec;
    String email = username + "@1.com";
    String password = String.valueOf(currentTimeInMillisec);

    String userId = doCreateUser(username, email, password);

    LOG.info("Default test user with name='{}' and id='{}' has been created.", username, userId);

    return defaultTestUserFactory.create(username, email, password, "", testUserProvider);
  }

  private String doCreateUser(String username, String email, String password) throws IOException {
    String authPartOfCommand =
        format(
            "--no-config --server http://localhost:8080/auth --user %s --password %s --realm master",
            adminTestUserProvider.get().getName(), adminTestUserProvider.get().getPassword());

    String createUserCommand =
        format(
            "docker exec -i %s sh -c 'keycloak/bin/kcadm.sh create users -r che -s username=%s -s enabled=true %s 2>&1'",
            keycloakContainerId, username, authPartOfCommand);
    String response = processAgent.execute(createUserCommand);
    if (!response.contains("Created new user with id ")) {
      throw new IOException("Test user creation error: " + response);
    }

    String userId = extractUserId(response);

    try {
      String setTestUsersPermanentPasswordCommand =
          format(
              "docker exec -i %s sh -c 'keycloak/bin/kcadm.sh set-password -r che --username %s --new-password %s %s 2>&1'",
              keycloakContainerId, username, password, authPartOfCommand);
      processAgent.execute(setTestUsersPermanentPasswordCommand);

      String setEmailCommand =
          format(
              "docker exec -i %s sh -c 'keycloak/bin/kcadm.sh update users/%s -r che --set email=%s %s 2>&1'",
              keycloakContainerId, userId, email, authPartOfCommand);
      processAgent.execute(setEmailCommand);

      String addUserRoleToUserCommand =
          format(
              "docker exec -i %s sh -c 'keycloak/bin/kcadm.sh add-roles -r che --uusername %s --rolename user %s 2>&1'",
              keycloakContainerId, username, authPartOfCommand);
      processAgent.execute(addUserRoleToUserCommand);

      String addReadTokenRoleToUserCommand =
          format(
              "docker exec -i %s sh -c 'keycloak/bin/kcadm.sh add-roles -r che --uusername %s --cclientid broker --rolename read-token %s 2>&1'",
              keycloakContainerId, username, authPartOfCommand);
      processAgent.execute(addReadTokenRoleToUserCommand);
    } catch (IOException e) {
      // clean up user
      delete(userId, username);
      throw e;
    }

    return userId;
  }

  /** Adds role "read-token" of client "broker" to admin user */
  public void setupAdmin(AdminTestUser adminTestUser) {
    String authPartOfCommand =
        format(
            "--no-config --server http://localhost:8080/auth --user %s --password %s --realm master",
            adminTestUser.getName(), adminTestUser.getPassword());

    String addReadTokenRoleToUserCommand =
        format(
            "docker exec -i %s sh -c 'keycloak/bin/kcadm.sh add-roles -r che --uusername %s --cclientid broker --rolename read-token %s 2>&1'",
            keycloakContainerId, adminTestUser.getName(), authPartOfCommand);

    try {
      processAgent.execute(addReadTokenRoleToUserCommand);
    } catch (IOException e) {
      // ignore error of adding role to admin because of it can be added before
    }
  }

  public void delete(TestUser testUser) throws IOException {
    delete(testUser.getId(), testUser.getName());
  }

  private String extractUserId(String response) {
    Matcher matcher = EXTRACT_USER_ID_PATTERN.matcher(response);

    if (matcher.find()) {
      return matcher.group(1);
    }

    throw new RuntimeException(
        String.format("User id wasn't found in create user response '%s'.", response));
  }

  private void delete(String userId, String username) throws IOException {
    String commandToDeleteUser =
        format(
            "docker exec -i %s sh -c 'keycloak/bin/kcadm.sh delete users/%s -r che -s username=%s --no-config --server http://localhost:8080/auth --user %s --password %s --realm master 2>&1'",
            keycloakContainerId,
            userId,
            username,
            adminTestUserProvider.get().getName(),
            adminTestUserProvider.get().getPassword());

    processAgent.execute(commandToDeleteUser);

    LOG.info("Test user with name='{}' has been removed.", username);
  }
}
