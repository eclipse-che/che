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
package org.eclipse.che.api.core.rest.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation may be applied to methods of sub-class of {@link org.eclipse.che.api.core.rest.Service}. When client accesses method {@link
 * org.eclipse.che.api.core.rest.Service#getServiceDescriptor()} all methods with this annotation are analyzed to provide descriptor of
 * particular service. In instance of {@link org.eclipse.che.api.core.rest.shared.dto.ServiceDescriptor ServiceDescriptor} methods with this annotation
 * are represented as set of links. It should help client to understand capabilities of particular RESTful service.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @see org.eclipse.che.api.core.rest.Service
 * @see org.eclipse.che.api.core.rest.shared.dto.ServiceDescriptor
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GenerateLink {
    String rel();
}
