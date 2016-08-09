package org.eclipse.che.plugin.embedjsexample.ide.view.client.jso;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;

/**
 * JavaScript overlay to demonstrate a global js function call
 *
 * @author Mathias Schaefer <mathias.schaefer@eclipsesource.com>
 */
public class HelloWorldViewOverlay extends JavaScriptObject {

    protected HelloWorldViewOverlay() {
    }

    public final static native void sayHello(final Element element, String message) /*-{
        new $wnd.HelloWorld(element, contents);
    }-*/;

}
