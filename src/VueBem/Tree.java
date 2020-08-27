package VueBem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;

public class Tree implements Iterable<Tree> {
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
        printTreeRecursive(0);
    }

    private void printTreeRecursive(int index) {
        for (int i=0; i < index; i++) {
            System.out.print("      ");
        }
        System.out.print(this.data + " { " + this.content + "}" + "\n");
        if(this.children.size() == 0) return;
        for (Tree child : this.children) {
            for (int i=0; i < index; i++) {
                System.out.print("      ");
            }
            printTreeRecursive(index+1);
        }

    }

    @NotNull
    @Override
    public Iterator<Tree> iterator() {
        return new TreeIterator(this);
    }

    static class TreeIterator implements Iterator<Tree> {
        Tree nextTree;
        Tree previousTree;
        int treeIndex = 0;

        public TreeIterator(Tree start) {
            nextTree = start;
            while (nextTree.children.size() > 0) {
                nextTree = nextTree.children.get(0);
            }
        }

        @Override
        public boolean hasNext() {
            return nextTree != null;
        }

        @Override
        public Tree next() {
            var result = nextTree;

            if(nextTree.children.size() == 0) {
                var parent = nextTree.parent;
                treeIndex = parent.children.indexOf(nextTree);
                if(treeIndex+1 < parent.children.size())
                    nextTree = parent.children.get(treeIndex+1);
                else
                    nextTree = parent;
            }
            else {
                treeIndex = nextTree.children.indexOf(previousTree);
                if(treeIndex < 0)
                    nextTree = nextTree.children.get(0);
                else
                    while (nextTree.children.size() > 0) {
                        nextTree = nextTree.children.get(0);
                    }
            }

            result = previousTree;
            return result;
        }

        // Файл только для чтения, мы не разрешаем удаление строк
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
