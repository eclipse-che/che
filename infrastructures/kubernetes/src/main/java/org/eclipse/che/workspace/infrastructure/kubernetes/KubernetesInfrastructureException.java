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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import io.fabric8.kubernetes.client.KubernetesClientException;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;

/**
 * An exception thrown by {@link KubernetesInfrastructure} and related components. Indicates that an
 * infrastructure operation can't be performed or an error occurred during operation execution.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesInfrastructureException extends InfrastructureException {
  public KubernetesInfrastructureException(KubernetesClientException e) {
    super(extractMessage(e), e);
  }

  private static String extractMessage(KubernetesClientException e) {
    int code = e.getCode();
    if (code == 401 || code == 403) {
      return e.getMessage()
          + " The error may be caused by an expired token or changed password. "
          + "Update Che server deployment with a new token or password.";
    } else {
      return e.getMessage();
    }
  }
}
