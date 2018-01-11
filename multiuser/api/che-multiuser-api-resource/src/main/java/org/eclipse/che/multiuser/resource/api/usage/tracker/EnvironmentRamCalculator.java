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
package org.eclipse.che.multiuser.resource.api.usage.tracker;

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
//import org.eclipse.che.api.environment.server.EnvironmentParser;
//import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.commons.lang.Size;

/**
 * Helps to calculate amount of RAM defined in {@link Environment environment}
 *
 * @author Sergii Leschenko
 */
public class EnvironmentRamCalculator {
  private static final long BYTES_TO_MEGABYTES_DIVIDER = 1024L * 1024L;

//  private final EnvironmentParser environmentParser;
  private final long defaultMachineMemorySizeBytes;

  @Inject
  public EnvironmentRamCalculator(
//      EnvironmentParser environmentParser,
      @Named("che.workspace.default_memory_mb") int defaultMachineMemorySizeMB) {
//    this.environmentParser = environmentParser;
    this.defaultMachineMemorySizeBytes = Size.parseSize(defaultMachineMemorySizeMB + "MB");
  }

  /**
   * Parses (and fetches if needed) recipe of environment and sums RAM size of all machines in
   * environment in megabytes.
   */
  public long calculate(Environment environment) throws ServerException {
    /*
    CheServicesEnvironmentImpl composeEnv = environmentParser.parse(environment);

    long sumBytes =
        composeEnv
            .getServices()
            .values()
            .stream()
            .mapToLong(
                value -> {
                  if (value.getMemLimit() == null || value.getMemLimit() == 0) {
                    return defaultMachineMemorySizeBytes;
                  } else {
                    return value.getMemLimit();
                  }
                })
            .sum();
    return sumBytes / BYTES_TO_MEGABYTES_DIVIDER;
    */
    return 0L;
  }
}
