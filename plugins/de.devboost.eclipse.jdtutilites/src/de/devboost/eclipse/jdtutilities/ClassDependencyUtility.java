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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

/**
 * The {@link ClassDependencyUtility} can be used to retrieve information about
 * dependencies between Java classes using the Eclipse JDT search engine.
 * 
 * Note: This class allows to find dependencies between classes which are 
 * located in the workspace only. It cannot be used to analyze dependencies to
 * classes located in the running Eclipse instance or the JDK.
 */
public class ClassDependencyUtility {
	
	private interface IReferenceFinder {
		public Set<String> find(String path) throws CoreException;
	}

	private class DependencySearchRequestor extends SearchRequestor {
		
		private Set<String> result;

		private DependencySearchRequestor(Set<String> result) {
			this.result = result;
		}

		@Override
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			Object foundElement = match.getElement();
			if (foundElement instanceof IJavaElement) {
				IJavaElement javaElement = (IJavaElement) foundElement;
				addClassToSearchResult(result, javaElement);
			}
		}
	}

	/**
	 * Returns the paths of all Java elements that reference the element at the 
	 * given path.
	 */
	public Set<String> findReferencesTo(String path) throws CoreException {
		return find(path, IJavaSearchConstants.REFERENCES);
	}

	private Set<String> findTransitively(String path, IReferenceFinder finder) throws CoreException {
		Set<String> references = new LinkedHashSet<String>();
		Set<String> pathsToVisit = new LinkedHashSet<String>();
		pathsToVisit.add(path);
		while (!pathsToVisit.isEmpty()) {
			// search for new elements
			Iterator<String> iterator = pathsToVisit.iterator();
			String nextPath = iterator.next();
			iterator.remove();

			if (!references.contains(nextPath)) {
				Set<String> dependencies = finder.find(nextPath);
				pathsToVisit.addAll(dependencies);
			}

			references.add(nextPath);
		}
		return references;
	}
	
	/**
	 * Returns the paths of all Java elements that reference the element at the 
	 * given path. Also, elements that reference these elements (and so on) are
	 * returned.
	 * 
	 * @return a set of transitive references 
	 */
	public Set<String> findReferencesFromTransitively(String path) throws CoreException {
		return findTransitively(path, new IReferenceFinder() {
			
			@Override
			public Set<String> find(String path) throws CoreException {
				return findReferencesFrom(path);
			}
		});
	}

	/**
	 * Returns the paths of all Java elements that are references by the element 
	 * at the given path.
	 */
	public Set<String> findReferencesFrom(String path) throws CoreException {
		Set<String> result = new LinkedHashSet<String>();
		List<IType> types = getTypes(path);
		if (types.isEmpty()) {
			return result;
		}
		
		SearchEngine engine = new SearchEngine();
		SearchRequestor requestor = new DependencySearchRequestor(result);
		IProgressMonitor monitor = new NullProgressMonitor();
		for (IType type : types) {
			engine.searchDeclarationsOfReferencedTypes(type, requestor, monitor);
		}
		return result;
	}

	private Set<String> find(String path, int searchType) throws CoreException {
		Set<String> result = new LinkedHashSet<String>();
		// mark this element as visited
		List<IType> types = getTypes(path);
		if (types.isEmpty()) {
			return result;
		}
		
		SearchEngine engine = new SearchEngine();
		SearchParticipant[] participants = new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
		IJavaSearchScope scope = createScope();
		SearchRequestor requestor = new DependencySearchRequestor(result);
		IProgressMonitor monitor = new NullProgressMonitor();
		for (IType type : types) {
			SearchPattern pattern = SearchPattern.createPattern(type, searchType);
			engine.search(pattern, participants, scope, requestor, monitor);
		}
		return result;
	}

	private List<IType> getTypes(String path) throws JavaModelException {
		IType[] types = new JDTUtility().getJavaTypes(path);
		if (types == null) {
			return Collections.emptyList();
		}
		if (types.length == 0) {
			return Collections.emptyList();
		}
		return Arrays.asList(types);
	}

	@SuppressWarnings("restriction")
	private IJavaSearchScope createScope() {
		return org.eclipse.jdt.internal.core.JavaModelManager.getJavaModelManager().getWorkspaceScope();
	}

	private void addClassToSearchResult(Set<String> result, IJavaElement javaElement)
			throws JavaModelException {
		if (javaElement == null) {
			return;
		}
		IResource resource = javaElement.getResource();
		if (resource == null) {
			return;
		}
		String path = resource.getFullPath().toString();
		result.add(path);
	}
}
