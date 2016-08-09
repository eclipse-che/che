package org.eclipse.che.plugin.embedjsexample.ide.view;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

/**
 * @author Mathias Schaefer <mathias.schaefer@eclipsesource.com>
 */
public interface HelloWorldView extends View<HelloWorldView.ActionDelegate> {

    interface ActionDelegate extends BaseActionDelegate {
    }

    void sayHello(String content);

    void setVisible(boolean visible);
}
