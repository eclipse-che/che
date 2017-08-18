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
package org.eclipse.che.dto.shared;

/**
 * Tag interface for DTOs that are serialized to compact (non human readable) JSON.
 *
 * <p>
 *
 * <p>Compact JSON has array as a root element. As a consequence GSON library throws an exception
 * when you try do deserialize JSON containing compact object as a root. Deserialize using {@link
 * com.google.gson.JsonElement} in such cases.
 *
 * <p>
 *
 * <p>Note: try to eliminate enums and boolean fields in DTO to get better serialized form density.
 */
public interface CompactJsonDto {}
