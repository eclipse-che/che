/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.api.testing.shared.common;

/**
 * Test result status enum.
 *
 * @author Bartlomiej Laczkowski
 */
public enum TestResultStatus {
  SUCCESS,
  SKIPPED,
  WARNING,
  FAILURE,
  ERROR;
}
