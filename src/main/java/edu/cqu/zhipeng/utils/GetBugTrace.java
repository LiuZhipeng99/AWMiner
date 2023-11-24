package edu.cqu.zhipeng.utils;


import edu.cqu.zhipeng.parser.ExtractCodeBlockForC;
import edu.cqu.zhipeng.parser.ExtractCodeBlockForCpp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.utils
 * @className: GetBugTrace
 * @author: Zhipengliu
 * @description: 这部分功能虽然可以写在这里，但可以通过数据集的信息进行采集添加字段，可以单独实现个python脚本或Java程序
 * 另一个方案：(这会限制语言 限制程序入口 编写方法  越是集成度高的插件系统要求越多 这和算法的时间 空间的平衡. 目前值考虑收集警告和相关commit的信息再直接对数据集做处理)
 * 基于ClassLoader的插件系统【暂未实现 仅实现了基于文件规范的插件系统】
 *  *使用URLClassLoader动态加载Jar， 加载的jar是实现了code表示提取，给其扫描器信息（filepath+line)返回代码信息字符串（适用于NLP)
 * @date: 2023/11/6 17:26
 * @version: 1.2
 */
public class GetBugTrace {
    public static void getbugtrace(String jarFile, String warning_json_, String logFilePath) {

    }
    public static String getwarningtrace(String fileName, int lineNUM) throws IOException {
        String codeContex = null;
        if (isCFile(fileName)){
            ExtractCodeBlockForC extractCodeBlock = new ExtractCodeBlockForC(fileName);
            String extractCodeBlockForC = extractCodeBlock.getCodeBlockByAst(lineNUM); //尽可能多的从单行获取代码上下文
            if ("200".equals(extractCodeBlockForC))
            {
                String code = extractCodeBlock.getCode();
                Integer startLine = extractCodeBlock.getStartLine();
                Integer endLine = extractCodeBlock.getEndLine();
                String code2 =getAcode(fileName,endLine+1);
                String code3 =getAcode(fileName,startLine-1);
                codeContex = code3 +"\n"+ code + "\n" + code2;
            }else {
                try {
                    String code =getAcode(fileName,lineNUM);
                    String code2 =getAcode(fileName,lineNUM+1);
                    String code3 =getAcode(fileName,lineNUM-1);
                    codeContex = code3 +"\n"+ code + "\n" + code2;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }else if (isCppFile(fileName))
        {
            ExtractCodeBlockForCpp extractCodeBlock = new ExtractCodeBlockForCpp(fileName);
            String existCodeBlockByAst = extractCodeBlock.getCodeBlockByAst(lineNUM);
            if ("200".equals(existCodeBlockByAst))
            {

                String code = extractCodeBlock.getCode();
                Integer startLine = extractCodeBlock.getStartLine();
                Integer endLine = extractCodeBlock.getEndLine();
                String code2 =getAcode(fileName,endLine+1);
                String code3 =getAcode(fileName,startLine-1);
                codeContex = code3 +"\n"+ code + "\n" + code2;
            }else {

                try {
                    String code =getAcode(fileName,lineNUM);
                    String code2 =getAcode(fileName,lineNUM+1);
                    String code3 =getAcode(fileName,lineNUM-1);
                    codeContex = code3 +"\n"+ code + "\n" + code2;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return codeContex;
    }
    public static String getAcode(String path, Integer line) throws IOException {
        return readCppFile(path).get(line-1);
    }

    public static List<String> readCppFile(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }
    public static boolean isCppFile(String filename) {
        return filename.endsWith(".cpp") || filename.endsWith(".cc") || filename.endsWith(".cxx")
                || filename.endsWith(".h") || filename.endsWith(".hpp") || filename.endsWith(".inl")
                || filename.endsWith("ipp");
    }

    public static boolean isCFile(String filename) {
        return filename.endsWith(".c");
    }
}