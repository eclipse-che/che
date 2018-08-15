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
package org.eclipse.che.plugin.composer.ide;

import com.google.gwt.i18n.client.Messages;

/**
 * Localization constants. Interface to represent the constants defined in resource bundle:
 * 'ComposerLocalizationConstant.properties'.
 *
 * @author Kaloyan Raev
 */
public interface ComposerLocalizationConstant extends Messages {
  @Key("attributes.header.label.name")
  String attributesHeader();

  @Key("package.attribute.label.name")
  String packageAttribute();

  @Key("composer.page.estimate.errorMessage")
  String composerPageEstimateErrorMessage();

  @Key("composer.page.errorDialog.title")
  String composerPageErrorDialogTitle();
}
