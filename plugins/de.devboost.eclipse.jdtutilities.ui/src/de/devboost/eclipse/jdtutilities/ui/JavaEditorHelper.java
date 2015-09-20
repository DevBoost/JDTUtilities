package de.devboost.eclipse.jdtutilities.ui;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

@SuppressWarnings("restriction")
public class JavaEditorHelper {
	
	public final static JavaEditorHelper INSTANCE = new JavaEditorHelper();

	/**
	 * Use {@link #INSTANCE} instead.
	 */
	@Deprecated
	public JavaEditorHelper() {
	}

	public void switchToJavaEditor(IType type, String methodName, List<String> parameterTypes) {
		ICompilationUnit unit = type.getCompilationUnit();
		JavaEditor javaEditor = getJavaEditor(unit);
		if(javaEditor != null){
			IMethod newMethod = null;
			try {
				String[] parameterTypeSignatures = new String[parameterTypes.size()];
				for (int i = 0; i < parameterTypeSignatures.length; i++) {
					String qualifiedName = parameterTypes.get(i);
					String signature = Signature.createTypeSignature(qualifiedName, true);
					parameterTypeSignatures[i] = signature;
				}

				newMethod = JavaModelUtil.findMethod(methodName, parameterTypeSignatures, false, type);
			} catch (JavaModelException e) {
				// ignore this
			}
			if (newMethod == null) {
				return;
			}
			javaEditor.setSelection(newMethod);
		}
	}

	public void switchToJavaEditor(ICompilationUnit unit, int lineNumber) {
		JavaEditor javaEditor = getJavaEditor(unit);
		if(javaEditor != null){
			IDocument document = javaEditor.getDocumentProvider().getDocument(javaEditor.getEditorInput());
			IRegion lineInfo = null;
			try {
				// line count internally starts with 0, and not with 1 like in
				// GUI
				lineInfo = document.getLineInformation(lineNumber - 1);
			} catch (BadLocationException e) {
				// ignored because line number may not really exist in document,
				// we guess this...
			}
			if (lineInfo != null) {
				javaEditor.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
			}
		}
	}

	public void switchToJavaEditor(ICompilationUnit compilationUnit, IJavaElement javaElement) {
		JavaEditor javaEditor = getJavaEditor(compilationUnit);
		if(javaEditor != null){
			javaEditor.setSelection(javaElement);
		}
	}

	public JavaEditor getJavaEditor(ICompilationUnit compilationUnit) {
		if (compilationUnit == null) {
			return null;
		}
		
		IResource resource = compilationUnit.getResource();
		if (resource == null) {
			return null;
		}
		
		if (resource.exists()) {
			IEditorPart part = null;
			part = EditorUtility.isOpenInEditor(compilationUnit);
			if (part == null) {
				try {
					part = JavaUI.openInEditor(compilationUnit);
				} catch (PartInitException e) {
					// ignore
				} catch (JavaModelException e) {
					// ignore
				}
			}
			IWorkbenchPage page = JavaPlugin.getActivePage();
			if (page != null && part != null) {
				page.bringToTop(part);
			}
			if (part != null) {
				part.setFocus();
			}
			if (part instanceof JavaEditor) {
				return (JavaEditor) part;
			}
		}
		return null;
	}

	public void switchToJavaEditor(IType type) {
		ICompilationUnit compilationUnit = type.getCompilationUnit();
		switchToJavaEditor(compilationUnit, type);
	}
}
