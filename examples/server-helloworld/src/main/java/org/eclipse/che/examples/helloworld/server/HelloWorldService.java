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
package org.eclipse.che.examples.helloworld.server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Register a RESTful server side component
 */

@Path("hello")
public class HelloWorldService {
    
    public HelloWorldService() {

    }
    
    @GET
    @Path("{name}")
    public String sayHello(@PathParam("name") String name) {
       return "Hello " + name + " !";
    }

}
