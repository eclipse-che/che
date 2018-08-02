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
package org.eclipse.che.plugin.ceylon.ide;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.Messages.DefaultMessage;

/**
 * Localization constants. Interface to represent the constants defined in resource bundle:
 * 'CeylonLocalizationConstant.properties'.
 *
 * @author David Festal
 */
public interface CeylonLocalizationConstant extends Messages {
  @Key("ceylon.action.create.file.title")
  @DefaultMessage("New Ceylon File")
  String createCeylonFileActionTitle();

  @Key("ceylon.action.create.file.description")
  @DefaultMessage("Create a Ceylon file")
  String createCeylonFileActionDescription();
}
