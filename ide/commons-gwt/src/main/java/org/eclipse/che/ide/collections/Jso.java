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

import com.google.gwt.core.client.JavaScriptObject;
import org.eclipse.che.ide.collections.js.JsoArray;

/**
 * Utility class for JavaScriptObject construction and serialization. We try to keep all the nasty
 * typesystem cludge with working with JavaScriptObject maps confined in this class. We rely on
 * identical method body coalescing to keep the code small.
 *
 * <p>TODO: Using the builder pattern like this for constructing JSOs on the client will cause a lot
 * of boundary crossings. Revisit this as a potential perf optimization if we find ourselve spending
 * a lot of time building objects for serialization.
 */
public class Jso extends JavaScriptObject implements JsonObject {

  public static Jso create() {
    return JavaScriptObject.createObject().cast();
  }

  /** Deserializes a JSON String and returns an overlay type. */
  public static native <T extends Jso> T deserialize(String jsonString) /*-{
        return JSON.parse(jsonString);
    }-*/;

  /** Serializes a JsonObject into a String. */
  public static native String serialize(JavaScriptObject jso) /*-{
        return JSON.stringify(jso);
    }-*/;

  protected Jso() {}

  @Override
  public final native JsonObject addField(String key, boolean value) /*-{
        this[key] = value;
        return this;
    }-*/;

  @Override
  public final native JsonObject addField(String key, double value) /*-{
        this[key] = value;
        return this;
    }-*/;

  @Override
  public final native JsonObject addField(String key, int value) /*-{
        this[key] = value;
        return this;
    }-*/;

  @Override
  public final JsonObject addField(String key, Array<?> value) {
    // Delegate to the JS impl
    return addField(key, (Object) value);
  }

  @Override
  public final JsonObject addField(String key, JsonObject value) {
    // Delegate to the JS impl
    return addField(key, (Object) value);
  }

  public final native Jso addField(String key, Object value) /*-{
        this[key] = value;
        return this;
    }-*/;

  @Override
  public final native Jso addField(String key, String value) /*-{
        this[key] = value;
        return this;
    }-*/;

  public final native Jso addNullField(String key) /*-{
        this[key] = null;
        return this;
    }-*/;

  public final native Jso addUndefinedField(String key) /*-{
        this[key] = undefined;
        return this;
    }-*/;

  public final native Jso deleteField(String key) /*-{
        delete this[key];
    }-*/;

  @Override
  public final JsoArray<JsonObject> getArrayField(String key) {
    return ((Jso) getObjectField(key)).cast();
  }

  @Override
  public final boolean getBooleanField(String key) {
    return getBooleanFieldImpl(key);
  }

  /**
   * @return evaluated boolean value (casted to a boolean). This is useful for "optional" boolean
   *     fields to treat {@code undefined} values as {@code false}
   */
  public final native boolean getFieldCastedToBoolean(String key) /*-{
        return !!this[key];
    }-*/;

  @Override
  public final int getIntField(String key) {
    return getIntFieldImpl(key);
  }

  @Override
  public final double getDoubleField(String key) {
    return getFieldCastedToNumber(key);
  }

  /**
   * Evaluates a given property to an integer number. The {@code undefined} and {@code null} key
   * values will be casted to zero.
   *
   * @return evaluated integer value (casted to integer)
   */
  public final int getFieldCastedToInteger(String key) {
    return (int) getFieldCastedToNumber(key);
  }

  /**
   * Evaluates a given property to a number. The {@code undefined} and {@code null} key values will
   * be casted to zero.
   *
   * @return evaluated number value (casted to number)
   */
  private native double getFieldCastedToNumber(String key) /*-{
        return +(this[key] || 0);
    }-*/;

  public final native JavaScriptObject getJsObjectField(String key) /*-{
        return this[key];
    }-*/;

  /*
   * GWT dev mode adds a __gwt_ObjectId property to all objects. We have to call
   * hasOwnProperty (which is overridden by GWT) when using for(in) loops to verify
   * that the key is actually a property of the object, and not the __gwt_ObjectId
   * set by Chrome dev mode.
   */
  @Override
  public final native JsoArray<String> getKeys() /*-{
        keys = [];
        for (key in this) {
            if (Object.prototype.hasOwnProperty.call(this, key)) {
                keys.push(key);
            }
        }
        return keys;
    }-*/;

  public final native boolean isEmpty() /*-{
        for (key in this) {
            if (Object.prototype.hasOwnProperty.call(this, key)) {
                return false;
            }
        }
        return true;
    }-*/;

  @Override
  public final native JsonObject getObjectField(String key) /*-{
        return this[key];
    }-*/;

  public final native Object getJavaObjectField(String key) /*-{
        return this[key];
    }-*/;

  @Override
  public final native String getStringField(String key) /*-{
        return this[key];
    }-*/;

  /** @return evaluated string value (casted to string) */
  public final native String getFieldCastedToString(String key) /*-{
        return "" + this[key];
    }-*/;

  /**
   * Checks to see if the specific key is present as a field/property on the Jso.
   *
   * <p>You should guard calls to primitive type getters with this or else you might blow up at
   * runtime (attempting to pass null as a return value for a primitive type).
   *
   * @param key
   * @return whether or not
   */
  public final native boolean hasOwnProperty(String key) /*-{
        return this.hasOwnProperty(key);
    }-*/;

  public final String serialize() {
    return Jso.serialize(this);
  }

  private native boolean getBooleanFieldImpl(String key) /*-{
        return this[key];
    }-*/;

  private native int getIntFieldImpl(String key) /*-{
        return this[key];
    }-*/;

  /** Removes the GWT development/hosted mode property "__gwt_ObjectId". */
  public final void removeGwtObjectId() {
    deleteField("__gwt_ObjectId");
  }
}
