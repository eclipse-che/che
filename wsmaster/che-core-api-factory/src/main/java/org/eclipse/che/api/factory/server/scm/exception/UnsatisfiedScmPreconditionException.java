/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.factory.server.scm.exception;

/**
 * Exception for the case when one of the precondition are not met. For example at least one k8s
 * namespace exists.
 */
public class UnsatisfiedScmPreconditionException extends Exception {
  public UnsatisfiedScmPreconditionException(String message) {
    super(message);
  }
}
