/*******************************************************************************
 * Copyright (c) 2012-2015
 * DevBoost GmbH, Dresden, Amtsgericht Dresden, HRB 34001
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   DevBoost GmbH - Dresden, Germany
 *      - initial API and implementation
 ******************************************************************************/
package de.devboost.eclipse.jdtutilities;

import org.eclipse.jdt.core.compiler.BuildContext;

public class CompilationEvent {

	private boolean batch;
	private BuildContext context;
	
	public CompilationEvent(BuildContext context, boolean batch) {
		super();
		this.context = context;
		this.batch = batch;
	}
	
	public boolean isBatch() {
		return batch;
	}

	public BuildContext getContext() {
		return context;
	}
}
