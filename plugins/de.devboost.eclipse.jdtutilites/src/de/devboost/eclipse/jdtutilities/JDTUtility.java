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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public abstract class JDTUtility {

	/**
	 * Returns true if the given project has the JDT nature.
	 */
	public boolean isJavaProject(IProject project) {
		if (project == null) {
			return false;
		}
		try {
			return project.isNatureEnabled("org.eclipse.jdt.core.javanature");
		} catch (CoreException e) {
		}
		return false;
	}

	/**
	 * Returns the Java project that corresponds to the given project or <code>null</code> if the project is not a Java
	 * project.
	 */
	public IJavaProject getJavaProject(IProject project) {
		return (isJavaProject(project) ? JavaCore.create(project) : null);
	}

	/**
	 * Returns the Java project that corresponds to the given name or <code>null</code> if the project cannot be found.
	 */
	public IJavaProject getJavaProject(String projectName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject(projectName);
		if (project == null) {
			return null;
		}
		if (!project.exists()) {
			return null;
		}
		return getJavaProject(project);
	}

	/**
	 * Returns the Java project that contains the given resource or <code>null</code> if the resource is not part of a
	 * Java project.
	 */
	public IJavaProject getJavaProject(IResource resource) {
		return getJavaProject(resource.getProject());
	}

	/**
	 * Returns all Java types that are contained in the resource located at the given path. The path may point to
	 * compiled or source code.
	 */
	public IType[] getJavaTypes(String path) throws JavaModelException {
		IJavaElement javaElement = getJavaElement(path);
		if (javaElement instanceof IClassFile) {
			IClassFile classFile = (IClassFile) javaElement;
			return new IType[] { classFile.getType() };
		}
		if (javaElement instanceof ICompilationUnit) {
			ICompilationUnit compilationUnit = (ICompilationUnit) javaElement;
			IType[] types = compilationUnit.getTypes();
			return types;
		}
		return null;
	}

	/**
	 * Returns the Java element contained in the resource located at the given path or <code>null</code> if the resource
	 * does not contain a Java element.
	 */
	public IJavaElement getJavaElement(String path) throws JavaModelException {

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = root.getFile(new Path(path));
		IJavaProject javaProject = getJavaProject(file);
		if (javaProject == null) {
			return null;
		}

		IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
		for (IPackageFragmentRoot packageFragmentRoot : roots) {
			IPath fragmentPath = packageFragmentRoot.getPath();
			String fragmentPathString = fragmentPath.toString();
			if (path.startsWith(fragmentPathString + "/")) {
				// resource is contained in this package fragment root
				String classPathRelativePath = path.substring(fragmentPathString.length() + 1);
				IJavaElement element = javaProject.findElement(new Path(classPathRelativePath));
				if (element != null) {
					return element;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the Java type that has the given qualified name and is contained in a project with the given name. If no
	 * type is found, <code>null</code> is returned.
	 */
	public IType getType(String projectName, String qualifiedName) {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject(projectName);
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null) {
			return null;
		}
		if (!javaProject.exists()) {
			return null;
		}
		try {
			IType type = javaProject.findType(qualifiedName);
			return type;
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

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

	/**
	 * Checks whether the given element is a Java source folder (i.e. a JDT package fragment root).
	 */
	private boolean isSourceFolder(IContainer element) {
		IJavaElement javaElement = JavaCore.create(element);
		if (javaElement instanceof IPackageFragmentRoot) {
			return true;
		}
		if (element == null) {
			return true;
		}
		if (element.getParent() instanceof IProject) {
			// if no source was found so far, assume that the upper most
			// folder is (intended to be) a source folder.
			return true;
		}
		return false;
	}

	/**
	 * Checks whether the given EMF platform URI is located in an output folder of the given, containing Java project.
	 */
	public boolean isInOutputFolder(IProject project, String platformURI) {
		IJavaProject javaProject = getJavaProject(project);
		if (javaProject == null) {
			return false;
		}

		try {
			// Handle projects that have multiple output locations.
			IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
			for (IClasspathEntry classpathEntry : classpathEntries) {
				IPath outputLocation = classpathEntry.getOutputLocation();
				if (isContained(outputLocation, platformURI)) {
					return true;
				}
			}

			IPath outputLocation = javaProject.getOutputLocation();
			if (isContained(outputLocation, platformURI)) {
				return true;
			}
		} catch (JavaModelException e) {
			logWarning("Can't determine output location for project " + javaProject.getElementName(), e);
		}

		return false;
	}

	/**
	 * Checks whether the given EMF platform URI is located in the given output folder.
	 */
	private boolean isContained(IPath outputLocation, String platformURI) {
		if (outputLocation == null) {
			return false;
		}
		
		String outputPath = outputLocation.toString();
		// We add a slash to the end of the path to make sure that the
		// output directory (e.g., 'bin') is not only a prefix of the
		// corresponding fragments in the URI (e.g., 'bind-src'), but really
		// refers to the same directory.
		if (!outputPath.endsWith("/")) {
			outputPath = outputPath + "/";
		}

		if (platformURI.startsWith(outputPath)) {
			return true;
		}

		return false;
	}

	/**
	 * Checks whether the given path is the output path of the given project. If the project is not a Java project,
	 * <code>false</code> is returned.
	 */
	public boolean isOutputFolder(IProject project, IPath path) {
		IJavaProject javaProject = getJavaProject(project);
		if (javaProject == null) {
			return false;
		}

		// FIXME Handle projects that have multiple output locations.

		try {
			IPath outputLocation = javaProject.getOutputLocation();
			if (outputLocation == null) {
				return false;
			}
			if (outputLocation.equals(path)) {
				return true;
			}
		} catch (JavaModelException e) {
			logWarning("Can't determine output location for project " + javaProject.getElementName(), e);
		}

		return false;
	}

	protected abstract void logWarning(String message, Exception e);

	/**
	 * Concatenates each element from the 'parts' collection and puts 'glue' in between.
	 */
	// TODO move this method to some other utility project handling strings
	private String explode(Collection<String> parts, String glue) {
		StringBuilder result = new StringBuilder();
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
	 * Returns the value of the given annotation property. If no annotation or property is found, null is returned.
	 */
	public Object getAnnotationValue(IAnnotatable annotable, String simpleAnnotationName, String annotationProperty) {
		IAnnotation annotation = annotable.getAnnotation(simpleAnnotationName);
		if (annotation == null) {
			return null;
		}
		return getAnnotationPropertyValue(annotation, annotationProperty);
	}

	/**
	 * Returns the value of the given property and Java annotation.
	 */
	public Object getAnnotationPropertyValue(IAnnotation annotation, String annotationPropertyName) {
		try {
			IMemberValuePair[] memberValuePairs = annotation.getMemberValuePairs();
			for (IMemberValuePair memberValuePair : memberValuePairs) {
				String memberName = memberValuePair.getMemberName();
				if (annotationPropertyName.equals(memberName)) {
					Object value = memberValuePair.getValue();
					return value;
				}
			}
		} catch (JavaModelException e) {
			// ignore
		}
		return null;
	}

	/**
	 * Returns the compilation unit contained in the given file or <code>null</code> if the file does not contain a
	 * compilation unit or if the file does not exist or if the JDT is not running.
	 */
	public ICompilationUnit getCompilationUnit(IFile file) {
		// determine compilation unit that is contained in the file
		IJavaElement javaElement = JavaCore.create(file);
		if (javaElement == null) {
			return null;
		}

		if (!javaElement.exists()) {
			return null;
		}

		if (javaElement instanceof ICompilationUnit) {
			return (ICompilationUnit) javaElement;
		}
		return null;
	}

	/**
	 * Returns the type that is contained in the given file. The file must be a Java class file. If the file does not
	 * exists or if it is not a class file, <code>null</code> is returned.
	 */
	public IType getType(IFile file) {
		IJavaElement javaElement = JavaCore.create(file);
		if (!javaElement.exists()) {
			return null;
		}

		if (javaElement instanceof IClassFile) {
			IClassFile classFile = (IClassFile) javaElement;
			return classFile.getType();
		}

		return null;
	}

	public CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}
}
