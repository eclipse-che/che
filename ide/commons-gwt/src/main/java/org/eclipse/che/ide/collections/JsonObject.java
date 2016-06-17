// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.eclipse.che.ide.collections;

/** Defines a simple, mutable JSON object. */
public interface JsonObject {

    JsonObject addField(String key, boolean value);

    JsonObject addField(String key, double value);

    JsonObject addField(String key, int value);

    JsonObject addField(String key, Array<?> value);

    JsonObject addField(String key, JsonObject value);

    JsonObject addField(String key, String value);

    boolean getBooleanField(String key);

    int getIntField(String key);

    double getDoubleField(String key);

    Array<String> getKeys();

    JsonObject getObjectField(String key);

    Array<?> getArrayField(String field);

    String getStringField(String key);
}
