package VueBem;


import java.util.ArrayList;

public class CssClassTree {
    String data;
    String content;
    ArrayList<CssClassTree> children;
    CssClassTree parent;

    public CssClassTree() {
        this.data = "";
        this.content = "";
        this.children = new ArrayList<CssClassTree>();
    }

    public CssClassTree(String data)
    {
        this.data = data;
        this.content = "";
        this.children = new ArrayList<CssClassTree>();
    }

    public void addChild(CssClassTree child) {
        child.parent = this;
        this.children.add(child);
    }

    public void addChildes(ArrayList<CssClassTree> child) {
        for (var item : child) {
            item.parent = this;
            addChild(item);
        }
    }

    public void setData(String newData) {
        this.data = newData;
    }

    public void printToConsole() {
        printTreeRecursive(this, 0);
    }

    public String toString() {
        return printTree(this, 0);
    }

    private String printTree(CssClassTree item, int index) {
        StringBuilder result = new StringBuilder();
        result.append("\n").append("  ".repeat(Math.max(0, index)));
        result.append(item.data).append(" {\n");
        if(!item.content.equals("")) {
            result.append("  ".repeat(Math.max(0, index+1)));
            result.append(item.content.trim()
                    .replace("; ", ";\n" + "  ".repeat(Math.max(0, index+1)))
            );
        }

        if(item.children.size() == 0) {
            result.append("\n").append("  ".repeat(Math.max(0, index)));
            return result.append("}\n").toString();
        } else {
            result.append("\n");
        }

        for (CssClassTree child : item.children) {
            result.append("  ".repeat(Math.max(0, index)));
            result.append(printTree(child, index + 1));
        }
        return result.append("  ".repeat(Math.max(0, index))).append("}\n").toString();
    }

    private void printTreeRecursive(CssClassTree item, int index) {
        for (int i=0; i < index; i++) {
            System.out.print("      ");
        }
        System.out.print(item.data + " { " + item.content + "}" + "\n");
        if(item.children.size() == 0) return;
        for (CssClassTree child : item.children) {
            for (int i=0; i < index; i++) {
                System.out.print("      ");
            }
            printTreeRecursive(child, index+1);
        }
    }

}
