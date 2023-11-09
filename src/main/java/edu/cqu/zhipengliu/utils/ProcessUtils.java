package edu.cqu.zhipengliu.utils;

import edu.cqu.zhipengliu.SAWMiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.utils
 * @className: ProcessUtils
 * @author: Zhipengliu
 * @description: Java运行os命令方便调用二进制程序（解决几个问题才有效）
 * @date: 2023/11/7 12:48
 * @version: 1.1
 */
public class ProcessUtils {
    Logger logger = LoggerFactory.getLogger(ProcessUtils.class);
    public void run_process(List<String> os_command, String errorFilePath, String logFilePath){
        //        start command
        ProcessBuilder processBuilder = null;
        try { //用Runtime.exec执行命令时会阻塞不知道为啥，Linux不会阻塞但产生不了文件。在win下，用命令行产生文件有cppcheck报错：所在位置 行:1 字符: 1，但用proBuilder就解决了
            processBuilder = new ProcessBuilder(os_command);
            File errorfile = new File(errorFilePath);
            File logfile = new File(logFilePath);
            if (errorfile.getParentFile().exists() | errorfile.getParentFile().mkdir()) {
                processBuilder.redirectError(ProcessBuilder.Redirect.to(errorfile));
            }
            if (logfile.getParentFile().exists() | logfile.getParentFile().mkdir()) {
                processBuilder.redirectOutput(ProcessBuilder.Redirect.to(logfile));
            }
//            System.out.println(processBuilder.command());
            // Start the process and wait for it to complete.
            Process process = processBuilder.start();
//           第一个BUG：用Runtime.exec执行命令时会阻塞不知道为啥，Linux不会阻塞但产生不了文件。在win下，用命令行产生文件有cppcheck报错：所在位置 行:1 字符: 1，但用proBuilder就解决了
            int exitCode = process.waitFor(); //Process.waitFor() 阻塞卡住不返回 是因为缓冲区满了好像如果输出小会返回，这里卡住直接终止后才会运行
//            所以无法观测这个进程是否退出， 这里只能想如何修改缓冲区大小而不巧GPT说这段代码没有接口修改，只能使用其他输出流方式才能修改从而知道进程退出状态。（目前打算先不管）
            //为确保子进程进行不能不管，但本想通过process的输出流/错误流进行处理，结果惊人发现只重定向了错误流输出流一直在输出，原来是这个引起缓冲区问题了。
            process.destroy();
            // 第二个BUG：扫描某个项目退出代码不是0，一番波折发现由于项目中有软连接指向的文件不存在。过程中发现停止是因为遇到了这种不存在的链接同样是.c文件就终止了，而Linux上更快遇到所以终止快
            if (exitCode != 0) {
                logger.error("command stop !!  check logfile: "+logFilePath);
            }
        } catch (InterruptedException | IOException e) {
            logger.error("command start failed, check args: " +processBuilder.command());
            e.printStackTrace();
        }
    }
}
