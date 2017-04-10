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
package org.eclipse.che.ide.jsonrpc;

import elemental.json.JsonArray;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import elemental.json.JsonType;
import elemental.json.JsonValue;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.ide.dto.DtoFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents JSON RPC params object. Can be constructed out of
 * stringified json object or by passing specific parameters.
 * Use {@link JsonRpcFactory#createParams(Object)},
 * {@link JsonRpcFactory#createParamsList(List)} or
 * {@link JsonRpcFactory#createParams(String)} to get an instance.
 */
public class JsonRpcParams {
    private final JsonFactory jsonFactory;
    private final DtoFactory  dtoFactory;

    private List<Param<?>> params;
    private Param<?>       param;

    @AssistedInject
    public JsonRpcParams(@Assisted("message") String message, JsonFactory jsonFactory, DtoFactory dtoFactory) {
        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");

        this.jsonFactory = jsonFactory;
        this.dtoFactory = dtoFactory;

        JsonValue jsonValue = jsonFactory.parse(message);
        if (jsonValue.getType().equals(JsonType.ARRAY)) {
            JsonArray jsonArray = jsonFactory.parse(message);
            this.params = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JsonValue element = jsonArray.get(i);
                Param<?> paramCandidate;
                if (element.getType().equals(JsonType.STRING)) {
                    paramCandidate = new Param<>(String.class, element.asString());
                } else if (element.getType().equals(JsonType.BOOLEAN)) {
                    paramCandidate = new Param<>(Boolean.class, element.asBoolean());
                } else if (element.getType().equals(JsonType.NUMBER)) {
                    paramCandidate = new Param<>(Double.class, element.asNumber());
                } else {
                    paramCandidate = new Param<>(Object.class, element);
                }
                this.params.add(paramCandidate);
            }
            this.param = null;
        } else if (jsonValue.getType().equals(JsonType.STRING)) {
            this.param = new Param<>(String.class, jsonValue.asString());
            this.params = null;
        } else if (jsonValue.getType().equals(JsonType.BOOLEAN)) {
            this.param = new Param<>(Boolean.class, jsonValue.asBoolean());
            this.params = null;
        } else if (jsonValue.getType().equals(JsonType.NUMBER)) {
            this.param = new Param<>(Double.class, jsonValue.asNumber());
            this.params = null;
        } else if (jsonValue.getType().equals(JsonType.OBJECT)) {
            this.param = new Param<>(Object.class, jsonValue);
            this.params = null;
        } else {
            this.param = null;
            this.params = null;
        }
    }

    @AssistedInject
    public JsonRpcParams(@Assisted("params") Object params, DtoFactory dtoFactory, JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
        this.dtoFactory = dtoFactory;

        if (params == null) {
            this.param = null;
            this.params = null;
        } else {
            if (params instanceof List) {
                List<?> listParams = (List<?>)params;
                this.params = new ArrayList<>(listParams.size());

                for (Object param : listParams) {
                    Param<?> paramCandidate;
                    if (param instanceof String) {
                        paramCandidate = new Param<>(String.class, (String)param);
                    } else if (param instanceof Double) {
                        paramCandidate = new Param<>(Double.class, (Double)param);
                    } else if (param instanceof Boolean) {
                        paramCandidate = new Param<>(Boolean.class, (Boolean)param);
                    } else {
                        paramCandidate = new Param<>(Object.class, param);
                    }
                    this.params.add(paramCandidate);
                }

                this.param = null;
            } else {
                if (params instanceof String) {
                    this.param = new Param<>(String.class, (String)params);
                } else if (params instanceof Double) {
                    this.param = new Param<>(Double.class, (Double)params);
                } else if (params instanceof Boolean) {
                    this.param = new Param<>(Boolean.class, (Boolean)params);
                } else {
                    this.param = new Param<>(Object.class, params);
                }

                this.params = null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    static <T> T cast(Object object) {
        return (T)object;
    }

    public boolean emptyOrAbsent() {
        return (params == null || params.isEmpty()) &&
               (param == null || (param.value instanceof JsonObject && jsonFactory.createObject().jsEquals((JsonObject)param.value)));
    }

    public JsonValue toJsonValue() {
        if (param != null) {
            if (param.type.equals(String.class)) {
                return jsonFactory.create((String)param.value);
            } else if (param.type.equals(Boolean.class)) {
                return jsonFactory.create((Boolean)param.value);
            } else if (param.type.equals(Double.class)) {
                return jsonFactory.create((Double)param.value);
            } else {
                return jsonFactory.parse(param.value.toString());
            }
        }

        JsonArray array = jsonFactory.createArray();
        for (int i = 0; i < params.size(); i++) {
            Param<?> paramCandidate = params.get(i);
            JsonValue jsonValue;
            if (paramCandidate.type.equals(String.class)) {
                jsonValue = jsonFactory.create((String)paramCandidate.value);
            } else if (paramCandidate.type.equals(Boolean.class)) {
                jsonValue = jsonFactory.create((Boolean)paramCandidate.value);
            } else if (paramCandidate.type.equals(Double.class)) {
                jsonValue = jsonFactory.create((Double)paramCandidate.value);
            } else {
                jsonValue = jsonFactory.parse(paramCandidate.value.toString());
            }

            array.set(i, jsonValue);
        }

        return array;
    }

    public <T> T getAs(Class<T> type) {
        checkNotNull(param, "Type must not be null");

        return param.type.equals(Object.class)
               ? dtoFactory.createDtoFromJson(param.value.toString(), type)
               : cast(param.value);

    }

    public <T> List<T> getAsListOf(Class<T> type) {
        checkNotNull(type, "Type must not be null");

        return params.stream().map(it -> it.value).collect(cast(Collectors.toList()));
    }

    @Override
    public String toString() {
        return toJsonValue().toJson();
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
