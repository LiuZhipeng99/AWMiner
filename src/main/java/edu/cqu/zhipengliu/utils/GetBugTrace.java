package edu.cqu.zhipengliu.utils;


import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.utils
 * @className: GetBugTrace
 * @author: Zhipengliu
 * @description: 这部分功能虽然可以写在这里，但可以通过数据集的信息进行采集添加字段，可以单独实现个python脚本或Java程序
 * 另一个方案：
 * 基于ClassLoader的插件系统【暂未实现 仅实现了基于文件规范的插件系统】
 *  *使用URLClassLoader动态加载Jar， 加载的jar是实现了code表示提取，给其扫描器信息（filepath+line)返回代码信息字符串（适用于NLP)
 * @date: 2023/11/6 17:26
 * @version: 1.2
 */
public class GetBugTrace {
    public static void getbugtrace(String jarFile, String warning_json_, String logFilePath) {

    }
}