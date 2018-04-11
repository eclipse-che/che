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
package org.eclipse.che.multiuser.machine.authentication.server.signature;

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.provision.env.EnvVarProvider;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.multiuser.machine.authentication.shared.Constants;

/**
 * Provides signature algorithm into machine environment.
 *
 * @author Anton Korneta
 */
public class SignatureAlgorithmEnvProvider implements EnvVarProvider {

  private final String algorithm;

  @Inject
  public SignatureAlgorithmEnvProvider(
      @Named("che.auth.signature_key_algorithm") String algorithm) {
    this.algorithm = algorithm;
  }

  @Override
  public Pair<String, String> get(RuntimeIdentity runtimeIdentity) {
    return Pair.of(Constants.SIGNATURE_ALGORITHM_ENV, algorithm);
  }
}
