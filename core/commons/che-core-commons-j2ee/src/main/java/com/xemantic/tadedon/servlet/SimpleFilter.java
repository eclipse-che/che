/*
 * Copyright 2010 Xemantic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xemantic.tadedon.servlet;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

/**
 * Servlet {@link Filter} which does not require initialization and clean up.
 *
 * <p>Created on Aug 6, 2010
 *
 * @author hshsce
 */
public abstract class SimpleFilter implements Filter {

  /** {@inheritDoc} */
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    /* nothing to do */
  }

  /** {@inheritDoc} */
  @Override
  public void destroy() {
    /* nothing to do */
  }
}
