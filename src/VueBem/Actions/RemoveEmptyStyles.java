package VueBem.Actions;

import VueBem.GenerateStylesService;
import VueBem.CssClassTree;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class RemoveEmptyStyles extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        final Editor editor = event.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;

        PsiFile file = event.getData(LangDataKeys.PSI_FILE);
        if (file == null) return;

        ArrayList<CssClassTree> styles = GenerateStylesService.getStyleFromTemplate(file);
        ArrayList<CssClassTree> res = new ArrayList<>();
        for (var item : styles) {
            CssClassTree clearedTree = GenerateStylesService.removeEmptyNodes(item);
            if(clearedTree != null) {
                res.add(clearedTree);
            }
        }

        GenerateStylesService.replaceStylesInDocument(res, event);

    }
}