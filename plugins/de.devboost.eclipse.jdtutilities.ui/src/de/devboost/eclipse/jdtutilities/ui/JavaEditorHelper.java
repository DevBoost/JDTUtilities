package de.devboost.eclipse.jdtutilities.ui;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
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

// FIXME Remove code duplication
@SuppressWarnings("restriction")
public class JavaEditorHelper {

	public void switchToJavaEditor(IType type, String methodName,
			List<String> parameterTypes) {
		
		ICompilationUnit unit = type.getCompilationUnit();
		IEditorPart part = null;
		if (unit.getResource().exists()) {
			part = EditorUtility.isOpenInEditor(unit);
			if (part == null) {
				try {
					part = JavaUI.openInEditor(unit);
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
		}
		
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
		
		if (part instanceof JavaEditor) {
			JavaEditor javaEditor = (JavaEditor) part;
			javaEditor.setSelection(newMethod);
		}
	}

	public void switchToJavaEditor(ICompilationUnit unit,
			int lineNumber) {
		
		IEditorPart part = null;
		if (unit.getResource().exists()) {
			part = EditorUtility.isOpenInEditor(unit);
			if (part == null) {
				try {
					part = JavaUI.openInEditor(unit);
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
		}
		
		if (part instanceof JavaEditor) {
			JavaEditor javaEditor = (JavaEditor) part;
			IDocument document = javaEditor.getDocumentProvider().getDocument(
					javaEditor.getEditorInput());
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
}
