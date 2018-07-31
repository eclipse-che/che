/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.qa.examples;

import java.io.IOException;
import java.util.List;


interface TreeVisitor<T, U> {
	public T visit(U location);
}

interface TreeVisitable<U> {
	public <T> T visit(TreeVisitor<T, U> visitor) throws IOException;
}

abstract class Param implements TreeVisitable<Param> {
	public final Param lookforParam(final String name) {
		TreeVisitor<Param, Param> visitor = new TreeVisitor<Param, Param>() {
			public Param visit(Param location) {
				return null;
			}
		};
		return visit(visitor); // SELECT #visit(...)
	}

	public abstract <T> T visit(TreeVisitor<T, Param> visitor);
}

class StructParam extends Param {
	public <T> T visit(TreeVisitor<T, Param> visitor) {
		return null;
	}
}

public class X {
	public static void main(String[] args) {
		StructParam p = new StructParam();
		p.lookforParam("abc");
		System.out.println("done");
	}

}
