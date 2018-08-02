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
package org.eclipse.che.selenium.core.executor;

import static java.lang.String.format;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import org.eclipse.che.selenium.core.utils.executor.CommandExecutor;
import org.eclipse.che.selenium.core.utils.process.ProcessAgent;

/**
 * This class is aimed to call Docker CLI command.
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class DockerCliCommandExecutor implements CommandExecutor {

  private final ProcessAgent processAgent;

  @Inject
  public DockerCliCommandExecutor(ProcessAgent processAgent) {
    this.processAgent = processAgent;
  }

  @Override
  public String execute(String command) throws IOException {
    String dockerCommand = format("docker %s", command);
    return processAgent.process(dockerCommand);
  }
}
