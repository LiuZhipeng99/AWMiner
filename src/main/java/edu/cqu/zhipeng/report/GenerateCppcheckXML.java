package edu.cqu.zhipeng.report;


import edu.cqu.zhipeng.utils.ProcessUtils;

import java.util.List;

/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.report
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
 *
 * 后续遇到的问题：扫描时间过久有可能是上M的c文件如sqlite.c、miniaudio这种库文件引起的，可通过find找出文件也可考虑找到这种库的列表
 * find . -type f -name "*.c" -exec du -Sh {} + | sort -rh | head -n 20 找出这些文件用i参数跳过
 * @date: 2023/11/6 14:26
 * @version: 1.2
 */
public class GenerateCppcheckXML {
    public static void report(String scanFilesPath, String reportXmlPath, String logFilePath) { //可以引入commit信息生成每个commit的report文件方便以后调查
//        Set cppcheck command
        int cpuCores = Runtime.getRuntime().availableProcessors();
        List<String> os_command = List.of(new String[]{"cppcheck", "-j", String.valueOf(cpuCores),
                "-i","lib","-i","assets","-i","images","-i","data_file.c","-i","ql_fw.c","-i","Zydis.c", "-i","packet-rrc.c","-i","u8g2_fonts.c",
                "-i","miniaudio.c","-i","u8g_font_data.c","-i","udivmodti4_test.c","-i","transliteration_data.c","-i","dataframe.addons.tests.c",
                "-i","sqlcipher.c","-i","sqlite3-binding.c","-i","sqlite3.c","-i","sqlitestudio.c","-i","wxsqlite3.c",// 通配符？
                "--enable=all", "--xml", scanFilesPath});

        ProcessUtils.run_process(os_command,reportXmlPath,logFilePath);
    }

}
