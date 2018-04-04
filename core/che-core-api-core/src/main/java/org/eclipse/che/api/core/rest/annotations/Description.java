/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.rest.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description of service or its parameters.
 *
 * <p>It may be applied to:
 *
 * <ul>
 *   <li>sub-classes of {@link org.eclipse.che.api.core.rest.Service Service}. In this case value of
 *       this annotation is copied to the field {@link
 *       org.eclipse.che.api.core.rest.shared.dto.ServiceDescriptor#getDescription()}
 *   <li>parameter of RESTful method annotated with {@link javax.ws.rs.QueryParam &#64;QueryParam}.
 *       In this case value of this annotation is copied to the field {@link
 *       org.eclipse.che.api.core.rest.shared.dto.LinkParameter#getDescription()}
 *   <li>entity parameter (not annotated with JAX-RS annotations) of RESTful method. Entity
 *       parameters are described in section 3.3.2.1 of JAX-RS specification 1.0. In this case value
 *       of this annotation is copied to the filed of {@link
 *       org.eclipse.che.api.core.rest.shared.dto.RequestBodyDescriptor#getDescription()}
 * </ul>
 *
 * <p>For example: There is EchoService. Let's see on the values of Description annotations. Here we
 * have two: at class and at method's parameter.
 *
 * <pre>
 * &#064Path("echo")
 * &#064Description("echo service")
 * public class EchoService extends Service {
 *
 *     &#064GenerateLink(rel = "message")
 *     &#064GET
 *     &#064Path("say")
 *     &#064Produces("plain/text")
 *     public String echo1(&#064Required &#064Description("echo message") &#064QueryParam("message") String message) {
 *         return message;
 *     }
 * }
 * </pre>
 *
 * <p>Request to URL '${base_uri}/echo' gets next output:
 *
 * <p>
 *
 * <pre>
 * {
 *   "description":"echo service",
 *   "version":"1.0",
 *   "href":"${base_uri}/echo",
 *   "links":[
 *     {
 *       "href":"${base_uri}/echo/say",
 *       "produces":"plain/text",
 *       "rel":"message",
 *       "method":"GET",
 *       "parameters":[
 *         {
 *           "name":"message",
 *           "type":"String",
 *           "required":true,
 *           "description":"echo message"
 *         }
 *       ]
 *     }
 *   ]
 * }
 * </pre>
 *
 * See two descriptions in JSON output.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @see org.eclipse.che.api.core.rest.shared.dto.ServiceDescriptor
 * @see org.eclipse.che.api.core.rest.shared.dto.LinkParameter
 * @see org.eclipse.che.api.core.rest.shared.dto.RequestBodyDescriptor
 */
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Description {
  /** @return the description */
  String value();
}
