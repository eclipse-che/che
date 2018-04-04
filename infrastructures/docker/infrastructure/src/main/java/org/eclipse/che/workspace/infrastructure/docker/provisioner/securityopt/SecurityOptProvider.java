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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.securityopt;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.emptyList;

import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Injects SecurityOpt configuration values and ensures that it is neither empty array nor single
 * value array with null or empty string.
 *
 * @author Hanno Kolvenbach
 */
@Singleton
public class SecurityOptProvider implements Provider<List<String>> {
  private final List<String> securityopt;

  @Inject
  public SecurityOptProvider(@Nullable @Named("che.docker.securityopt") String[] securityopt) {
    if (securityopt == null
        || securityopt.length == 0
        || (securityopt.length == 1 && isNullOrEmpty(securityopt[0]))) {
      this.securityopt = emptyList();
    } else {
      this.securityopt = Arrays.asList(securityopt);
    }
  }

  @Nullable
  @Override
  public List<String> get() {
    return securityopt;
  }
}
