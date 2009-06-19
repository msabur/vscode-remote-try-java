package com.sun.tools.javac.parser;

import java.io.PrintStream;
import java.util.Map;

import org.jmlspecs.openjml.JmlTreeScanner;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;

/**
 * This class walks a JML AST printing debug information about each node. Note
 * that it needs a reference to the parser in order to get position information.
 * 
 * @author David Cok
 */
public class TreePrinter extends JmlTreeScanner {
    
    /** The current indentation level */
    String indent = "";
    
    /** The output stream */
    PrintStream out;
    
    /** The end position map */
    Map<JCTree, Integer> endPositions;
    
    /** A constructor for the tree
     * @param out where to write the output information
     * @param p the parser with the position information
     */
    public TreePrinter(PrintStream out, Map<JCTree, Integer> endPositions) {
        this.out = out;
        this.endPositions = endPositions;
    }
    
    /** Implements the superclass scan method to print out a line of information about
     * argument and then proceed to scan its children
     */
    public void scan(JCTree t) {
        if (t == null) return;
        out.println(indent + t.getClass() 
                + " " + t.getTag() 
                + " " + t.getStartPosition() 
                + " " + (endPositions==null?"":Integer.toString(TreeInfo.getEndPos(t, endPositions))));
        String oldindent = indent;
        indent = indent + "  ";
        super.scan(t);
        indent = oldindent;
    }
}