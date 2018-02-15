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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import javax.inject.Singleton;
import org.eclipse.che.selenium.core.utils.process.ProcessAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Dmytro Nochevnov */
@Singleton
public class DockerUtil {
  private static final Logger LOG = LoggerFactory.getLogger(DockerUtil.class);

  @Inject
  @Named("che.host")
  private String cheHost;

  @Inject private ProcessAgent processAgent;

  public boolean isCheRunLocally() {
    String command =
        String.format(
            "[[ $(docker run --rm --net host eclipse/che-ip:nightly) == '%s' ]] && echo true",
            cheHost);

    try {
      String result = processAgent.execute(command);

      if (result != null && result.equals("true")) {
        return true;
      }
    } catch (Exception e) {
      LOG.warn("Can't check if Eclipse Che run locally.", e);
    }

    return false;
  }
}
