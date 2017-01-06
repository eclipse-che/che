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
package org.eclipse.che.dto.shared;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that contains "rules" for delegation invocation of DTO method to some third party classes. In some case we may need more then
 * just getters and setters in DTO, but there is no common mechanism to generate such implementation for DTO interface. In this case this
 * annotation may help. Here is example of usage:
 * <p/>
 * <ol>
 * <li>
 * DTO interface. It contains getters, setters and with methods and we do nothing special for them. Implementation generated automatically
 * as well for client and server side. But if we want to have more complex method generator doesn't help, for example add some method for
 * getting full name of user.
 * <pre>
 * &#064DTO
 * public interface User {
 *     String getFirstName();
 *
 *     void setFirstName(String firstName);
 *
 *     User withFirstName(String firstName);
 *
 *     String getLastName();
 *
 *     void setLastName(String lastName);
 *
 *     User withLastName(String lastName);
 *
 *     &#064DelegateTo(client = &#064DelegateRule(type = Util.class, method = "fullName"),
 *                 server = &#064DelegateRule(type = Util.class, method = "fullName"))
 *     String getFullName();
 * }
 * </pre>
 * For method {@code getFullName} add annotation {@code &#064DelegateTo}. Annotations may contains different delegate rules for client and
 * server side.
 * <p/>
 * <div>DelegateTo</div>
 * <table border="0" cellpadding="1" cellspacing="0">
 * <tr align="left">
 * <th align="left">Parameter</th>
 * <th align="left">Description</th>
 * </tr>
 * <tr><td>client</td><td>Rules for client</td></tr>
 * <tr><td>server</td><td>Rules for server</td></tr>
 * </table>
 * <div>DelegateRule</div>
 * <table border="0" cellpadding="1" cellspacing="0">
 * <tr align="left">
 * <th align="left">Parameter</th>
 * <th align="left">Description</th>
 * </tr>
 * <tr><td>type</td><td>Class that contains method to delegate method call</td></tr>
 * <tr><td>method</td><td>Name of method</td></tr>
 * </table>
 * </li>
 * <li>
 * Util class
 * <pre>
 * public class Util {
 *     public static String fullName(User user) {
 *         return user.getFirstName() + " " + user.getLastName();
 *     }
 * }
 * </pre>
 * </li>
 * <li>
 * Requirements for methods to delegate DTO methods calls: Method must be public and static. Method must accept DTO interface as first
 * parameter, if DTO method contains other parameters then the delegate method must accept the whole set of DTO method parameters starting
 * from the second position. For example:
 * <p/>
 * DTO method:
 * <pre>
 * &#064DelegateTo(client = &#064DelegateRule(type = Util.class, method = "fullName"),
 *             server = &#064DelegateRule(type = Util.class, method = "fullName"))
 * String getFullNameWithPrefix(String prefix);
 * </pre>
 * <p/>
 * Delegate method:
 * <pre>
 * public static String fullName(User user, String prefix) {
 *         return prefix + " " + user.getFirstName() + " " + user.getLastName();
 * }
 * </pre>
 * </ul>
 * </li>
 * </ol>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DelegateTo {
    DelegateRule client();

    DelegateRule server();
}
