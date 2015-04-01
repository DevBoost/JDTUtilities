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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public abstract class AbstractCleanWorkspaceJob extends Job {
	
	public AbstractCleanWorkspaceJob() {
		super("Clean workspace");
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		setRule(root);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		final IProject[] projects = root.getProjects();
		logInfo("Cleaning workspace.");
		for (IProject project : projects) {
			logInfo("Cleaning project " + project.getName());
			try {
				project.build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
			} catch (CoreException ce) {
				logException(ce);
			}
			
			logInfo("Building project " + project.getName());
			try {
				project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
			} catch (CoreException ce) {
				logException(ce);
			}
		}
		setCleaned();
		return Status.OK_STATUS;
	}

	protected abstract void setCleaned();

	protected abstract void logException(Exception e);

	protected abstract void logInfo(String message);
}