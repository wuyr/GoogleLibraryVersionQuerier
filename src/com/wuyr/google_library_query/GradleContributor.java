package com.wuyr.google_library_query;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

public class GradleContributor extends CompletionContributor {

    private boolean needShow = false;

    @Override
    public void beforeCompletion(@NotNull CompletionInitializationContext context) {
        needShow = context.getFile().getName().equals("build.gradle");
    }

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        if (needShow) {
            com.intellij.openapi.editor.Document document = parameters.getEditor().getDocument();
            CaretModel caretModel = parameters.getEditor().getCaretModel();

            int lineStart = caretModel.getVisualLineStart();
            int lineEnd = caretModel.getVisualLineEnd();
            String currentLine = document.getText(new TextRange(lineStart, lineEnd)).replaceAll("\n", "");
            // TODO: 2020/5/5 先不用去空格，检测空格前面有impl就不用再加imple
            //.replaceAll("\\s+", "")

            System.out.println(currentLine);

            result.addElement(LookupElementBuilder.create("implementation 'androidx.appcompat:appcompat:1.2.0-beta01'"));
            result.addElement(LookupElementBuilder.create("implementation 'androidx.asynclayoutinflater:asynclayoutinflater:1.0.0'"));
            result.addElement(LookupElementBuilder.create("implementation 'androidx.recyclerview:recyclerview:1.2.0-alpha03'"));
            result.addElement(LookupElementBuilder.create("implementation 'androidx.cardview:cardview:1.0.0'"));
            result.addElement(LookupElementBuilder.create("implementation 'com.google.android.material:material:1.2.0-alpha06'"));

            needShow = false;
        }
        super.fillCompletionVariants(parameters, result);
    }
}

