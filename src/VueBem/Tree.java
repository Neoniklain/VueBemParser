package VueBem;


import java.util.ArrayList;

public class Tree {
    String data;
    String content;
    ArrayList<Tree> children;
    Tree parent;

    public Tree() {
        this.data = "";
        this.content = "";
        this.children = new ArrayList<Tree>();
    }

    public Tree(String data)
    {
        this.data = data;
        this.content = "";
        this.children = new ArrayList<Tree>();
    }

    public void addChild(Tree child) {
        child.parent = this;
        this.children.add(child);
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

    private String printTree(Tree item, int index) {
        StringBuilder result = new StringBuilder();
        result.append("\n").append("  ".repeat(Math.max(0, index)));
        result.append(item.data).append(" {\n");
        if(!item.content.equals("")) {
            result.append(item.content).append("\n");
        }
        if(item.children.size() == 0) {
            result.append("\n").append("  ".repeat(Math.max(0, index)));
            return result.append("}\n").toString();
        }
        for (Tree child : item.children) {
            result.append("  ".repeat(Math.max(0, index)));
            result.append(printTree(child, index + 1));
        }
        return result.append("  ".repeat(Math.max(0, index))).append("}\n").toString();
    }

    private void printTreeRecursive(Tree item, int index) {
        for (int i=0; i < index; i++) {
            System.out.print("      ");
        }
        System.out.print(item.data + " { " + item.content + "}" + "\n");
        if(item.children.size() == 0) return;
        for (Tree child : item.children) {
            for (int i=0; i < index; i++) {
                System.out.print("      ");
            }
            printTreeRecursive(child, index+1);
        }
    }
}
