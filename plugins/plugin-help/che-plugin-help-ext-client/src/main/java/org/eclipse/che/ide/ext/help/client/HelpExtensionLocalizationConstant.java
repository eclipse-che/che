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
package org.eclipse.che.ide.ext.help.client;

import com.google.gwt.i18n.client.Messages;

/** @author Andrey Plotnikov */
public interface HelpExtensionLocalizationConstant extends Messages {

  /* Redirect Actions */
  @Key("action.redirect.to.support.title")
  String actionRedirectToSupportTitle();

  @Key("action.redirect.to.support.description")
  String actionRedirectToSupportDescription();

  /* Buttons */
  @Key("ok")
  String ok();
}
