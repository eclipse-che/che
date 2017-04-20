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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.dto.server.DtoFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.api.core.jsonrpc.JsonRpcUtils.cast;

/**
 * Represents JSON RPC params object. Can be constructed out of
 * stringified json object or by passing specific parameters.
 * Use {@link JsonRpcFactory#createParams(Object)},
 * {@link JsonRpcFactory#createParams(String)} to get an instance.
 */
public class JsonRpcParams {
    private final JsonParser jsonParser;

    private List<Param<?>> params;
    private Param<?>       param;

    @AssistedInject
    public JsonRpcParams(@Assisted("message") String message, JsonParser jsonParser) {
        this.jsonParser = jsonParser;

        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");

        JsonElement jsonElement = jsonParser.parse(message);
        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonParser.parse(message).getAsJsonArray();
            this.params = new ArrayList<>(jsonArray.size());
            for (JsonElement element : jsonArray) {
                if (element.isJsonPrimitive()) {
                    JsonPrimitive primitiveElement = element.getAsJsonPrimitive();
                    Param<?> paramCandidate;
                    if (primitiveElement.isBoolean()) {
                        paramCandidate = new Param<>(Boolean.class, primitiveElement.getAsBoolean());
                    } else if (primitiveElement.isNumber()) {
                        paramCandidate = new Param<>(Double.class, primitiveElement.getAsDouble());
                    } else {
                        paramCandidate = new Param<>(String.class, primitiveElement.getAsString());
                    }
                    this.params.add(paramCandidate);
                } else {
                    this.params.add(new Param<>(Object.class, element.getAsJsonObject()));
                }
            }

            this.param = null;
        } else if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive primitiveElement = jsonElement.getAsJsonPrimitive();
            if (primitiveElement.isBoolean()) {
                this.param = new Param<>(Boolean.class, primitiveElement.getAsBoolean());
            } else if (primitiveElement.isNumber()) {
                this.param = new Param<>(Double.class, primitiveElement.getAsDouble());
            } else {
                this.param = new Param<>(String.class, primitiveElement.getAsString());
            }

            this.params = null;
        } else if (jsonElement.isJsonObject()) {
            this.param = new Param<>(Object.class, jsonElement.getAsJsonObject());

            this.params = null;
        } else {
            this.params = null;
            this.param = null;
        }
    }


    @AssistedInject
    public JsonRpcParams(JsonParser jsonParser, @Assisted("params") Object params) {
        this.jsonParser = jsonParser;

        if (params == null) {
            this.params = null;
            this.param = null;
        } else {
            if (params instanceof List) {
                List<?> listParams = (List<?>)params;
                this.params = new ArrayList<>(listParams.size());

                for (Object param : listParams) {
                    Param<?> paramCandidate;
                    if (param instanceof Boolean) {
                        paramCandidate = new Param<>(Boolean.class, (Boolean)param);
                    } else if (param instanceof String) {
                        paramCandidate = new Param<>(String.class, (String)param);
                    } else if (param instanceof Double) {
                        paramCandidate = new Param<>(Double.class, (Double)param);
                    } else {
                        paramCandidate = new Param<>(Object.class, param);
                    }
                    this.params.add(paramCandidate);
                }

                this.param = null;
            } else {
                Param<?> paramCandidate;
                if (params instanceof Boolean) {
                    this.param = new Param<>(Boolean.class, (Boolean)params);
                } else if (params instanceof String) {
                    this.param = new Param<>(String.class, (String)params);
                } else if (params instanceof Double) {
                    this.param = new Param<>(Double.class, (Double)params);
                } else {
                    this.param = new Param<>(Object.class, params);
                }

                this.params = null;
            }
        }
    }

    public boolean emptyOrAbsent() {
        return (params == null || params.isEmpty()) && (param == null || jsonParser.parse("{}").equals(param.value));
    }

    public JsonElement toJsonElement() {
        if (param != null) {
            if (param.type.equals(Object.class)) {
                return jsonParser.parse(param.value.toString());
            } else if (param.type.equals(String.class)) {
                return new JsonPrimitive((String)param.value);
            } else if (param.type.equals(Boolean.class)) {
                return new JsonPrimitive((Boolean)param.value);
            } else if (param.type.equals(Double.class)) {
                return new JsonPrimitive((Double)param.value);
            }
        }

        JsonArray array = new JsonArray();
        for (Param<?> paramCandidate : params) {
            JsonElement element;
            if (paramCandidate.type.equals(Object.class)) {
                element = jsonParser.parse(paramCandidate.value.toString());
            } else if (paramCandidate.type.equals(String.class)) {
                element = new JsonPrimitive((String)paramCandidate.value);
            } else if (paramCandidate.type.equals(Boolean.class)) {
                element = new JsonPrimitive((Boolean)paramCandidate.value);
            } else {
                element = new JsonPrimitive((Double)paramCandidate.value);
            }
            array.add(element);
        }
        return array;
    }

    public <T> T getAs(Class<T> type) {
        checkNotNull(type, "Type must not be null");
        checkNotNull(param, "Param must not be null");
        checkState(type.equals(param.type), "Types should match");

        if (param.type.equals(Object.class)) {
            return DtoFactory.getInstance().createDtoFromJson(param.value.toString(), type);
        } else {
            return cast(param.value);
        }
    }

    public <T> List<T> getAsListOf(Class<T> type) {
        checkNotNull(type, "Type must not be null");

        return params.stream().map(it -> it.value).collect(cast(Collectors.toList()));
    }

    @Override
    public String toString() {
        return toJsonElement().toString();
    }

    private class Param<T> {
        final private Class<T> type;
        final private T        value;

        private Param(Class<T> type, T value) {
            this.type = type;
            this.value = value;
        }
    }
}
