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
	
	protected abstract void logException(Exception e);

	protected abstract void logInfo(String message);

	private Job createCleanAllJob() {
		Job cleanAllJob = new AbstractCleanWorkspaceJob() {

			@Override
			protected void setCleaned() {
				AbstractWorkspaceCleaner.this.setCleaned();
			}

			@Override
			protected void logException(Exception e) {
				AbstractWorkspaceCleaner.this.logException(e);
			}

			@Override
			protected void logInfo(String message) {
				AbstractWorkspaceCleaner.this.logInfo(message);
			}
			
		};
		return cleanAllJob;
	}
}
