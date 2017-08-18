/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.installer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import org.eclipse.che.api.installer.server.model.impl.BasicInstaller;
import org.eclipse.che.api.installer.shared.model.Installer;

/**
 * Unison installer.
 *
 * @author Anatolii Bazko
 * @see Installer
 */
@Singleton
public class UnisonInstaller extends BasicInstaller {
  private static final String AGENT_DESCRIPTOR = "org.eclipse.che.unison.json";
  private static final String AGENT_SCRIPT = "org.eclipse.che.unison.script.sh";

  @Inject
  public UnisonInstaller() throws IOException {
    super(AGENT_DESCRIPTOR, AGENT_SCRIPT);
  }
}
