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
package org.eclipse.che.multiuser.organization.shared.event;

/**
 * Defines organizations event types.
 *
 * @author Anton Korneta
 */
public enum EventType {

  /** Published when organization name changed. */
  ORGANIZATION_RENAMED,

  /** Published when organization removed. */
  ORGANIZATION_REMOVED,

  /** Published when new member added to organization. */
  MEMBER_ADDED,

  /** Published when member removed from organization. */
  MEMBER_REMOVED
}
