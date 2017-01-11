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
 * Helps to inform client about valid values of request parameters.
 * <p/>
 * This annotation may be applied to parameter of RESTful method annotated with {@link javax.ws.rs.QueryParam &#64;QueryParam}. In this
 * case value of this annotation is copied to field {@link org.eclipse.che.api.core.rest.shared.dto.LinkParameter#getValid()}
 * <p/>
 * For example: There is EchoService. Let's see on the value of Valid annotation, it is {"hello", "goodbye"}.
 * <pre>
 * &#064Path("echo")
 * &#064Description("echo service")
 * public class EchoService extends Service {
 *
 *     &#064GenerateLink(rel = "message")
 *     &#064GET
 *     &#064Path("say")
 *     &#064Produces("plain/text")
 *     public String echo1(&#064Required &#064Valid({"hello", "goodbye"}) &#064QueryParam("message") String message) {
 *         return message;
 *     }
 * }
 * </pre>
 * <p/>
 * Request to URL '${base_uri}/echo' gets next output:
 * <p/>
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
 *       "method":HttpMethod.GET,
 *       "parameters":[
 *         {
 *           "name":"message",
 *           "type":"String",
 *           "required":true,
 *           "valid":[
 *             "hello",
 *             "goodbye"
 *           ]
 *         }
 *       ]
 *     }
 *   ]
 * }
 * </pre>
 * There are two "valid" values for parameter "message": ["hello", "goodbye"] in JSON output.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @see org.eclipse.che.api.core.rest.shared.dto.LinkParameter
 * @see org.eclipse.che.api.core.rest.shared.dto.RequestBodyDescriptor
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Valid {
    String[] value();
}
