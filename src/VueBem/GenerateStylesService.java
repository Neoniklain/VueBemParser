package VueBem;

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;

import java.util.ArrayList;

class GenerateStylesService {

    static void generateMethods(AnActionEvent event) {
        final Project project = event.getData(PlatformDataKeys.PROJECT);
        final Editor editor = event.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;

        final CaretModel caret = editor.getCaretModel();

        PsiFile file = event.getData(LangDataKeys.PSI_FILE);
        if (file == null) return;

        ArrayList<String> classes = getClassesFromTemplate(file);
        ArrayList<Tree> styles = getStyleFromTemplate(file);

        ArrayList<Tree> classesTree = parseClassesToTree(classes);

        WriteCommandAction.runWriteCommandAction(project, () -> {

            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
            Document document = documentManager.getDocument(file);

            String result = "";
            for(var item : classesTree) {
                result = result + item.toString();
            }

            document.insertString(caret.getOffset(), result);
        });

//        System.out.println("styles: " + styles.size());
//        for (Tree item : styles) {
//            item.printToConsole();
//        }
//
//        System.out.println("classesTree: ");
//        for (Tree item : classesTree) {
//            item.printToConsole();
//        }
//
//        ArrayList<Tree> trees = uniteTrees(styles, classesTree, 0);
//
//        System.out.println("trees: ");
//        for (Tree item : trees) {
//            item.printToConsole();
//        }

    }

    public static ArrayList<Tree> uniteTrees(ArrayList<Tree> to, ArrayList<Tree> from, int index) {
        for (var result: to) {
            for (var item: from) {
                if(result.data.equals(item.data)) {
                    System.out.println("Iter " + index);
                    result.children = uniteTrees(result.children, item.children, index++);
                }
                else {
                    if(result.parent != null) {
                        System.out.println("New child " + item.data);
                        result.parent.addChild(item);
                    }
                }
            }
        }

        return to;
    }

    private static ArrayList<String> getClassesFromTemplate(PsiFile editor) {

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
                    if(word.length() == 0) continue;
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

    private static ArrayList<Tree> getStyleFromTemplate(PsiFile editor) {

        String text = editor.getText();
        String startTag = "<style";
        String endTag = "</style>";
        int startIndex = text.indexOf(startTag);
        int endIndex = text.indexOf(endTag);
        String template = text.substring(startIndex, endIndex + endTag.length());

        String[] words = template.replaceAll("\n", " ").replaceAll("\\s+", " ").split(" ");

        ArrayList<Tree> result = new ArrayList<>();
        int index = 0;
        while(index < words.length - 1) {
            Pair pair = recursiveStyleParser(words, index);
            if(pair.value != null)
                result.add(pair.value);
            index = pair.index + 1;
        }

        return result;
    }

    private static Pair recursiveStyleParser(String[] words, int index) {
        Tree root = new Tree();
        int i = index;
        for (; i < words.length; i++) {
            if(i == 0) continue;
            String word = words[i];
            StringBuilder previousWord = new StringBuilder(words[i - 1]);

            if(word.equals("@media")) {
                previousWord = new StringBuilder();
                for (int j = i; j < words.length; j++, i++) {
                    word = words[j];
                    if(word.equals("{")) break;
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
                Tree value = res.value;
                value.setData(previousWord.toString());
                root.addChild(value);
                continue;
            }

            if (i+1 < words.length - 1 && !words[i+1].equals("{")) {
                root.content = root.content + " " + word;
            }
        }
        return new Pair(null ,i);
    }

    private static ArrayList<Tree> parseClassesToTree(ArrayList<String> classes) {
        ArrayList<Tree> result = new ArrayList<>();

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
                    childChild =  "&_" + levelTwo[1];
                }
            }

//            System.out.println("root child childChild " + rootClass + " " + child + " " + childChild);

            Tree tree = findTree(result, rootClass);
            if(tree == null) {
                result.add(new Tree(rootClass));
            } else {
                Tree childrenTree = findTree(tree.children, child);
                if(childrenTree == null && !child.equals("")) {
                    tree.addChild(new Tree(child));
                } else {
                    Tree childrenChildrenTree = findTree(tree.children, childChild);
                    if(childrenChildrenTree == null && !childChild.equals("")) {
                        childrenTree.addChild(new Tree(childChild));
                    }
                }
            }

        }

        return result;
    }

    private static Tree findTree(ArrayList<Tree> list, String rootClass) {
        for (Tree item : list) {
            if (item.data.equals(rootClass)) {
                return item;
            }
        }
        return null;
    }

}