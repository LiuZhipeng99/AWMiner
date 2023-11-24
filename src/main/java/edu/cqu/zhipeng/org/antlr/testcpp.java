package edu.cqu.zhipeng.org.antlr;

import edu.cqu.zhipeng.org.antlr.c.CParser;
import edu.cqu.zhipeng.org.antlr.cpp.CPP14Lexer;
import edu.cqu.zhipeng.org.antlr.cpp.CPP14Parser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;


import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Xzzz
 * @data 2023/07/02
 */
public class testcpp {

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
        String path = "D:\\project\\chapGPT\\src\\test\\files\\test.cpp";
        CharStream charStream = CharStreams.fromFileName(path);
        CPP14Lexer cpp14Lexer = new CPP14Lexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(cpp14Lexer);
        CPP14Parser cpp14Parser = new CPP14Parser(tokens);
        CPP14Parser.TranslationUnitContext ctx = cpp14Parser.translationUnit();

//      ParserRuleContext ctx = parser.statementExpressionList();
//      ParserRuleContext ctx = parser.methodDeclaration();
        int lineNum =17;
        generateAST(ctx, false, 0, tokens,17);


    }

    private static void generateAST(RuleContext ctx, boolean verbose, int indentation, CommonTokenStream tokens, int lineNum) {
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
        if (ctx instanceof CPP14Parser.SelectionStatementContext||
                ctx instanceof CPP14Parser.IterationStatementContext||
                ctx instanceof CPP14Parser.SimpleDeclarationContext||
                ctx instanceof CPP14Parser.DeclarationContext||
                ctx instanceof CPP14Parser.ExpressionStatementContext||
                ctx instanceof  CPP14Parser.LabeledStatementContext
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

        // Check if the source interval is valid
        if (sourceInterval.a >= 0 && sourceInterval.b >= 0) {
            Token tokenEnd = tokens.get(sourceInterval.b);
            return tokenEnd.getLine();
        } else {
            // Return an error value or throw an exception to indicate an invalid source interval
            return -1;
        }
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

    public static String getBody(int startLine, int endLine) throws IOException {
        List<String> lines = new ArrayList<>();
        String path = null;
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
