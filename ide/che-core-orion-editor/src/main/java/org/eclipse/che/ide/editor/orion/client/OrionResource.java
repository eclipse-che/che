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
package org.eclipse.che.ide.editor.orion.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * The resource interface for the Orion extension.
 *
 * @author Artem Zatsarynnyi
 */
public interface OrionResource extends ClientBundle {

  @Source({"orion-codenvy-theme.css", "org/eclipse/che/ide/api/ui/style.css"})
  CssResource editorStyle();

  @Source({"incremental-find-container.css", "org/eclipse/che/ide/api/ui/style.css"})
  IncrementalFindResources getIncrementalFindStyle();

  interface IncrementalFindResources extends CssResource {
    String incrementalFindContainer();

    String incrementalFindError();
  }
}
