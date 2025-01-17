/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sysds.runtime.compress.colgroup.scheme;

import org.apache.sysds.runtime.compress.colgroup.AColGroup;
import org.apache.sysds.runtime.compress.colgroup.indexes.IColIndex;
import org.apache.sysds.runtime.matrix.data.MatrixBlock;
import org.apache.sysds.runtime.matrix.data.Pair;

public abstract class ACLAScheme implements ICLAScheme {
	protected final IColIndex cols;

	protected ACLAScheme(IColIndex cols) {
		this.cols = cols;
	}

	@Override
	public AColGroup encode(MatrixBlock data) {
		return encode(data, cols);
	}

	@Override
	public ICLAScheme update(MatrixBlock data) {
		return update(data, cols);
	}

	@Override
	public Pair<ICLAScheme, AColGroup> updateAndEncode(MatrixBlock data) {
		return updateAndEncode(data, cols);
	}

	@Override
	public Pair<ICLAScheme, AColGroup> updateAndEncode(MatrixBlock data, IColIndex columns) {
		try {
			return tryUpdateAndEncode(data, columns);
		}
		catch(Exception e) {
			LOG.warn("Failed to update and encode in one pass, fallback to separated update and encode");
			ICLAScheme s = update(data, columns);
			AColGroup g = encode(data, columns);
			return new Pair<>(s, g);
		}
	}

	protected Pair<ICLAScheme, AColGroup> tryUpdateAndEncode(MatrixBlock data, IColIndex columns) {
		final ICLAScheme s = update(data, columns);
		final AColGroup g = encode(data, columns);
		return new Pair<>(s, g);
	}

	protected final void validate(MatrixBlock data, IColIndex columns) throws IllegalArgumentException {
		if(columns.size() != cols.size())
			throw new IllegalArgumentException(
				"Invalid number of columns to encode expected: " + cols.size() + " but got: " + columns.size());

		final int nCol = data.getNumColumns();
		if(nCol < cols.get(cols.size() - 1))
			throw new IllegalArgumentException(
				"Invalid columns to encode with max col:" + nCol + " list of columns: " + columns);
	}

}
