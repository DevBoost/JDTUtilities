/*******************************************************************************
 * Copyright (c) 2006-2013
 * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   DevBoost GmbH - Berlin, Germany
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

/**
 * The {@link AbstractWorkspaceCleaner} can be used to perform a clean build on
 * the whole workspace.
 */
public abstract class AbstractWorkspaceCleaner {

	public void performCleanBuildIfRequired() {
		logInfo("Checking if clean build is required.");
		Job job = createRefreshWorkspaceOnFirstStartupJob();
		if (job == null) {
			return;
		}
		job.schedule();
	}

	private Job createRefreshWorkspaceOnFirstStartupJob() {
		if (mustClean()) {
			logInfo("Clean build is required. Scheduling clean workspace job.");
			Job job = createCleanAllJob();
			return job;
		} else {
			logInfo("No clean build is required.");
			return null;
		}
	}

	protected abstract void setCleaned();

	protected abstract boolean mustClean();
	
	protected abstract void logException(CoreException ce);

	protected abstract void logInfo(String message);

	private Job createCleanAllJob() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		final IProject[] projects = root.getProjects();
		Job cleanAllJob = new Job("Clean workspace") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				logInfo("Cleaning workspace.");
				for (IProject project : projects) {
					logInfo("Cleaning project " + project.getName());
					try {
						project.build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
					} catch (CoreException ce) {
						logException(ce);
					}
				}
				setCleaned();
				return Status.OK_STATUS;
			}
		};
		cleanAllJob.setRule(root);
		return cleanAllJob;
	}
}
