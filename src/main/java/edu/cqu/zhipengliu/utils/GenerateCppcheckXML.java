package edu.cqu.zhipengliu.utils;


import edu.cqu.zhipengliu.entity.WarningCppcheck;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.utils
 * @className: GenerateCppcheckXML
 * @author: Zhipengliu
 * @description: 利用cppcheck二进制程序扫描产生xml输出， 需要了解其参数用法以高效产出所需的  命令参数如 j、enable、addon （j参数是并发在多文件上的，单个文件还是一个进程在扫描）
 * 扫描的规范方面：可以使用--enable参数加上一些内置规范， 也可以用其python实现的misra规范（需要python环境和misra.json）：
 * --rule=misra_c --addon=/usr/share/cppcheck/addons/misra.json
 * @date: 2023/11/6 14:26
 * @version: 1.2
 */
public class GenerateCppcheckXML {
    public static void report(String scanFilesPath, String reportXmlPath, String logFilePath) {
//        Set cppcheck command
        ProcessBuilder processBuilder = null;
        String os = System.getProperty("os.name").toLowerCase();
        String cpu_core = "6";
        if (os.contains("linux")) {  //cpu自适应
            cpu_core = "$(grep -c ^processor /proc/cpuinfo)";
        }
        List<String> os_command = List.of(new String[]{"cppcheck", "-j", cpu_core,"--rule=misra_c","--addon=/usr/share/cppcheck/addons/misra.json", "--xml", scanFilesPath});


//        start command
        try { //用Runtime.exec执行命令时会阻塞不知道为啥，Linux不会阻塞但产生不了文件。在win下，用命令行产生文件有cppcheck报错：所在位置 行:1 字符: 1，但用proBuilder就解决了
            processBuilder = new ProcessBuilder(os_command);
            File errorfile = new File(reportXmlPath);
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
            int exitCode = process.waitFor();
            process.destroy();
            if (exitCode != 0) {
                System.out.println("Scan failed !!  check "+reportXmlPath+" for errors");
            }
        } catch (InterruptedException | IOException e) {
            System.out.println("cppcheck start failed, check args: " +processBuilder.command());
            e.printStackTrace();
        }
    }

}
