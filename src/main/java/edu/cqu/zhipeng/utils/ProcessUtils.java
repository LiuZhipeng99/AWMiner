package edu.cqu.zhipeng.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.utils
 * @className: ProcessUtils
 * @author: Zhipengliu
 * @description: Java运行os命令方便调用二进制程序（解决几个问题才有效）, run process是专门写了timeout和错误/日志输出文件，适合调用SPA
 * @date: 2023/11/7 12:48
 * @version: 1.1
 */
public class ProcessUtils {
    static Logger logger = LoggerFactory.getLogger(ProcessUtils.class);
    public static void run_process(List<String> os_command, String errorFilePath, String logFilePath, int minutes){
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

            Process process = processBuilder.start();
            long processHandle = process.pid();
            if (processHandle != -1L && System.getProperty("os.name").toLowerCase().contains("linux")) {
                String command = "renice -n 10 -p " + processHandle;
                Runtime.getRuntime().exec(command); // 使得运行并发的cppcheck优先级为10 不影响服务器其他任务
            }
            // Object.wait 让当前线程等待某个条件而暂时停止：线程间通信
            if (process.waitFor(minutes, TimeUnit.MINUTES)) {
                // 未超时结束
                if(process.exitValue()!=0) logger.error(os_command.get(0) + " exit abnormal, 可能项目当前commit没有c文件");
            }else {
                // 超时处理逻辑 （没在时间内完成）
                logger.error(os_command.get(0)  + " run over "+ minutes +"min, stopping pid "+ processHandle); //本来这个run_process应该有exit返回的用于进程间通信，刚好如果这cppcheck不正常退出后续的parser会catch进而退出循环
                process.destroy(); // 可以选择终止子进程
            }
//           下面使用 thread.join 当前线程等待thread这个线程程超时：线程间同步 。 同时Process.watfor是进程版
//            Thread waitForThread = new Thread(() -> {
//                try {
//                    process.waitFor();
//                } catch (InterruptedException e) {
//                    logger.error("command Interrupt, check args: " +os_command);
//                    throw new RuntimeException(e);
//                }
//            });
//            waitForThread.start();
//            waitForThread.join(30 * 60 * 1000);
//            if (waitForThread.isAlive()) {
//                // Timeout occurred - kill the process
//                logger.error(os_command.get(0) + " run over 30 min, stopping");
//                process.destroy();
//            } else {
//                int exitCode = process.exitValue();
//            }
//           第一个BUG：用Runtime.exec执行命令时会阻塞不知道为啥，Linux不会阻塞但产生不了文件。在win下，用命令行产生文件有cppcheck报错：所在位置 行:1 字符: 1，但用proBuilder就解决了
//            int exitCode = process.waitFor(); //Process.waitFor() 阻塞卡住不返回 是因为缓冲区满了好像如果输出小会返回，这里卡住直接终止后才会运行
//            process.destroy();
//            所以无法观测这个进程是否退出， 这里只能想如何修改缓冲区大小而不巧GPT说这段代码没有接口修改，只能使用其他输出流方式才能修改从而知道进程退出状态。（目前打算先不管）
            //为确保子进程进行不能不管，但本想通过process的输出流/错误流进行处理，结果惊人发现只重定向了错误流输出流一直在输出，原来是这个引起缓冲区问题了。
            // 第二个BUG：扫描某个项目退出代码不是0，一番波折发现由于项目中有软连接指向的文件不存在。过程中发现停止是因为遇到了这种不存在的链接同样是.c文件就终止了，而Linux上更快遇到所以终止快

        } catch (InterruptedException | IOException e) {
            logger.error("command start failed, check args: " +os_command);
            e.printStackTrace();
        }
    }


    public static String execute(File workingDir, String ... commandAndArgs) {
        try {
            Process p = new ProcessBuilder(commandAndArgs)
                    .directory(workingDir)
                    .redirectErrorStream(true)
                    .start();
            try {
                StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream());
                outputGobbler.run();
                //Thread outputGobblerThread = new Thread(outputGobbler);
                //outputGobblerThread.start();
                p.waitFor();

                if (p.exitValue() == 0) {
                    return outputGobbler.getOutput();
                } else {
                    throw new RuntimeException("Error executing command " + commandAndArgs + ":\n" + outputGobbler.getOutput());
                }
            }
            finally {
                close(p.getInputStream());
                close(p.getOutputStream());
                //p.destroy();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error executing command " + commandAndArgs, e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Error executing command " + commandAndArgs, e);
        }
    }

    private static void close(Closeable closeable) throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }

    private static class StreamGobbler implements Runnable {
        private final InputStream is;
        private final StringBuffer output = new StringBuffer();

        StreamGobbler(InputStream is) {
            this.is = is;
        }

        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ((line = br.readLine()) != null) {
                    output.append(line + '\n');
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public String getOutput() {
            return this.output.toString();
        }
    }
}
