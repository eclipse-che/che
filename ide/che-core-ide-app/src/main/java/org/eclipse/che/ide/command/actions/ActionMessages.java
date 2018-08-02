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
package org.eclipse.che.ide.command.actions;

import com.google.gwt.i18n.client.Messages;

public interface ActionMessages extends Messages {
  @Key("rename.command.action.title")
  String renameCommandActionTitle();

  @Key("rename.command.action.description")
  String renameCommandActionDescription();

  @Key("move.command.action.title")
  String moveCommandActionTitle();

  @Key("move.command.action.description")
  String moveCommandActionDescription();
}
