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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CompilationParticipant;

/**
 * The AbstractCompilationParticipant is a default implementation of the JDT
 * {@link CompilationParticipant} that collects all files that are build in a
 * list and calls the template method {@link #buildFinished(BuildContext)} for
 * each files once the build is finished.
 * 
 * The AbstractCompilationParticipant is active for all Java projects.
 */
public abstract class AbstractCompilationParticipant extends CompilationParticipant {

	/**
	 * All files passed to {@link #buildStarting(BuildContext[], boolean)} are
	 * stored in this list, because there can be multiple calls to
	 * {@link #buildStarting(BuildContext[], boolean)} before 
	 * {@link #buildFinished(IJavaProject)} is eventually called.
	 * In {@link #buildFinished(IJavaProject)} we process the list and remove
	 * the files after processing.
	 */
	private List<CompilationEvent> files = new ArrayList<CompilationEvent>();

	@Override
	public void buildStarting(BuildContext[] files, boolean isBatch) {
		super.buildStarting(files, isBatch);
		if (files == null) {
			return;
		}
		for (BuildContext file : files) {
			CompilationEvent event = new CompilationEvent(file, isBatch);
			this.files.add(event);
			buildStarting(event);
		}
	}
	
	@Override
	public void buildFinished(IJavaProject project) {
		super.buildFinished(project);
		
		try {
			// we use an iterator to avoid ConcurrentModificationExceptions
			List<CompilationEvent> events = new ArrayList<CompilationEvent>();
			Iterator<CompilationEvent> it = files.iterator();
			while (it.hasNext()) {
				CompilationEvent event = it.next();
				events.add(event);
				it.remove();
			}
			buildFinished(events);
		} catch (Throwable t) {
			// TODO: handle exception
			t.printStackTrace();
		}
	}
	
	@Override
	public boolean isActive(IJavaProject project) {
		return true;
	}

	public abstract void buildStarting(CompilationEvent event);

	public abstract void buildFinished(Collection<CompilationEvent> events);
}
