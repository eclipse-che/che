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
 * Helps to inform client about mandatory parameters of request.
 * <p/>
 * This annotation may be applied to parameter of RESTful method annotated with {@link javax.ws.rs.QueryParam &#64;QueryParam}. In this
 * case field of {@link org.eclipse.che.api.core.rest.shared.dto.LinkParameter#isRequired()} is set to {@code true}.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @see org.eclipse.che.api.core.rest.shared.dto.LinkParameter
 * @see org.eclipse.che.api.core.rest.shared.dto.RequestBodyDescriptor
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Required {
}
