/*******************************************************************************
 * Copyright (c) 2012
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class JDTUtility {

	/**
	 * Returns the name of the Java package that contains the given file.
	 */
	public String getPackageName(IFile file) {
		String packageName = new String();
		IContainer parent = file.getParent();
		List<String> packages = new ArrayList<String>();
		while (!isSourceFolder(parent)) {
			packages.add(0, parent.getName());
			parent = parent.getParent();
		}
		packageName = explode(packages, ".");
		return packageName;
	}

	private boolean isSourceFolder(IContainer element) {
		IJavaElement javaElement = JavaCore.create(element);
		if (javaElement instanceof IPackageFragmentRoot) {
			return true;
		}
		if (element == null) {
			return true;
		}
		if (element.getParent() instanceof IProject) {
			//if no source was found so far, assume that the upper most
			//folder is (intended to be) a source folder.
			return true;
		}
		return false;
	}

	/**
	 * Concatenates each element from the 'parts' collection and puts 'glue'
	 * in between.
	 */
	// TODO move this method to some other utility project handling strings
	private String explode(Collection<String> parts, String glue) {
		StringBuffer result = new StringBuffer();
		int size = parts.size();
		Iterator<String> iterator = parts.iterator();
		for (int i = 0; i < size; i++) {
			result.append(iterator.next());
			if (i < size - 1) {
				result.append(glue);
			}
		}
		return result.toString();
	}

	/**
	 * Returns the value of the given annotation property. If no annotation or
	 * property is found, null is returned.
	 */
	public String getAnnotationValue(IAnnotatable annotable, String simpleAnnotationName,
			String annotationProperty) {
		IAnnotation annotation = annotable.getAnnotation(simpleAnnotationName);
		try {
			IMemberValuePair[] memberValuePairs = annotation.getMemberValuePairs();
			for (IMemberValuePair memberValuePair : memberValuePairs) {
				String memberName = memberValuePair.getMemberName();
				Object value = memberValuePair.getValue();
				if (annotationProperty.equals(memberName)) {
					return value.toString();
				}
			}
		} catch (JavaModelException e) {
			// ignore
		}
		return null;
	}
}
