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
 * 扫描的规范方面：可以使用--enable参数加上一些内置规范：
 * //        error ：发现bug时提示级别。
 * //        warning ：建议预防程序中产生bug的提示。
 * //        style ：关系到代码整洁的编程风格提示。
 * //        performance ：可以使代码运行更有效的建议提示。
 * //        portability ：可移植性提示。64位兼容、可运行在不同编译器等等的移植性。
 * //        information ：关于检查问题过程中的一些信息提示。
 * 也可以用其python实现的misra规范（需要python环境和misra.json）：
 * --rule=misra_c --addon=/usr/share/cppcheck/addons/misra.json
 * 参考说明：官网、<a href="https://www.cnblogs.com/young525/p/5873771.html">...</a>
 * @date: 2023/11/6 14:26
 * @version: 1.2
 */
public class GenerateCppcheckXML {
    public static void report(String scanFilesPath, String reportXmlPath, String logFilePath) {
//        Set cppcheck command
        int cpuCores = Runtime.getRuntime().availableProcessors();
        List<String> os_command = List.of(new String[]{"cppcheck", "-j", String.valueOf(cpuCores),"--enable=warning", "--xml", scanFilesPath});

        ProcessUtils util = new ProcessUtils();
        util.run_process(os_command,reportXmlPath,logFilePath);
    }

}
