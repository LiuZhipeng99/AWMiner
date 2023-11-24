//package path.to;
//
//import org.antlr.v4.runtime.CharStream;
//import org.antlr.v4.runtime.CharStreams;
//import org.antlr.v4.runtime.CommonTokenStream;
//import org.antlr.v4.runtime.ParserRuleContext;
//import org.antlr.v4.runtime.tree.ParseTree;
//import org.antlr.v4.runtime.tree.ParseTreeWalker;
//
//import java.io.IOException;
//
///**
// * @author Xzzz
// * @data 2023/07/11
// */
//public class TypeTest {
//    String path = "D:\\project\\chapGPT\\src\\test\\files\\test.cpp";
//    CharStream charStream = CharStreams.fromFileName(path);
//    CLexer lexer = new CLexer(charStream);
//    CommonTokenStream tokens = new CommonTokenStream(lexer);
//    CParser parser = new CParser(tokens);
//    ParserRuleContext ctx = parser.compilationUnit();
//
//
//    // 使用ANTLR 4语法树遍历器遍历语法树，找到包含给定行号的节点
//    int lineNumber = 42; // 假设要查找的行号是42
//    ParseTreeWalker walker = new ParseTreeWalker();
//    MyListener listener = new MyListener(lineNumber);
//    walker.walk(listener, tree);
//    ParseTree node = listener.getNode(); // 获取包含给定行号的节点
//
//    // 根据节点类型和位置信息提取代码
//    String extractedCode = extractCode(node);
//
//    public TypeTest() throws IOException {
//    }
//
//    // MyListener类，用于在ANTLR 4语法树中查找给定行号的节点
//    class MyListener extends  {
//        private int lineNumber;
//        private ParseTree node;
//
//        public MyListener(int lineNumber) {
//            this.lineNumber = lineNumber;
//        }
//
//        public ParseTree getNode() {
//            return node;
//        }
//
//        // 进入语句节点时检查行号是否匹配
//        @Override
//        public void enterStatement(CParser.StatementContext ctx) {
//            if (ctx.getStart().getLine() <= lineNumber && ctx.getStop().getLine() >= lineNumber) {
//                node = ctx;
//            }
//        }
//
//        // 进入函数定义节点时检查行号是否匹配
//        @Override
//        public void enterFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
//            if (ctx.getStart().getLine() <= lineNumber && ctx.getStop().getLine() >= lineNumber) {
//                node = ctx.declarator();
//            }
//        }
//
//        // 进入结构体定义节点时检查行号是否匹配
//        @Override
//        public void enterStructOrUnionSpecifier(CParser.StructOrUnionSpecifierContext ctx) {
//            if (ctx.getStart().getLine() <= lineNumber && ctx.getStop().getLine() >= lineNumber) {
//                node = ctx;
//            }
//        }
//    }
//
//    // extractCode方法，根据节点类型和位置信息提取代码
//    private String extractCode(ParseTree node) {
//        String code = node.toStringTree().replaceAll("[()]", ""); // 去除括号
//        if (node instanceof CParser.StatementContext) {
//            // 如果是语句节点，返回整个语句
//            return code;
//        } else if (node instanceof CParser.DeclaratorContext) {
//            // 如果是函数定义节点，返回函数头
//            return code.split("\\{")[0] + ";"; // 只返回函数头，不包括函数体
//        } else {
//            // 如果是结构体定义节点，返回整个结构体
//            return code;
//        }
//    }
//}
