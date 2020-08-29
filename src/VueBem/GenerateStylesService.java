package VueBem;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;

public class GenerateStylesService {

    public static ArrayList<CssClassTree> uniteTrees(ArrayList<CssClassTree> to, ArrayList<CssClassTree> from, int index) {
        for (var result : to) {
            for (var item : from) {
                if (result.data.equals(item.data)) {
                    result.children = uniteTrees(result.children, item.children, index++);
                } else {
                    if (result.parent != null) {
                        result.parent.addChild(item);
                    }
                }
            }
        }

        return to;
    }

    public static ArrayList<String> getClassesFromTemplate(PsiFile editor) {

        String text = editor.getText();
        String startTag = "<template>";
        String endTag = "</template>";
        int startIndex = text.indexOf(startTag);
        int endIndex = text.lastIndexOf(endTag);
        String template = text.substring(startIndex, endIndex + endTag.length());

        ArrayList<String> result = new ArrayList<>();

        String[] words = template.replace("\n", "").split(" ");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.length() == 0) continue;

            if (word.contains("<!--")) {
                while (!word.contains("-->")) {
                    i++;
                    word = words[i];
                }
                continue;
            }

            if (word.contains("class=\"{")) {
                StringBuilder difClass = new StringBuilder();
                for (int j = i; j < words.length; j++, i++) {
                    word = words[j];
                    difClass.append(" ").append(word);
                    if (word.contains("}")) {
                        break;
                    }
                }
                String[] difficultWords = difClass.toString().split("[:,]");
                for (int k = 0; k < difficultWords.length; k++) {
                    if (k % 2 != 0) {
                        result.add(getDifficultWord(difficultWords[k]));
                    }
                }
            }

            if (word.contains("class=\"")) {
                for (int j = i; j < words.length; j++, i++) {
                    word = words[j];
                    if (word.length() == 0) continue;
                    result.add(getWord(word));
                    char c = word.toCharArray()[word.length() - 1];
                    if (c == '\"' || c == '>') {
                        break;
                    }
                }
            }
        }

        return result;
    }

    public static ArrayList<CssClassTree> getStyleFromTemplate(PsiFile editor) {

        String text = editor.getText();
        String startTag = "<style";
        String endTag = "</style>";
        int startIndex = text.indexOf(startTag);
        int endIndex = text.indexOf(endTag);
        String template = text.substring(startIndex, endIndex + endTag.length());

        String[] words = template.replaceAll("\n", " ").replaceAll("\\s+", " ").split(" ");

        ArrayList<CssClassTree> result = new ArrayList<>();
        int index = 0;
        while (index < words.length - 1) {
            Pair pair = recursiveStyleParser(words, index);
            if (pair.value != null)
                result.add(pair.value);
            index = pair.index + 1;
        }

        return result;
    }

    public static ArrayList<CssClassTree> parseClassesToTree(ArrayList<String> classes) {
        ArrayList<CssClassTree> result = new ArrayList<>();

        for (String item : classes) {
            String[] levelOne = item.split("__");
            String rootClass = "";
            String child = "";
            String childChild = "";

            if (levelOne[0] != null) {
                rootClass = "." + levelOne[0];
            }
            if (levelOne.length > 1) {
                String[] levelTwo = levelOne[1].split("_");
                if (levelTwo[0] != null) {
                    child = "&__" + levelTwo[0];
                }
                if (levelTwo.length > 1) {
                    childChild = "&_" + levelTwo[1];
                }
            }

            CssClassTree tree = findTree(result, rootClass);
            if (tree == null) {
                result.add(new CssClassTree(rootClass));
            } else if(!child.equals("")) {
                CssClassTree childrenTree = findTree(tree.children, child);
                if (childrenTree == null) {
                    tree.addChild(new CssClassTree(child));
                } else if(!childChild.isEmpty()) {
                    CssClassTree childrenChildrenTree = findTree(childrenTree.children, childChild);
                    if (childrenChildrenTree == null) {
                        childrenTree.addChild(new CssClassTree(childChild));
                    }
                }
            }

        }

        return result;
    }

    private static String getWord(String word) {
        String startTag = "class=\"";
        int startIndex = word.indexOf(startTag);
        if (startIndex >= 0) {
            word = word.substring(startIndex + startTag.length(), word.length());
        }
        int lastIndex = word.indexOf("\"");
        if (lastIndex >= 0) {
            word = word.substring(0, lastIndex);
        }
        return word.replaceAll("\\s+", "");
    }

    private static String getDifficultWord(String word) {
        String startTag = "class=\"{";
        int startIndex = word.indexOf(startTag);
        if (startIndex >= 0) {
            word = word.substring(startIndex + startTag.length(), word.length());
        }
        return word.replace("'", "").replaceAll("\\s+", "");
    }

    private static Pair recursiveStyleParser(String[] words, int index) {
        CssClassTree root = new CssClassTree();
        int i = index;
        for (; i < words.length; i++) {
            if (i == 0) continue;
            String word = words[i];
            StringBuilder previousWord = new StringBuilder(words[i - 1]);

            if (word.equals("@media")) {
                previousWord = new StringBuilder();
                for (int j = i; j < words.length; j++, i++) {
                    word = words[j];
                    if (word.equals("{")) break;
                    previousWord.append(" ").append(word);
                }
                word = words[i];
            }

            if (word.equals("}")) {
                return new Pair(root, i);
            }

            if (word.equals("{") && root.data.equals("")) {
                root.data = previousWord.toString();
                root.content = "";
                continue;
            }

            if (word.equals("{")) {
                Pair res = recursiveStyleParser(words, i);
                i = res.index;
                CssClassTree value = res.value;
                value.setData(previousWord.toString());
                root.addChild(value);
                continue;
            }

            if (i + 1 < words.length - 1 && !words[i + 1].equals("{")) {
                root.content = root.content + " " + word;
            }
        }
        return new Pair(null, i);
    }

    private static CssClassTree findTree(ArrayList<CssClassTree> list, String rootClass) {
        for (CssClassTree item : list) {
            if (item.data.equals(rootClass)) {
                return item;
            }
        }
        return null;
    }

    public static CssClassTree removeEmptyNodes(CssClassTree item) {

        if(item.children.size() > 0) {
            ArrayList<CssClassTree> childrens = new ArrayList<>();
            for (var child : item.children) {
                CssClassTree childResult = removeEmptyNodes(child);

                if (childResult != null) {
                    childrens.add(childResult);
                }
            }
            item.children = childrens;
        }

        if(item.content.isEmpty() && item.children.size() == 0) {
            return null;
        }

        return item;
    }

    public static void replaceStylesInDocument(ArrayList<CssClassTree> classesTree, AnActionEvent event) {
        final Project project = event.getData(PlatformDataKeys.PROJECT);
        PsiFile file = event.getData(LangDataKeys.PSI_FILE);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
            Document document = documentManager.getDocument(file);

            String result = "";
            for(var item : classesTree) {
                result = result + item.toString();
            }

            String startTag = "<style";
            String endTag = "</style>";
            var text =  document.getText();
            int startIndex = text.indexOf(startTag);
            int endIndex = text.indexOf(endTag);
            int startStyleBlock = document.getText().indexOf("\n", startIndex);
            document.replaceString(startStyleBlock, endIndex, "\n" + result + "\n");
        });
    }

    public static void writeTreeAsStyleInDocument(ArrayList<CssClassTree> classesTree, AnActionEvent event) {
        final Project project = event.getData(PlatformDataKeys.PROJECT);
        PsiFile file = event.getData(LangDataKeys.PSI_FILE);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
            Document document = documentManager.getDocument(file);

            String result = "";
            for(var item : classesTree) {
                result = result + item.toString();
            }

            String startTag = "<style";
            int i = document.getText().indexOf(startTag);
            int startStyleBlock = document.getText().indexOf("\n", i);

            document.insertString(startStyleBlock, "\n" + result);
        });
    }
}