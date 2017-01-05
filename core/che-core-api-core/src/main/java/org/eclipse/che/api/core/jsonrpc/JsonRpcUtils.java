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
package org.eclipse.che.api.core.jsonrpc;

import com.google.gson.JsonElement;

import org.eclipse.che.dto.server.DtoFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple utility class
 */
class JsonRpcUtils {

    @SuppressWarnings("unchecked")
    static <T> T cast(Object object) {
        return (T)object;
    }

    static <T> T getAs(JsonElement element, Class<T> type, DtoFactory dtoFactory) {
        if (type.equals(String.class)) {
            return cast(element.getAsString());
        } else if (type.equals(Double.class)) {
            return cast(element.getAsDouble());
        } else if (type.equals(Boolean.class)) {
            return cast(element.getAsBoolean());
        } else if (type.equals(Void.class)) {
            return null;
        } else {
            return dtoFactory.createDtoFromJson(element.toString(), type);
        }
    }

    static <T> List<T> getAsListOf(List<JsonElement> elements, Class<T> type, DtoFactory dtoFactory) {
        return elements.stream().map(it -> getAs(it, type, dtoFactory)).collect(Collectors.toList());
    }
}
