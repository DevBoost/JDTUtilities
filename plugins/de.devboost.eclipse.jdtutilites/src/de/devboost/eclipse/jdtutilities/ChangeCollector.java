/*******************************************************************************
 * Copyright (c) 2006-2013
 * Software Technology Group, Dresden University of Technology
 * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany;
 *   DevBoost GmbH - Berlin, Germany
 *      - initial API and implementation
 ******************************************************************************/
package de.devboost.eclipse.jdtutilities;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class ChangeCollector<T> {

	private Set<T> changedObjects = new LinkedHashSet<T>();
	
	public Set<T> retrieveChanges() {
		synchronized (changedObjects) {
			Set<T> copy = new LinkedHashSet<T>();
			copy.addAll(changedObjects);
			changedObjects.clear();
			return copy;
		}
	}
	
	public void addChanges(Set<T> newChangedObjects) {
		synchronized (changedObjects) {
			boolean queueWasEmpty = changedObjects.isEmpty();
			changedObjects.addAll(newChangedObjects);

			// if the queue is not empty, we do not need to start a job, because
			// there is already one that was not executed yet
			if (queueWasEmpty) {
				notifyNewChangedObjectArrived();
			}
		}
	}

	protected abstract void notifyNewChangedObjectArrived();
}
