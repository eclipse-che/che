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
package org.eclipse.che.ide.editor.orion.client.jso;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * The "Service attributes" for registering orion 'orion.core.contenttype' service. See <a
 * href="https://wiki.eclipse.org/Orion/Documentation/Developer_Guide/Plugging_into_the_editor#orion.core.contenttype">Orion
 * documentation</a> for details.
 *
 * @author Sven Efftinge
 */
public class OrionContentTypeOverlay extends JavaScriptObject {

  protected OrionContentTypeOverlay() {}

  public static final native OrionContentTypeOverlay create() /*-{
        return {};
    }-*/;

  public final native JavaScriptObject toServiceObject() /*-{
        return { contentTypes : [this] };
    }-*/;

  public final native void setId(String theId) /*-{
        this.id = theId;
    }-*/;

  public final native void setName(String theName) /*-{
        this.name = theName;
    }-*/;

  public final void setExtension(String... fileExtensions) {
    JsArrayString arr = JavaScriptObject.createArray().cast();
    for (String value : fileExtensions) {
      arr.push(value);
    }
    setExtension(arr);
  }

  public final void setFileName(String... fileNames) {
    JsArrayString arr = JavaScriptObject.createArray().cast();
    for (String value : fileNames) {
      arr.push(value);
    }
    setFileName(arr);
  }

  public final native void setExtension(JsArrayString fileExtensions) /*-{
        this.extension = fileExtensions;
    }-*/;

  public final native void setFileName(JsArrayString fileNames) /*-{
        this.fileName = fileNames;
    }-*/;

  public final native void setExtends(String parentContentType) /*-{
        this["extends"] = parentContentType;
    }-*/;

  public final native void setImage(String imageURL) /*-{
        this.image = imageURL;
    }-*/;

  public final native void setImageClass(String theImageClass) /*-{
        this.imageClass = theImageClass;
    }-*/;

  public final native String getId() /*-{
        return this.id;
    }-*/;

  public final native String getName() /*-{
        return this.name;
    }-*/;

  public final native JsArrayString getExtensions() /*-{
        return this.extension;
    }-*/;
}
