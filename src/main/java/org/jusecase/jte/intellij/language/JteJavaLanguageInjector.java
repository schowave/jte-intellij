package org.jusecase.jte.intellij.language;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.psi.*;

import java.util.Collections;
import java.util.List;

public class JteJavaLanguageInjector implements MultiHostInjector {
    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        if (context instanceof JtePsiJavaCodeElement) {
            JtePsiJavaCodeElement host = (JtePsiJavaCodeElement)context;

            registrar.startInjecting(StdFileTypes.JAVA.getLanguage());

            boolean hasWrittenClass = false;

            for (PsiElement child : host.getChildren()) {
                if (child instanceof JtePsiImport) {
                    JtePsiJavaImport javaPart = PsiTreeUtil.getChildOfType(child, JtePsiJavaImport.class);
                    if (javaPart != null) {
                        injectJavaPart("import ", ";\n", registrar, host, javaPart);
                    }
                } else if (child instanceof JtePsiParam) {
                    JtePsiJavaParam javaPart = PsiTreeUtil.getChildOfType(child, JtePsiJavaParam.class);
                    if (javaPart != null) {
                        if (!hasWrittenClass) {
                            String classPrefix = "class DummyTemplate { public void render(String output, ";
                            JtePsiParam nextParam = PsiTreeUtil.getNextSiblingOfType(child, JtePsiParam.class);
                            if (nextParam != null) {
                                injectJavaPart(classPrefix, null, registrar, host, javaPart);
                            } else {
                                injectJavaPart(classPrefix, ") {", registrar, host, javaPart);
                            }
                            hasWrittenClass = true;
                        } else {
                            JtePsiParam nextParam = PsiTreeUtil.getNextSiblingOfType(child, JtePsiParam.class);
                            if (nextParam != null) {
                                injectJavaPart(", ", null, registrar, host, javaPart);
                            } else {
                                injectJavaPart(", ", ") {", registrar, host, javaPart);
                            }
                        }
                    }
                } else if (child instanceof JtePsiOutput) {
                    JtePsiJavaOutput javaPart = PsiTreeUtil.getChildOfType(child, JtePsiJavaOutput.class);
                    injectJavaPart("output = ", ";\n", registrar, host, javaPart);
                }
            }

            if (hasWrittenClass) {
                registrar.addPlace(null, "}}", host, new TextRange(host.getTextLength(), host.getTextLength()));
            }

            registrar.doneInjecting();
        }
    }

    private void injectJavaPart(String prefix, String suffix, MultiHostRegistrar registrar, JtePsiJavaCodeElement host, JtePsiElement javaPart) {
        int startOffsetInHost = getStartOffsetInHost(host, javaPart);
        registrar.addPlace(prefix, suffix, host, new TextRange(startOffsetInHost, startOffsetInHost + javaPart.getTextLength()));
    }

    private int getStartOffsetInHost(JtePsiJavaCodeElement host, PsiElement node) {
        int result = node.getStartOffsetInParent();
        while (node != host) {
            node = node.getParent();
            result += node.getStartOffsetInParent();
        }
        return result;
    }

    @NotNull
    @Override
    public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return Collections.singletonList(JtePsiJavaCodeElement.class);
    }
}
