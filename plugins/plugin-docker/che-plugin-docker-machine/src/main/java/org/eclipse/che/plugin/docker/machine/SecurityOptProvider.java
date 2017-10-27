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
package org.eclipse.che.plugin.docker.machine;

import static com.google.common.base.Strings.isNullOrEmpty;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Injects docker SecurityOpt configuration and ensures that it is neither empty array nor single
 * value array with null or empty string.
 *
 * @author Hanno Kolvenbach
 */
@Singleton
public class SecurityOptProvider implements Provider<String[]> {
  private final String[] securityOpt;

  @Inject
  public SecurityOptProvider(@Nullable @Named("che.docker.securityopt") String[] securityOpt) {
    if (securityOpt == null
        || securityOpt.length == 0
        || (securityOpt.length == 1 && isNullOrEmpty(securityOpt[0]))) {
      this.securityOpt = null;
    } else {
      this.securityOpt = securityOpt;
    }
  }

  @Nullable
  @Override
  public String[] get() {
    return securityOpt;
  }
}
