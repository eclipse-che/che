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
package org.eclipse.che.factory.filter;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Allows to accept the factories by the URLs with old username.
 *
 * @author Anton Korneta
 */
@Singleton
public class RemoveIllegalCharactersFactoryURLFilter implements Filter {

    protected static final String USER_NAME = "user";

    private static final Pattern ILLEGAL_USERNAME_CHARACTERS = Pattern.compile("[^a-zA-Z0-9]");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                                                                                                     ServletException {
        chain.doFilter(new NormalizeFactoryUrlRequestWrapper(((HttpServletRequest)request)), response);
    }

    @Override
    public void destroy() {}

    /** Normalizes each username parameter in factory URLs by removing invalid characters. */
    private class NormalizeFactoryUrlRequestWrapper extends HttpServletRequestWrapper {

        public NormalizeFactoryUrlRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            final Map<String, String[]> params = super.getParameterMap();
            final String[] users = params.get(USER_NAME);
            if (users != null && users.length > 0) {
                // normalize each parameter value that contains invalid username
                for (int i = 0; i < users.length; i++) {
                    users[i] = replaceInvalidCharacters(users[i]);
                }
                final HashMap<String, String[]> normalizeParams = new HashMap<>(params);
                normalizeParams.put(USER_NAME, users);
                return normalizeParams;
            }
            return params;
        }

        @Override
        public String getParameter(String name) {
            final String parameter = super.getParameter(name);
            if (name.equals(USER_NAME) && parameter != null) {
                return replaceInvalidCharacters(parameter);
            }
            return parameter;
        }

        @Override
        public String getQueryString() {
            String query = super.getQueryString();
            if (query != null) {
                // normalize each query value that contains invalid username
                final String[] queryParams = query.split("&");
                final String queryParamName = USER_NAME + '=';
                for (String param : queryParams) {
                    if (param.contains(queryParamName)) {
                        final String user = param.substring(queryParamName.length(), param.length());
                        query = query.replaceFirst(param, param.replace(user, replaceInvalidCharacters(user)));
                    }
                }
            }
            return query;
        }

        private String replaceInvalidCharacters(String username) {
            return ILLEGAL_USERNAME_CHARACTERS.matcher(username).replaceAll("");
        }
    }
}
