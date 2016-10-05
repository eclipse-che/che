/*******************************************************************************
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend Technologies - initial API and implementation
 *******************************************************************************/
package zend.com.che.plugin.zdb.server.connection;

import static zend.com.che.plugin.zdb.server.connection.IDebugDataType.DataType.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Zend debug expression value.
 * 
 * @author Bartlomiej Laczkowski
 */
public class ZendDebugExpressionValue implements IDebugExpressionValue, IDebugDataType {

	public static final ZendDebugExpressionValue NULL_VALUE = new ZendDebugExpressionValue(PHP_NULL, "null", null);

	protected DataType type;
	protected List<IDebugExpression> children;
	protected int childrenCount = 0;
	protected String value;

	public ZendDebugExpressionValue(DataType type, String value, List<IDebugExpression> children) {
		this.type = type;
		this.value = value;
		this.children = children == null ? new ArrayList<>() : children;
	}

	public ZendDebugExpressionValue(DataType type, String value, List<IDebugExpression> children,
			int childrenCount) {
		this.type = type;
		this.value = value;
		this.children = children == null ? new ArrayList<>() : children;
		this.childrenCount = childrenCount;
	}

	@Override
	public DataType getDataType() {
		return type;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public List<IDebugExpression> getChildren() {
		return sort(children);
	}

	@Override
	public int getChildrenCount() {
		return childrenCount;
	}

	public static List<IDebugExpression> sort(List<IDebugExpression> list) {
		Collections.sort(list, new Comparator<IDebugExpression>() {
			@Override
			public int compare(IDebugExpression o1, IDebugExpression o2) {
				String o1name = o1.getName();
				int o1idx = o1name.lastIndexOf(':');
				if (o1idx != -1)
					o1name = o1name.substring(o1idx + 1);
				String o2name = o2.getName();
				int o2idx = o2name.lastIndexOf(':');
				if (o2idx != -1)
					o2name = o2name.substring(o2idx + 1);
				return o1name.compareToIgnoreCase(o2name);
			}
		});
		return list;
	}

}