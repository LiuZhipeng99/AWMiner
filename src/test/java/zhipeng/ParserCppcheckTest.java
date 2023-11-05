package zhipeng;

import edu.cqu.zhipengliu.entity.StaticWarning;
import edu.cqu.zhipengliu.parser.ParserCppcheckWarning;
import org.dom4j.DocumentException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ParserCppcheckTest {

    @Test // 测试了xml到数据库、java调用cppcheck产生xml
    public void testCppcheckwarning() throws DocumentException {
        ArrayList<StaticWarning> wr = new ParserCppcheckWarning().parseXml("D:\\0Workspace\\IDEA-CODE\\SAWMiner\\src\\test\\java\\zhipeng\\cppcheckreport-no-addon.xml", "testgit","testid");
        System.out.println(wr.size());

//        cppcheck --addon=misra --xml-version=2 --enable=all dir
//        error ：发现bug时提示级别。
//        warning ：建议预防程序中产生bug的提示。
//        style ：关系到代码整洁的编程风格提示。
//        performance ：可以使代码运行更有效的建议提示。
//        portability ：可移植性提示。64位兼容、可运行在不同编译器等等的移植性。
//        information ：关于检查问题过程中的一些信息提示。
//        System.out.println(wr.size());
            // 构建cppcheck命令行
        try {
            // 指定要扫描的目录
            String directory = "tmp/brpc";
//            String directory = "./src";

            // 创建ProcessBuilder对象，指定cppcheck命令及参数
            ProcessBuilder processBuilder = new ProcessBuilder("cppcheck", "--xml", directory);

            // 设置重定向输出到文件
            processBuilder.redirectError(ProcessBuilder.Redirect.to(new File("report-test.xml")));
            processBuilder.redirectOutput(ProcessBuilder.Redirect.to(new File("output-test.txt")));
            // 启动进程并等待其完成
            Process process = processBuilder.start();
//           第一个BUG：用Runtime.exec执行命令时会阻塞不知道为啥，Linux不会阻塞但产生不了文件。在win下，用命令行产生文件有cppcheck报错：所在位置 行:1 字符: 1，但用proBuilder就解决了
            int exitCode = process.waitFor(); //Process.waitFor() 阻塞卡住不返回 是因为缓冲区满了好像如果输出小会返回，这里卡住直接终止后才会运行
//            所以无法观测这个进程是否退出， 这里只能想如何修改缓冲区大小而不巧GPT说这段代码没有接口修改，只能使用其他输出流方式才能修改从而知道进程退出状态。（目前打算先不管）
            //为确保子进程进行不能不管，但本想通过process的输出流/错误流进行处理，结果惊人发现只重定向了错误流输出流一直在输出，原来是这个引起缓冲区问题了。

//            BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//            String outputLine;
//            while ((outputLine = outputReader.readLine()) != null) {
//                // 处理输出
//                System.out.println(outputLine);
//            }
//            String errorLine;
//            while ((errorLine = errorReader.readLine()) != null) {
//                // 处理错误
//                System.out.println(errorLine);
//            }
//            int exitCode = process.waitFor();
//            outputReader.close();
//            errorReader.close();
            process.destroy();
            // 检查进程的退出代码
            if (exitCode == 0) {
                System.out.println("扫描完成！");
            } else {
                System.out.println("扫描失败！");
            }
            // 第二个BUG：扫描某个项目退出代码不是0，一番波折发现由于项目中有软连接指向的文件不存在。过程中发现停止是因为遇到了这种不存在的链接同样是.c文件就终止了，而Linux上更快遇到所以终止快
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
