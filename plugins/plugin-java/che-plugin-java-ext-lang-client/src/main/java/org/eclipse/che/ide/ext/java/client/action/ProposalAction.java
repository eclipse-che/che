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
package org.eclipse.che.ide.ext.java.client.action;

/**
 * This interface is a general type for all completion proposal actions. You must implement the
 * interface when you add new completion action.
 *
 * @author Valeriy Svydenko
 */
public interface ProposalAction {
  /** Implement this method to provide proposal action handler */
  void performAsProposal();

  /** Returns action id */
  String getId();
}
