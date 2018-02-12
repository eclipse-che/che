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
