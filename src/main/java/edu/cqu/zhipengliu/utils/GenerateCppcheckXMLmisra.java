package edu.cqu.zhipengliu.utils;


import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.utils
 * @className: GenerateCppcheckXMLmisra
 * @author: Zhipengliu
 * @description: 类似的，生成misra的警告xml，但这里可以加一个filter的功能过滤掉cppcheck默认检查出的问题（考虑到需要过滤的部分很少就还没实现：xml匹配id节点misra过滤或者suppress参数过滤）
 * @date: 2023/11/6 14:26
 * @version: 1.2
 */
public class GenerateCppcheckXMLmisra {
    public static void report(String scanFilesPath, String reportXmlPath, String logFilePath) {
//        Set cppcheck command
        ProcessBuilder processBuilder = null;
        int cpuCores = Runtime.getRuntime().availableProcessors();
        List<String> os_command = List.of(new String[]{"cppcheck", "-j", String.valueOf(cpuCores),"--rule=misra_c","--addon=/usr/share/cppcheck/addons/misra.json", "--xml", scanFilesPath});

        ProcessUtils util = new ProcessUtils();
        util.run_process(os_command,reportXmlPath,logFilePath);
    }

}
