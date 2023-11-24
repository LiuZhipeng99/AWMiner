package edu.cqu.zhipeng.org.antlr;

import edu.cqu.zhipeng.org.antlr.cpp.CPP14Lexer;
import edu.cqu.zhipeng.org.antlr.cpp.CPP14Parser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * @author Xzzz
 * @data 2023/07/02
 */
public class cppApp {

    static ArrayList<String> LineNum = new ArrayList<String>();
    static ArrayList<String> Type = new ArrayList<String>();
    static ArrayList<String> Content = new ArrayList<String>();
    static ArrayList<String> RawLineNum = new ArrayList<String>();
    static ArrayList<String>  parent = new ArrayList<>();
    private static String readFile(String pathname) throws IOException {
        File file = new File(pathname);
        byte[] encoded = Files.readAllBytes(file.toPath());
        return new String(encoded, Charset.forName("UTF-8"));
    }
    public static void main(String args[]) throws IOException {
        String path = "D:\\project\\chapGPT\\src\\test\\files\\misra-test.c";
        CharStream charStream = CharStreams.fromFileName(path);
        CPP14Lexer cpp14Lexer = new CPP14Lexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(cpp14Lexer);
        CPP14Parser cpp14Parser = new CPP14Parser(tokens);
        CPP14Parser.TranslationUnitContext ctx = cpp14Parser.translationUnit();

//      ParserRuleContext ctx = parser.statementExpressionList();
//      ParserRuleContext ctx = parser.methodDeclaration();

        generateAST(ctx, false, 0, tokens);
        String filename = path.substring(path.lastIndexOf("\\") + 1, path.lastIndexOf("."));
        String save_dot_filename = String.format("D:\\project\\chapGPT\\src\\test\\files\\ast_%s.dot", filename);
        PrintWriter writer = new PrintWriter(save_dot_filename);
        writer.println(String.format("digraph %s {", filename));
        printDOT(writer);
        writer.println("}");
        writer.close();
    }

    private static void generateAST(RuleContext ctx, boolean verbose, int indentation, CommonTokenStream tokens) {
        boolean toBeIgnored = !verbose && ctx.getChildCount() == 1 && ctx.getChild(0) instanceof ParserRuleContext;
        if (!toBeIgnored) {


            System.out.println(ctx.getClass().getSimpleName());
            System.out.println(ctx.getText());


            // get line number, added by tsmc.sumihui, 20190425
            Interval sourceInterval = ctx.getSourceInterval();
            Token firstToken = tokens.get(sourceInterval.a);
            int a = sourceInterval.a;
            int lineNum = firstToken.getLine();
            RawLineNum.add(Integer.toString(lineNum));

        }
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree element = ctx.getChild(i);
            if (element instanceof RuleContext) {
                generateAST((RuleContext) element, verbose, indentation + (toBeIgnored ? 0 : 1), tokens);
            }
        }
    }

    private static void printDOT(PrintWriter writer) {
        printLabel(writer);
        int pos = 0;
        for (int i = 1; i < LineNum.size(); i++) {
            pos = getPos(Integer.parseInt(LineNum.get(i)) - 1, i);
            writer.println((Integer.parseInt(LineNum.get(i)) - 1) + Integer.toString(pos) + "->" + LineNum.get(i) + i);
        }
    }

    private static void printLabel(PrintWriter writer) {
        for (int i = 0; i < LineNum.size(); i++) {
//          writer.println(LineNum.get(i)+i+"[label=\""+Type.get(i)+"\\n "+Content.get(i)+" \"]");
            writer.println(LineNum.get(i) + i + "[label=\"" + Type.get(i) + "\", linenum=\"" + RawLineNum.get(i) + "\"]"+"\\n "+Content.get(i));

        }
    }

    private static int getPos(int n, int limit) {
        int pos = 0;
        for (int i = 0; i < limit; i++) {
            if (Integer.parseInt(LineNum.get(i)) == n) {
                pos = i;
            }
        }
        return pos;
    }

}
