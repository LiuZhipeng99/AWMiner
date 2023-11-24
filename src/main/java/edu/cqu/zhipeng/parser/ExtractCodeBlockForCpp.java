package edu.cqu.zhipeng.parser;

import edu.cqu.zhipeng.org.antlr.c.CParser;
import lombok.Data;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
//import path.to.c.CParser;

import edu.cqu.zhipeng.org.antlr.cpp.CPP14Lexer;
import edu.cqu.zhipeng.org.antlr.cpp.CPP14Parser;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Xzzz
 * @data 2023/07/11
 */
@Data
public class ExtractCodeBlockForCpp {
    private String path;
    private String code;
    private Integer startLine;
    private Integer endLine;

    private HashMap<Integer,List<RuleContext>> lineForToken= new HashMap<>();
    public ExtractCodeBlockForCpp(String path) {
        this.path = path;
    }

    public String getCodeBlockByAst(int lineNum)
    {
        CharStream charStream = null;
        try {
            charStream = CharStreams.fromFileName(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        CPP14Lexer lexer = new CPP14Lexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CPP14Parser parser = new CPP14Parser(tokens);
        ParserRuleContext tree = parser.translationUnit();
        return traverseAllNodeToFindCodeLine(tree,false,0,tokens,lineNum);

    }

    public String traverseAllNodeToFindCodeLine_gpt(RuleContext ctx, boolean verbose, int indentation, CommonTokenStream tokens, int lineNum) {
        boolean toBeIgnored = !verbose && ctx.getChildCount() == 1 && ctx.getChild(0) instanceof ParserRuleContext;
        int startLine = getStartLine(tokens, ctx);
        int endLine = getEndLine(tokens, ctx);
        if (!toBeIgnored) {
            if (startLine <= lineNum && lineNum <= endLine) {
                try {
                    String body = getBody(startLine, endLine);
                    this.code = body;
                    this.startLine = startLine;
                    this.endLine = endLine;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return "200";
            }
            if (ctx instanceof CPP14Parser.ClassSpecifierContext ||
                    ctx instanceof CPP14Parser.FunctionDefinitionContext ||
                    ctx instanceof CPP14Parser.TemplateDeclarationContext ||
                    ctx instanceof CPP14Parser.NamespaceDefinitionContext ||
                    ctx instanceof CPP14Parser.SelectionStatementContext ||
                    ctx instanceof CPP14Parser.IterationStatementContext) {
                return "201";
            }
        }
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree element = ctx.getChild(i);
            if (element instanceof RuleContext) {
                String status = traverseAllNodeToFindCodeLine_gpt((RuleContext) element, verbose, indentation + (toBeIgnored ? 0 : 1), tokens, lineNum);
                if ("200".equals(status)) {
                    return "200";
                }
                else if ("201".equals(status)) {
                    int preStartLine = getStartLine(tokens, ctx);
                    int preEndLine = getEndLine(tokens, ctx);
                    try {
                        this.code = getBody(preStartLine, preEndLine);
                        this.startLine = preStartLine;
                        this.endLine = preEndLine;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return "200";
                }
            }
        }

        return "404";
    }
    public String traverseAllNodeToFindCodeLine(RuleContext ctx, boolean verbose, int indentation, CommonTokenStream tokens, int lineNum){
        boolean toBeIgnored = !verbose && ctx.getChildCount() == 1 && ctx.getChild(0) instanceof ParserRuleContext;
        int startLine = getStartLine(tokens, ctx);
        int endLine = getEndLine(tokens, ctx);
        if (!toBeIgnored) {
//            if (lineForToken.get(startLine)!=null)
//            {
//                List<RuleContext> ruleContexts = lineForToken.get(startLine);
//                ruleContexts.add(ctx);
//            }else {
//                ArrayList<RuleContext> list = new ArrayList<>();
//                list.add(ctx);
//                lineForToken.put(lineNum,list);
//            }

            if (startLine==lineNum)
            {
//                System.out.println(ctx.getClass().getSimpleName());
                if(returnNodeTypeForWhole(ctx))
                {
                    try {

                        String body = getBody(startLine, endLine);

                        this.code=body;
                        this.startLine=startLine;
                        this.endLine=endLine;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    return "200";

                }else if (ctx instanceof CPP14Parser.UnqualifiedIdContext||
                        ctx instanceof CPP14Parser.LiteralContext||
                        ctx instanceof CPP14Parser.SimpleTypeSpecifierContext
                ){
                    return "201";
                }

            }

        }
            for (int i = 0; i < ctx.getChildCount(); i++) {
                ParseTree element = ctx.getChild(i);
                if (element instanceof RuleContext) {
                    String status =traverseAllNodeToFindCodeLine((RuleContext) element, verbose, indentation + (toBeIgnored ? 0 : 1), tokens,lineNum);
                    if ("200".equals(status))
                    {
                        return "200";
                    }
                    else if ("201".equals(status))
                    {
                        if (returnNodeTypeForPart(ctx))
                        {
                            int preStartLine = getStartLine(tokens, ctx);
                            int preEndLine = getEndLine(tokens, ctx);
                            try {

                                this.code= getBody(preStartLine, preEndLine);
                                this.startLine=preStartLine;
                                this.endLine=preEndLine;
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return "200";
                        }
                        return "201";
                    }
                }
            }


        return "404";
    }
    public   boolean returnNodeTypeForWhole(RuleContext ctx) {
        if (
                ctx instanceof CPP14Parser.SelectionStatementContext||
                ctx instanceof CPP14Parser.IterationStatementContext||
                ctx instanceof CPP14Parser.DeclarationContext||
                ctx instanceof CPP14Parser.ExpressionStatementContext||
                ctx instanceof CPP14Parser.NoPointerDeclaratorContext ||

                ctx instanceof CPP14Parser.BracedInitListContext||
                ctx instanceof CPP14Parser.MemberdeclarationContext||
                        ctx instanceof  CPP14Parser.EnumeratorListContext
        )
        {
            return true;
        }
        else{
            return false;
        }
    }
    public   boolean returnNodeTypeForPart(RuleContext ctx) {
        if (ctx instanceof CPP14Parser.SelectionStatementContext||
                ctx instanceof CPP14Parser.IterationStatementContext||
                ctx instanceof CPP14Parser.SimpleDeclarationContext||
                ctx instanceof CPP14Parser.DeclarationContext||
                ctx instanceof CPP14Parser.BracedInitListContext||
                ctx instanceof CPP14Parser.ExpressionStatementContext

//                ctx instanceof CPP14Parser.NoPointerDeclaratorContext
        )
        {
            return true;
        }
        else{
            return false;
        }
    }

    public   String getBlock(CommonTokenStream tokens, List<String> list, ParseTree function) {

        int startLine = getStartLine(tokens, function);
        int endLine = getEndLine(tokens, function);
        StringBuilder stringBuilder = new StringBuilder();
        List<String> collect = list.stream().skip(startLine-1).limit(endLine - startLine + 1).collect(Collectors.toList());
        for (String s : collect) {
            stringBuilder.append(s).append("\n");
        }
        return stringBuilder.toString();
    }

    public  int getStartLine(CommonTokenStream tokens, ParseTree function) {
        Interval sourceInterval = function.getSourceInterval();
        Token tokenBegin = tokens.get(sourceInterval.a);
        return tokenBegin.getLine();
    }
    public int getEndLine(CommonTokenStream tokens, ParseTree function) {
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


    public  void FunctionDefinitionList(ParseTree node, List<ParseTree> methodNode) {
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
