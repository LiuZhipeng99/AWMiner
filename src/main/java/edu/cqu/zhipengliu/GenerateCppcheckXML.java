package edu.cqu.zhipengliu;


import edu.cqu.zhipengliu.entity.WarningCppcheck;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GenerateCppcheckXML {
    public static void report(String scanFile, String reportFileName,String logFile){
        try { //用Runtime.exec执行命令时会阻塞不知道为啥，Linux不会阻塞但产生不了文件。在win下，用命令行产生文件有cppcheck报错：所在位置 行:1 字符: 1，但用proBuilder就解决了
            // 指定要扫描的目录

            // 创建ProcessBuilder对象，指定cppcheck命令及参数
//            ProcessBuilder processBuilder = new ProcessBuilder("cppcheck","--xml", scanFile);
            ProcessBuilder processBuilder = new ProcessBuilder("cppcheck","--enable=warning","--xml", scanFile);

            // 设置重定向输出到文件
            processBuilder.redirectError(ProcessBuilder.Redirect.to(new File(reportFileName)));
            processBuilder.redirectOutput(ProcessBuilder.Redirect.to(new File(logFile)));

            // 启动进程并等待其完成
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            process.destroy();
            // 检查进程的退出代码
            if (exitCode == 0) {
                System.out.println("扫描完成！");
            } else {
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
