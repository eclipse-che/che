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

package org.eclipse.che.ide.collections.js;

import com.google.gwt.core.client.JavaScriptObject;
import org.eclipse.che.ide.collections.Jso;
import org.eclipse.che.ide.collections.StringMap;

/**
 * This is used to satisfy DTO casting requirements.
 *
 * <p>On the client, if you have a reference to StringMap, or JsoStringMap (the JavaScriptObject
 * backed impl), feel free to cross cast this to the more robust {@link
 * com.codenvy.ide.collections.Jso}.
 */
public final class JsoStringMap<T> extends JavaScriptObject implements StringMap<T> {

  /*
   * GWT dev mode adds a __gwt_ObjectId property to all objects. We have to call
   * hasOwnProperty (which is overridden by GWT) when using for(in) loops to verify
   * that the key is actually a property of the object, and not the __gwt_ObjectId
   * set by Chrome dev mode.
   */

  /** Convenience factory method. */
  public static <T> JsoStringMap<T> create() {
    return Jso.create().cast();
  }

  protected JsoStringMap() {}

  @Override
  public native T get(String key) /*-{
        return Object.prototype.hasOwnProperty.call(this, key) ? this[key] : undefined;
    }-*/;

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

  @Override
  public final native boolean isEmpty() /*-{
        for (key in this) {
            if (Object.prototype.hasOwnProperty.call(this, key)) {
                return false;
            }
        }
        return true;
    }-*/;

  /**
   * Method for iterating through the contents of a Map.
   *
   * <p>
   *
   * <p>{@code T} is the expected type of the values returned from the map. <b>Caveat:</b> if you
   * have a map of heterogeneous types, you need to think what value of T you specify here.
   *
   * @param callback The callback object that gets called on each iteration.
   */
  @Override
  public native void iterate(IterationCallback<T> callback) /*-{
        for (key in this) {
            if (Object.prototype.hasOwnProperty.call(this, key)) {
                callback.
                    @org.eclipse.che.ide.collections.StringMap.IterationCallback::onIteration(Ljava/lang/String;Ljava/lang/Object;)
                    (key, this[key]);
            }
        }
    }-*/;

  // TODO: We still have problem with "__proto__"
  @Override
  public native void put(String key, T value) /*-{
        this[key] = value;
    }-*/;

  @Override
  public void putAll(StringMap<T> map) {
    map.iterate(
        new IterationCallback<T>() {
          @Override
          public void onIteration(String key, T value) {
            put(key, value);
          }
        });
  }

  @Override
  public native T remove(String key) /*-{
        if (!Object.prototype.hasOwnProperty.call(this, key)) {
            return undefined;
        }
        var retVal = this[key];
        delete this[key];
        return retVal;
    }-*/;

  @Override
  public native boolean containsKey(String key) /*-{
        return Object.prototype.hasOwnProperty.call(this, key);
    }-*/;

  /**
   * Returns the size of the map (the number of keys).
   *
   * <p>
   *
   * <p>NB: This method is currently O(N) because it iterates over all keys.
   *
   * @return the size of the map.
   */
  @Override
  public final native int size() /*-{
        size = 0;
        for (key in this) {
            if (Object.prototype.hasOwnProperty.call(this, key)) {
                size++;
            }
        }
        return size;
    }-*/;

  /** {@inheritDoc} */
  public final native JsoArray<T> getValues() /*-{
        var data = [];
        for (var i in this) {
            if (Object.prototype.hasOwnProperty.call(this, key)) {
                data.push(this[i]);
            }
        }
        return data;
    }-*/;
}
