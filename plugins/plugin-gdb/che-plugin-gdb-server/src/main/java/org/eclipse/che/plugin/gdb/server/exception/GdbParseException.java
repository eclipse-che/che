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
package org.eclipse.che.plugin.gdb.server.exception;

import static java.lang.Math.min;

/** @author Anatoliy Bazko */
@SuppressWarnings("serial")
public class GdbParseException extends GdbException {

  public static final int MAX_OUTPUT_LENGTH = 80;

  public GdbParseException(Class clazz, String output) {
    super(
        "Can't parse '"
            + output.substring(0, min(output.length(), MAX_OUTPUT_LENGTH))
            + "' into "
            + clazz.getSimpleName());
  }
}
