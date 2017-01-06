/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.local;

import com.google.inject.Singleton;

import org.everrest.core.servlet.EverrestServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Has additional {@link Singleton} annotation which required for web components
 * by guice container. Since we want to have possibility to deploy servlet via
 * {@link com.google.inject.servlet.ServletModule#configureServlets
 * ServletModule.configureServlets} .
 *
 * @author andrew00x
 */
@SuppressWarnings("serial")
@Singleton
public final class CheGuiceEverrestServlet extends EverrestServlet {
    @Override
    public void service(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, ServletException {
        super.service(new HttpServletRequestWrapper(httpRequest){

            @Override
            public String getServletPath() {
                return "/api";
            }
        }, httpResponse);
    }
}
