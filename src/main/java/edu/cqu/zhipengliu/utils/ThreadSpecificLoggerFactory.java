package edu.cqu.zhipengliu.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;

import java.io.File;

/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.utils
 * @className: ThreadSpecificLoggerFactory
 * @author: Zhipengliu
 * @description: TODO
 * @date: 2023/11/9 21:32
 * @version: 1.1
 */
public class ThreadSpecificLoggerFactory {
    public static Logger getLogger() {
        String threadName = Thread.currentThread().getName();

        String logFile = "./log/" + threadName + ".log";
        // 设置日志文件路径
        System.setProperty(SimpleLogger.LOG_FILE_KEY, logFile);
        new File("./log/").mkdir();
        // 创建Logger实例
        Logger logger = LoggerFactory.getLogger("SAMinerThread");

        // 清除日志文件配置，以免影响其他Logger实例
        System.clearProperty(SimpleLogger.LOG_FILE_KEY);

        return logger;
    }
}
