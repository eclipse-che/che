/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui;

import com.google.gwt.i18n.client.Messages;

/**
 * I18n messages interface for the 'CodenvyUI' module.
 *
 * @author Vitaly Parfonov
 * @author Artem Zatsarynnyi
 */
public interface UILocalizationConstant extends Messages {

  @DefaultMessage("OK")
  String okButtonText();

  @DefaultMessage("Cancel")
  String cancelButtonText();

  @DefaultMessage("Value is not valid")
  String validationErrorMessage();
}
