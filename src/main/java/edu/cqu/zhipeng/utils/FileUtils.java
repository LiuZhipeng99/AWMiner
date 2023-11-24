package edu.cqu.zhipeng.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import edu.cqu.zhipeng.entity.GithubDetail;
import edu.cqu.zhipeng.entity.StaticWarning;
import edu.cqu.zhipeng.entity.WarningCppcheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.utils
 * @className: FileUtils
 * @author: Zhipengliu
 * @description: 分离一些IO操作的函数
 * @date: 2023/11/22 13:22
 * @version: 1.1
 */
public class FileUtils {
    public final static String tempDir = "tmp_github/"; //克隆下来的目录设置（目前写死）
    public final static String dataSetDir = "GeneratedDataset/ActionableWarning/";
    public final static String dataSetDir2 = "GeneratedDataset/NonActionableWarning/";
    static Logger logger = LoggerFactory.getLogger(FileUtils.class);


    public static ArrayList<GithubDetail> getGithubSet(String filePath) throws IOException, CsvValidationException {
        //TODO 添加GitHub镜像功能。 函数克隆很多github项目》返回repolist，考虑到网络和io可以优化。克隆时间是否应该区分，为方便更新代码（简单实现就是把目录加上date比如带月或者日）
        //目前用长度判断行是否有效（以后可以匹配模式）////先读文件再克隆优化为边读边克隆 同时也可以实例化GitHubDetail
        ArrayList<GithubDetail> repoList = new ArrayList<>();
        Path path = Paths.get(filePath);  // io.FileReader
        CSVReader reader = new CSVReader(new FileReader(path.toString()));
        String[] line;
        String[] header = reader.readNext();
        while ((line = reader.readNext()) != null) {  //直接用line有两处：恢复扫描和加入列表
            File directory = new File(dataSetDir);
            String githubName = line[0];
            if (directory.exists() && directory.isDirectory()) { // 增加继续扫描的能力（导致重扫需要删文件）
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && file.getName().equals(githubName+".json")) {
                            System.out.print(githubName + "已存在于" + dataSetDir+".  ");
                            line = reader.readNext(); // 有可能line变为null导致while报错
                        }
                    }
                }
            }
            if(line == null) break; //上面的恢复机制，如果最后一行已经运行了的会导致line为空
            try {
                GithubDetail hub = new GithubDetail();
                hub.setGithubName(line[0]);
                hub.setGithubLink(line[1]);
                hub.setLocalTmpPath(tempDir + line[0]); //考虑用user_name的形式避免fork项目一样的文件夹
                hub.setRepo(GitUtils.cloneIfNotExists(hub.getLocalTmpPath(), hub.getGithubLink()));
                hub.setBranch(line[2]); //git 2.28 默认分支变为main
                hub.setStarCount(Integer.parseInt(line[3])); //这个其实也可以通过爬虫读取，但没commitCount直接读git库方便
                repoList.add(hub);
            } catch (Exception e) {
                //throw new RuntimeException(e); //打印出错误栈能知道详细信息 国内环境大概率需要魔法。
                logger.error(" Try git clone failed, there is a network problem. Please run Script/Script_clone_github.py repoList.csv");
                e.printStackTrace();
            }
        }
        return repoList;
    }

    public static void save(String repoName, ArrayList<StaticWarning> introduced_all, ArrayList<StaticWarning> fixed_all) throws IOException {
        // 将ArrayList转换为JSON
//        if(count%100 != 0) return; //未实现每100个commit保存一次
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        //这里可以添加判断fixed中tool_name来确定TypeToken
        Type CppcheckType = new TypeToken<ArrayList<WarningCppcheck>>() {
        }.getType();
        String jsonStr_fixed = gson.toJson(fixed_all, CppcheckType);
        String jsonStr_introduced = gson.toJson(introduced_all, CppcheckType);
        // 将JSON保存到文件
//        String jsonPath_fixed = "GeneratedDataset/ActionableWarning/" + repoName+"_commit_"+(count-100)+"to"+count+".json";
        String jsonPath_fixed = dataSetDir + repoName+".json";
        String jsonPath_introduced = dataSetDir2 + repoName+".json";
        toJsonFile(jsonPath_fixed, jsonStr_fixed);
        toJsonFile(jsonPath_introduced, jsonStr_introduced);
//        introduced_all.clear(); // 不需要清除
//        fixed_all.clear();
    }


    public static void toJsonFile(String jsonPath, String jsonStr) throws IOException {
        File fjson = new File(jsonPath);
        File parentDir = fjson.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean exit = parentDir.mkdirs();
        }
        FileWriter writer = new FileWriter(fjson);
        writer.write(jsonStr);
        writer.close();
    }
}
