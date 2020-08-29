package VueBem.Actions;

import VueBem.GenerateStylesService;
import VueBem.CssClassTree;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ParseHtmlService extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        final Project project = event.getData(PlatformDataKeys.PROJECT);
        final Editor editor = event.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;

//        final CaretModel caret = editor.getCaretModel();

        PsiFile file = event.getData(LangDataKeys.PSI_FILE);
        if (file == null) return;

        ArrayList<String> classes = GenerateStylesService.getClassesFromTemplate(file);
//        ArrayList<Tree> styles = GenerateStylesService.getStyleFromTemplate(file);

        ArrayList<CssClassTree> classesTree = GenerateStylesService.parseClassesToTree(classes);

        GenerateStylesService.writeTreeAsStyleInDocument(classesTree, event);

    }
}