package edu.cqu.zhipengliu.utils;


import edu.cqu.zhipengliu.entity.WarningCppcheck;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.utils
 * @className: GenerateCppcheckXML
 * @author: Zhipengliu
 * @description: 利用cppcheck二进制程序扫描产生xml输出， 需要了解其参数用法以高效产出所需的  命令参数如 xml、j、enable、addon
 * --rule=misra_c --addon=/usr/share/cppcheck/addons/misra.json -j 32 --xml --xml-version=2
 * @date: 2023/11/4 14:26
 * @version: 1.1
 */
public class GenerateCppcheckXML {
    public static void report(String scanFilesPath, String reportXmlPath, String logFilePath){
        try { //用Runtime.exec执行命令时会阻塞不知道为啥，Linux不会阻塞但产生不了文件。在win下，用命令行产生文件有cppcheck报错：所在位置 行:1 字符: 1，但用proBuilder就解决了
            // 指定要扫描的目录

            // 创建ProcessBuilder对象，指定cppcheck命令及参数
//            ProcessBuilder processBuilder = new ProcessBuilder("cppcheck","--xml", scanFile);
            ProcessBuilder processBuilder = new ProcessBuilder("cppcheck","--enable=warning","--xml", scanFilesPath);

            // 设置重定向输出到文件
            File errorfile = new File(reportXmlPath);
            File logfile = new File(logFilePath);
            if(errorfile.getParentFile().exists() | errorfile.getParentFile().mkdir()){
                processBuilder.redirectError(ProcessBuilder.Redirect.to(errorfile));
            }
            if(logfile.getParentFile().exists() | logfile.getParentFile().mkdir()){
                processBuilder.redirectOutput(ProcessBuilder.Redirect.to(logfile));
            }

            // 启动进程并等待其完成
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            process.destroy();
            // 检查进程的退出代码
            if (exitCode != 0) {
                System.out.println("扫描失败！");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String compare(ArrayList<WarningCppcheck> v1, ArrayList<WarningCppcheck> v2){
        ArrayList<WarningCppcheck> wr_reduce = new ArrayList<>();
        for(WarningCppcheck obj1 : v1){ // 找出新版本v2减少了哪些warning
            if(!v2.contains(obj1)){
                wr_reduce.add(obj1);
            }
        }
//        System.out.println("reduce："+wr_reduce.size()+wr_reduce);
        return String.valueOf(wr_reduce.size())+wr_reduce;
    }
}
