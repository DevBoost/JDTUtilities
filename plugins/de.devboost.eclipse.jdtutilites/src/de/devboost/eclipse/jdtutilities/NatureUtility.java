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

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;

public class NatureUtility {

	/**
	 * Adds a nature to a project.
	 * 
	 * @param project the project to modify
	 * @param natureId the ID of the nature to add
	 * 
	 * @throws CoreException if adding the project nature fails
	 */
	public void addNature(IProject project, String natureId) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();
		
		for (int i = 0; i < natures.length; ++i) {
			if (natureId.equals(natures[i])) {
				// already active
				return;
			}
		}
		// Add the nature
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = natureId;
		description.setNatureIds(newNatures);
		project.setDescription(description, null);
	}

	public void addBuilder(IProject project, String builderId) throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();
		
		// check whether builder is already registered
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderId)) {
				return;
			}
		}
		
		// if not, add builder to project configuration
		ICommand[] newCommands = new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newCommands, 0, commands.length);
		// create new command for builder
		ICommand command = desc.newCommand();
		command.setBuilderName(builderId);
		// add command to project description
		newCommands[newCommands.length - 1] = command;
		desc.setBuildSpec(newCommands);
		// write project configuration
		project.setDescription(desc, null);
	}
	
	public void removeBuilder(IProject project, String builderId) throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		ICommand[] newCommands = commands;
		for (int i = 0; i < newCommands.length; ++i) {
			if (newCommands[i].getBuilderName().equals(builderId)) {
				ICommand[] tempCommands = new ICommand[newCommands.length - 1];
				System.arraycopy(newCommands, 0, tempCommands, 0, i);
				System.arraycopy(newCommands, i + 1, tempCommands, i, newCommands.length - i - 1);
				newCommands = tempCommands;
				break;
			}
		}
		if (newCommands != commands) {
			description.setBuildSpec(newCommands);
		}
	}
}
