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
package org.eclipse.che.plugin.openshift.client.exception;

import java.io.IOException;

public class OpenShiftException extends IOException {

  public OpenShiftException(String message) {
    super(message);
  }

  public OpenShiftException(Throwable cause) {
    super(cause);
  }

  public OpenShiftException(String message, Throwable cause) {
    super(message, cause);
  }
}
