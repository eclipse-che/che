/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.clipboard;

import com.google.gwt.inject.client.AbstractGinModule;
import org.eclipse.che.ide.ui.zeroclipboard.ClipboardButtonBuilder;
import org.eclipse.che.ide.ui.zeroclipboard.ClipboardButtonBuilderImpl;

/** GIN module for configuring components related to clipboard support. */
public class ClipboardModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(ClipboardButtonBuilder.class).to(ClipboardButtonBuilderImpl.class);
  }
}
