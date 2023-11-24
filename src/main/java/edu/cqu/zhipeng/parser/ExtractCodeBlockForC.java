package edu.cqu.zhipeng.parser;

import edu.cqu.zhipeng.org.antlr.c.CLexer;
import edu.cqu.zhipeng.org.antlr.c.CParser;
import lombok.Data;
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
 * @author Xzzz 2023/07/11
 * @description: 由GetBugTrace调用
 */
@Data
public class ExtractCodeBlockForC {
    private String path;
    private String code;
    private Integer startLine;
    private Integer endLine;


    public ExtractCodeBlockForC(String path) {
        this.path = path;
    }

    public String getCodeBlockByAst(int lineNum)
    {
        CharStream charStream = null;
        try {
            charStream = CharStreams.fromFileName(path);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        CLexer lexer = new CLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CParser parser = new CParser(tokens);
        ParserRuleContext tree = parser.compilationUnit();
        return traverseAllNodeToFindCodeLine(tree,false,0,tokens,lineNum);

    }


    //在这个版本中，我移除了对特定类型节点的检查，因为在寻找包含特定行的代码块时，应该考虑所有类型的节点。如果希望只考虑特定类型的节点，可以根据需要调整代码。
    //此外，我将判断行号是否在节点的开始行和结束行之间的逻辑放在了toBeIgnored条件判断之后，这样对于符合toBeIgnored条件的节点，我们仍然会检查其子节点。
    public String traverseAllNodeToFindCodeLine_gpt(RuleContext ctx, boolean verbose, int indentation, CommonTokenStream tokens, int lineNum){
        boolean toBeIgnored = !verbose && ctx.getChildCount() == 1 && ctx.getChild(0) instanceof ParserRuleContext;
        int startLine = getStartLine(tokens, ctx);
        int endLine = getEndLine(tokens, ctx);
        if (!toBeIgnored) {
            if (startLine <= lineNum && lineNum <= endLine) {
                try {
                    this.code= getBody(startLine, endLine);
                    this.startLine=startLine;
                    this.endLine=endLine;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return "200";
            }
        }

        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree element = ctx.getChild(i);
            if (element instanceof RuleContext) {
                String status = traverseAllNodeToFindCodeLine_gpt((RuleContext) element, verbose, indentation + (toBeIgnored ? 0 : 1), tokens, lineNum);
                if ("200".equals(status)) {
//                    System.out.println(status);
                    return "200";
                }
            }
        }
        return "404";
    }

    public String traverseAllNodeToFindCodeLine
            (RuleContext ctx, boolean verbose, int indentation, CommonTokenStream tokens, int lineNum){
        boolean toBeIgnored = !verbose && ctx.getChildCount() == 1 && ctx.getChild(0) instanceof ParserRuleContext;
        int startLine = getStartLine(tokens, ctx);
        int endLine = getEndLine(tokens, ctx);
        if (!toBeIgnored) {
            if (startLine==lineNum)
            {
//                System.out.println(ctx.getClass().getSimpleName());
                if(returnNodeTypeForWhole(ctx))
                {
                    try {
                        this.code= getBody(startLine, endLine);
                        this.startLine=startLine;
                        this.endLine=endLine;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return "200";
                }else if (ctx instanceof  CParser.TypedefNameContext ||
                ctx instanceof  CParser.PrimaryExpressionContext||
                ctx instanceof CParser.ParameterDeclarationContext)
                {
                    int parentStartLine = getStartLine(tokens, ctx.getParent());
                    int parentEndLine = getEndLine(tokens, ctx.getParent());
                    try {
                        this.code= getBody(parentStartLine, parentEndLine);
                        this.startLine=parentStartLine;
                        this.endLine=parentEndLine;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
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
//                        System.out.println(status);
                        return "200";
                    } else if ("201".equals(status))
                    {
//                        System.out.println(status);
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

    private boolean returnNodeTypeForPart(RuleContext ctx) {
        if (ctx instanceof CParser.SelectionStatementContext||
                ctx instanceof CParser.IterationStatementContext||
                ctx instanceof CParser.StructOrUnionContext||
                ctx instanceof CParser.DeclarationContext
       ) {
            return true;
        }
        else{
            return false;
        }
    }

    public   boolean returnNodeTypeForWhole(RuleContext ctx) {
        if (ctx instanceof CParser.SelectionStatementContext||
                ctx instanceof CParser.IterationStatementContext||
                ctx instanceof CParser.StructOrUnionContext||
                ctx instanceof CParser.DeclarationContext||
                ctx instanceof CParser.ExpressionStatementContext
                || ctx instanceof CParser.CompoundStatementContext //增加函数定义
//                || ctx instanceof CParser.StructDeclarationListContext //增加if语句
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
