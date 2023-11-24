package edu.cqu.zhipeng.org.antlr;

import edu.cqu.zhipeng.org.antlr.c.CLexer;
import edu.cqu.zhipeng.org.antlr.c.CParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Xzzz
 * @data 2023/07/02
 */
public class testc {
    private String path;

    public testc(String path) {
        this.path = path;
    }

    static List<ParseTree> methodNode = new ArrayList<>();
    public static void main(String[] args) throws Exception {
        String path = "D:\\project\\chapGPT\\src\\test\\files\\";
        CharStream charStream = CharStreams.fromFileName(path);
        CLexer lexer = new CLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CParser parser = new CParser(tokens);
        ParserRuleContext tree = parser.compilationUnit();
        int lineNum=7;
        testc test = new testc(path);
        test.generateAST(tree,false,0,tokens,lineNum);
//        List<String> list = readCppFile(path);
//
//        FunctionDefinitionList(tree,methodNode);
//        for (ParseTree function : methodNode) {
//
//            String block = getBlock(tokens, list, function);
//            System.out.println(block);
//        }


    }
    private  void generateAST(RuleContext ctx, boolean verbose, int indentation, CommonTokenStream tokens,int lineNum) {
        boolean toBeIgnored = !verbose && ctx.getChildCount() == 1 && ctx.getChild(0) instanceof ParserRuleContext;
        if (!toBeIgnored) {

            int startLine = getStartLine(tokens, ctx);
            int endLine = getEndLine(tokens, ctx);
            String ruleName = CParser.ruleNames[ctx.getRuleIndex()];


            if (startLine==lineNum)

            {
                System.out.println(ctx.getParent().getClass());
                if(returnNodeTypeForWhole(ctx))
            {
                try {
                    String body = getBody(startLine, endLine);
                    System.out.println(body);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }

            }





        }
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree element = ctx.getChild(i);
            if (element instanceof RuleContext) {
                generateAST((RuleContext) element, verbose, indentation + (toBeIgnored ? 0 : 1), tokens,lineNum);
            }
        }
    }

    private static boolean returnNodeTypeForWhole(RuleContext ctx) {
        if (ctx instanceof CParser.SelectionStatementContext||
                ctx instanceof CParser.IterationStatementContext||
                ctx instanceof CParser.StructOrUnionContext||
                ctx instanceof CParser.DeclarationContext||
                ctx instanceof CParser.ExpressionStatementContext
                )
        {
            return true;
        }
        else{
            return false;
        }
    }

    private static String getBlock(CommonTokenStream tokens, List<String> list, ParseTree function) {

        int startLine = getStartLine(tokens, function);
        int endLine = getEndLine(tokens, function);
        StringBuilder stringBuilder = new StringBuilder();
        List<String> collect = list.stream().skip(startLine-1).limit(endLine - startLine + 1).collect(Collectors.toList());
        for (String s : collect) {
            stringBuilder.append(s).append("\n");
        }
        return stringBuilder.toString();
    }

    private static int getStartLine(CommonTokenStream tokens, ParseTree function) {
        Interval sourceInterval = function.getSourceInterval();
        Token tokenBegin = tokens.get(sourceInterval.a);
        return tokenBegin.getLine();
    }
    private static int getEndLine(CommonTokenStream tokens, ParseTree function) {
        Interval sourceInterval = function.getSourceInterval();
        Token tokenEnd = tokens.get(sourceInterval.b);
        return tokenEnd.getLine();
    }


    public static void FunctionDefinitionList(ParseTree node, List<ParseTree> methodNode) {
       if(node instanceof CParser.FunctionDefinitionContext)
       {
           methodNode.add(node);
       }else {
           for (int i = 0; i < node.getChildCount(); i++) {
               FunctionDefinitionList(node.getChild(i), methodNode);
           }
       }

    }

    public  String getBody(int startLine,int endLine) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        List<String> collect = lines.stream().skip(startLine-1).limit(endLine - startLine + 1).collect(Collectors.toList());
        for (String s : collect) {
            stringBuilder.append(s).append("\n");
        }
        return stringBuilder.toString();
    }



}
