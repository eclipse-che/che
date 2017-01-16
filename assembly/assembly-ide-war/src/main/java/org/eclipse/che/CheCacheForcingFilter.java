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
package org.eclipse.che;

import com.xemantic.tadedon.servlet.CacheForcingFilter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Max Shaposhnik
 */
public class CheCacheForcingFilter extends CacheForcingFilter {

    private Set<Pattern> actionPatterns;

    public void init(FilterConfig filterConfig) {

        Enumeration<String> names = filterConfig.getInitParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (name.startsWith("pattern")) {
                actionPatterns.add(Pattern.compile(filterConfig.getInitParameter(name)));
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            for (Pattern pattern : actionPatterns) {
                if (pattern.matcher(((HttpServletRequest)request).getRequestURL()).matches()) {
                    super.doFilter(request, response, chain);
                }
            }
        } finally {
            chain.doFilter(request, response);
        }
    }
}
