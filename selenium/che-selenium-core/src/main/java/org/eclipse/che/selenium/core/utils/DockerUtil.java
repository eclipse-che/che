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
package org.eclipse.che.selenium.core.utils;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.PREPARING_WS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import javax.inject.Singleton;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Dmytro Nochevnov */
@Singleton
public class DockerUtil {
  private static final Logger LOG = LoggerFactory.getLogger(DockerUtil.class);

  @Inject
  @Named("che.host")
  private String cheHost;

  public boolean isCheRunLocally() {
    ListLineConsumer outputConsumer = new ListLineConsumer();
    String[] commandLine = {
      "bash",
      "-c",
      String.format(
          "[[ $(docker run --rm --net host eclipse/che-ip:nightly) == '%s' ]] && echo true",
          cheHost)
    };

    try {
      ProcessUtil.executeAndWait(commandLine, PREPARING_WS_TIMEOUT_SEC, SECONDS, outputConsumer);
      if (outputConsumer.getText().equals("true")) {
        return true;
      }
    } catch (InterruptedException | IOException | TimeoutException e) {
      LOG.warn("Can't check if Eclipse Che run locally. Error: {}", outputConsumer.getText(), e);
    }

    return false;
  }
}
