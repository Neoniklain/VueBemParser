package VueBem;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ParseHtmlService extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        GenerateStylesService.generateMethods(event);
    }
}