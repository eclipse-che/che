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
package org.eclipse.che.ide.orion.compare.jso;

import com.google.gwt.core.client.JavaScriptObject;
import org.eclipse.che.ide.orion.compare.FileOptions;

/** @author Evgen Vidolob */
public class FileOptionsJs extends JavaScriptObject implements FileOptions {
  protected FileOptionsJs() {}

  @Override
  public final native void setContent(String content) /*-{
        this.Content = content;
    }-*/;

  @Override
  public final native void setName(String name) /*-{
        this.Name = name;
    }-*/;

  @Override
  public final native void setReadOnly(boolean readOnly) /*-{
        this.readonly = readOnly;
    }-*/;
}
