/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.helloworld.client.action;

/**
 * As usual, importing resources, related to Action API.
 * The 3rd import is required to call a default alert box.
 */
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import com.codenvy.ide.ext.helloworld.client.Resource;
import org.eclipse.che.ide.rest.AsyncRequest;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

public class HelloWorldAction extends Action
{
    /**
     * Define a constructor and pass over text to be displayed in the dialogue box
     */

    final static Resource resource = GWT.create(Resource.class); 
    
    @Inject
    public HelloWorldAction() {
      super(resource.hello());
    }

    /**
     * Getting previously registered server side component and adding  text input to it (asking to enter name). To get a server side
     * component a path is provided which is /api/ComponentName
     */

    @Override
    public void actionPerformed(ActionEvent arg0) {
        String name = Window.prompt("What's your name?", "");
        AsyncRequest.build(RequestBuilder.GET, "/api/hello/" + name).send(new AsyncRequestCallback<String>(new StringUnmarshaller()) {
            protected void onSuccess(String answer) {
                
                  Window.alert(answer);
            };
            protected void onFailure(Throwable arg0) {};
        });
      
    }
}
