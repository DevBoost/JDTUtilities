/*******************************************************************************
 * Copyright (c) 2012-2013
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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

public abstract class ProjectUtility {

	/**
	 * Checks whether the given project (or one of its transitive dependencies)
	 * contains errors.
	 * 
	 * @param projectName the name of the project to check
	 * @return true if there is errors, false if not
	 */
	public boolean hasProblems(String projectName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		
		Set<String> requiredProjects = new LinkedHashSet<String>();
		
		Set<String> projectsToAnalyze = new LinkedHashSet<String>();
		projectsToAnalyze.add(projectName);
		
		while (!projectsToAnalyze.isEmpty()) {
			Iterator<String> iterator = projectsToAnalyze.iterator();
			IProject project = root.getProject(iterator.next());
			iterator.remove();
			if (project != null) {
				requiredProjects.add(project.getName());
				String[] dependencies = getRequiredProjects(project);
				for (String dependency : dependencies) {
					if (!requiredProjects.contains(dependency)) {
						projectsToAnalyze.add(dependency);
					}
					requiredProjects.add(dependency);
				}
			}
		}
		
		for (String requiredProject : requiredProjects) {
			if (hasProblems(root.getProject(requiredProject))) {
				return true;
			}
		}
		return false;
	}

	// This code is copied from org.eclipse.debug.core.model.LaunchConfigurationDelegate.
	public boolean hasProblems(IProject project) {
		try {
			if (!project.exists()) {
				return false;
			}
			IMarker[] markers = project.findMarkers(IMarker.PROBLEM, true,
					IResource.DEPTH_INFINITE);
			int markerCount = markers.length;
			if (markerCount > 0) {
				for (int i = 0; i < markerCount; i++) {
					if (isErrorOrWorse(markers[i])) {
						return true;
					}
				}
			}
		} catch (CoreException ce) {
			logError("Exception while checking project for error markers.", ce);
		}
		
		return false;
	}

	private String[] getRequiredProjects(IProject project) {
		IJavaProject javaProject = new JDTUtility().getJavaProject(project);
		if (javaProject != null) {
			try {
				String[] requiredProjectNames = javaProject.getRequiredProjectNames();
				return requiredProjectNames;
			} catch (JavaModelException e) {
				logError("Exception while determining project dependencies.", e);
			}
		}
		return new String[0];
	}
	
	// This code is copied from org.eclipse.debug.core.model.LaunchConfigurationDelegate.
	private boolean isErrorOrWorse(IMarker problemMarker) throws CoreException {
		Integer severity = (Integer) problemMarker.getAttribute(IMarker.SEVERITY);
		if (severity != null) {
			return severity.intValue() >= IMarker.SEVERITY_ERROR;
		} 
		
		return false;
	}

	protected abstract void logError(String message, Exception e);
}
