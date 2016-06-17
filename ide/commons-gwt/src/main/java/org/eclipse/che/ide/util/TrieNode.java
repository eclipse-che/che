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

package org.eclipse.che.ide.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A node in a trie that can be used for efficient autocompletion lookup.
 *
 * @param <T>
 *         value object type
 */
public final class TrieNode<T> {
    private final String prefix;

    private final List<TrieNode<T>> children;

    private T value;

    private TrieNode(String prefix) {
        this.prefix = prefix;
        this.value = null;
        this.children = new ArrayList<>();
    }

    public static <T> TrieNode<T> makeNode(String prefix) {
        return new TrieNode<T>(prefix);
    }

    public List<TrieNode<T>> getChildren() {
        return children;
    }

    TrieNode<T> findInsertionBranch(String prefix) {
        for (int i = 0, size = children.size(); i < size; i++) {
            TrieNode<T> child = children.get(i);
            if (prefix.startsWith(child.getPrefix())) {
                return child;
            }
        }
        return null;
    }

    public void addChild(TrieNode<T> child) {
        children.add(child);
    }

    public boolean getIsLeaf() {
        return this.value != null;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.prefix;
    }
}
