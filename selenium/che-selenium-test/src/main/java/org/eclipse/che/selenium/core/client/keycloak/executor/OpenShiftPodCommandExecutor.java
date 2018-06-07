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
package org.eclipse.che.selenium.core.client.keycloak.executor;

import static java.lang.String.format;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.eclipse.che.selenium.core.provider.OpenShiftWebConsoleUrlProvider;
import org.eclipse.che.selenium.core.utils.process.ProcessAgent;
import org.eclipse.che.selenium.core.utils.process.ProcessAgentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Dmytro Nochevnov */
@Singleton
public class OpenShiftPodCommandExecutor implements KeycloakCommandExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(OpenShiftPodCommandExecutor.class);

  private static final boolean IS_MAC_OS =
      System.getProperty("os.name").toLowerCase().startsWith("mac");
  private static final String DEFAULT_OPENSHIFT_USERNAME = "developer";
  private static final String DEFAULT_OPENSHIFT_PASSWORD = "any";

  private static final Path PATH_TO_OPENSHIFT_CLI_DIRECTORY =
      Paths.get(System.getProperty("java.io.tmpdir"));

  private static final Path PATH_TO_OPENSHIFT_CLI = PATH_TO_OPENSHIFT_CLI_DIRECTORY.resolve("oc");
  public static final String ECLIPSE_CHE_NAMESPACE = "eclipse-che";

  private String keycloakPodName;

  @Inject private ProcessAgent processAgent;

  @Inject(optional = true)
  @Named("openshift.username")
  private String openShiftUsername;

  @Inject(optional = true)
  @Named("openshift.password")
  private String openShiftPassword;

  @Inject private OpenShiftWebConsoleUrlProvider openShiftWebConsoleUrlProvider;

  @Override
  public String execute(String command) throws IOException {
    if (keycloakPodName == null || keycloakPodName.trim().isEmpty()) {
      obtainKeycloakPodName();
    }

    String openShiftCliCommand =
        format(
            "%s exec %s -- /opt/jboss/keycloak/bin/kcadm.sh %s",
            PATH_TO_OPENSHIFT_CLI, keycloakPodName, command);

    return processAgent.process(openShiftCliCommand);
  }

  private void obtainKeycloakPodName() throws IOException {
    if (Files.notExists(PATH_TO_OPENSHIFT_CLI)) {
      downloadOpenShiftCLI();
    }

    reLoginToOpenShift();

    // obtain name of keycloak pod
    keycloakPodName =
        processAgent.process(
            format(
                "%s get pod --namespace=%s -l app=keycloak --no-headers | awk '{print $1}'",
                PATH_TO_OPENSHIFT_CLI, ECLIPSE_CHE_NAMESPACE));

    if (keycloakPodName.trim().isEmpty()) {
      throw new RuntimeException(
          format(
              "Keycloak pod is not found at namespace %s at OpenShift instance %s.",
              ECLIPSE_CHE_NAMESPACE, openShiftWebConsoleUrlProvider.get()));
    }
  }

  private void downloadOpenShiftCLI() throws IOException {
    if (Files.notExists(PATH_TO_OPENSHIFT_CLI_DIRECTORY)) {
      Files.createDirectory(PATH_TO_OPENSHIFT_CLI_DIRECTORY);
    }

    URL url;
    File packagePath;
    String commandToUnpackOpenShiftCli;
    if (IS_MAC_OS) {
      url =
          new URL(
              "https://github.com/openshift/origin/releases/download/v3.9.0/openshift-origin-client-tools-v3.9.0-191fece-mac.zip");
      packagePath =
          PATH_TO_OPENSHIFT_CLI_DIRECTORY.resolve("openshift-origin-client-tools.zip").toFile();
      commandToUnpackOpenShiftCli =
          format("unzip -d %s %s", PATH_TO_OPENSHIFT_CLI_DIRECTORY, packagePath);
    } else {
      url =
          new URL(
              "https://github.com/openshift/origin/releases/download/v3.9.0/openshift-origin-client-tools-v3.9.0-191fece-linux-64bit.tar.gz");
      packagePath =
          PATH_TO_OPENSHIFT_CLI_DIRECTORY.resolve("openshift-origin-client-tools.tar.gz").toFile();
      commandToUnpackOpenShiftCli =
          format("tar --strip 1 -xzf %s -C %s", packagePath, PATH_TO_OPENSHIFT_CLI_DIRECTORY);
    }

    LOG.info("Downloading OpenShift CLI from {} ...", url);
    FileUtils.copyURLToFile(url, packagePath);
    LOG.info("OpenShift CLI has been downloaded.");

    processAgent.process(commandToUnpackOpenShiftCli);

    FileUtils.deleteQuietly(packagePath);
  }

  private void reLoginToOpenShift() throws ProcessAgentException {
    String reLoginToOpenShiftCliCommand =
        format(
            "%1$s logout && %1$s login --server=%2$s -u=%3$s -p=%4$s --insecure-skip-tls-verify",
            PATH_TO_OPENSHIFT_CLI,
            openShiftWebConsoleUrlProvider.get(),
            openShiftUsername != null ? openShiftUsername : DEFAULT_OPENSHIFT_USERNAME,
            openShiftPassword != null ? openShiftPassword : DEFAULT_OPENSHIFT_PASSWORD);

    processAgent.process(reLoginToOpenShiftCliCommand);
  }
}
