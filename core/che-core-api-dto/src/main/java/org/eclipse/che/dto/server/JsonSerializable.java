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
package org.eclipse.che.dto.server;

import com.google.gson.JsonElement;
import java.io.Serializable;
import java.io.Writer;

/** An entity that may serialize itself to JSON. */
public interface JsonSerializable extends Serializable {

  /** Serializes DTO to JSON format. */
  String toJson();

  /** Serialize the DTO to JSON text through the given Writer. */
  void toJson(Writer w);

  /** Serializes DTO to JSON object. */
  JsonElement toJsonElement();
}
