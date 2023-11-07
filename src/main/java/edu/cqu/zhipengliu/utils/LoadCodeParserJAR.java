package edu.cqu.zhipengliu.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;


/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.utils
 * @className: LoadCodeParser
 * @author: Zhipengliu
 * @description: 基于ClassLoader的插件系统【暂未实现 仅实现了基于文件规范的插件系统】
 * 使用URLClassLoader动态加载Jar， 加载的jar是实现了code表示提取，给其扫描器信息（filepath+line)返回代码信息字符串（适用于NLP)
 * @date: 2023/11/6 16:54
 * @version: 1.1
 */
//import org.example.CodeSmellDatasets.JSONUtil;
public class LoadCodeParserJAR {
    public void testUrlClassLoader() throws ClassNotFoundException {
        String path = "D:\\0Workspace\\IDEA-CODE\\SAWMiner\\src\\main\\resources";
        loadJar(path);
        //测试是否能够加载jar里面的类，测试成功加载了jar里面相关的类
        Class.forName("org.example.CodeSmellDatasets.JSONUtil");


    }

    public static void loadJar(String path){
        // 系统类库路径
        File libPath = new File(path);

        // 获取所有的.jar和.zip文件
        File[] jarFiles = libPath.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar") || name.endsWith(".zip");
            }
        });

        if (jarFiles != null) {
            // 从URLClassLoader类中获取类所在文件夹的方法
            // 对于jar文件，可以理解为一个存放class文件的文件夹
            Method method = null;

        }
    }

}