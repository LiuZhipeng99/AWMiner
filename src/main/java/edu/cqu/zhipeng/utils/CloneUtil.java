package edu.cqu.zhipeng.utils;

import java.io.*;

/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipeng.utils
 * @className: CloneUtil
 * @author: Zhipengliu
 * @description: 用于序列化进行深拷贝，且不用对需要深拷贝的类（即用了list存储的情况） 实现clone方法仅实现Serializable：<a href="https://blog.csdn.net/Mrerlou/article/details/120330796">...</a>
 * @date: 2023/11/23 21:45
 * @version: 1.1
 */
public class CloneUtil {
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T clone(T obj){
        T cloneObj = null;
        //写入字节流
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream obs = new ObjectOutputStream(out);
            obs.writeObject(obj);
            obs.close();

            //分配内存，写入原始对象，生成新对象
            ByteArrayInputStream ios = new ByteArrayInputStream(out.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(ios);
            //返回生成的新对象
            cloneObj = (T) ois.readObject();
            ois.close();
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return cloneObj;
    }
}
