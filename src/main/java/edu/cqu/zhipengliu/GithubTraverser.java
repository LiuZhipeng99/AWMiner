package edu.cqu.zhipengliu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import edu.cqu.zhipengliu.entity.GithubDetail;
import edu.cqu.zhipengliu.entity.StaticWarning;
import edu.cqu.zhipengliu.entity.WarningCppcheck;
import edu.cqu.zhipengliu.parser.ParserCppcheckWarning;
import edu.cqu.zhipengliu.utils.GenerateCppcheckXML;
import org.dom4j.DocumentException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.refactoringminer.util.GitServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.utils
 * @className: GithubTraverser
 * @author: Zhipengliu
 * @description: c_repos_sorted.txt中描述的GitHub项目实例化为GithubDetail，遍历其commit，生成报告
 * @date: 2023/11/4 15:15
 * @version: 1.1
 */


public class GithubTraverser {
    GitServiceImpl gitService = new GitServiceImpl(); //这里用多态特性实例化GitService，也没什么意义不如直接实例化，主要是也没其他gitservice实现
    public final static String tempDir = "tmp_github/"; //克隆下来的目录设置（目前写死）
    Logger logger = LoggerFactory.getLogger(GithubTraverser.class);
    public ArrayList<GithubDetail> getGithubSet(String filePath) throws IOException, CsvValidationException {
        //TODO 添加GitHub镜像功能。 函数克隆很多github项目》返回repolist，考虑到网络和io可以优化。克隆时间是否应该区分，为方便更新代码（简单实现就是把目录加上date比如带月或者日）
        //TODO 这个函数紧密依赖txt文件行存储 读入用字符串split，用格式化如json会更好。目前用长度判断行是否有效（以后可以匹配模式）
        // 先读文件再克隆优化为边读边克隆 同时也可以实例化GitHubDetail
        ArrayList<GithubDetail> repoList = new ArrayList<>();
        Path path = Paths.get(filePath);  // io.FileReader
        CSVReader reader = new CSVReader(new FileReader(path.toString()));
        String[] line;
        while ((line = reader.readNext()) != null) {
            if (line.length >= 4) {
                String id = line[0];
                try {
                    GithubDetail hub = new GithubDetail();
                    hub.setGithubName(line[3]);
                    hub.setGithubLink(line[2]);
                    hub.setStarCount(line[1]);
                    hub.setBranch(line.length >= 5 ? line[4] : "master"); //The default branch of git version 2.28 becomes main, some projects are
                    hub.setLocalTmpPath(tempDir + line[3]);
                    hub.setRepo(gitService.cloneIfNotExists(
                            hub.getLocalTmpPath(),hub.getGithubLink()));
                    repoList.add(hub);
                } catch (Exception e) {
//                        throw new RuntimeException(e); //打印出错误栈能知道详细信息 国内环境大概率需要魔法。
                    logger.warn(tempDir + line[3]+" can't loaded");
                    logger.error(line[2] + " Try clone failed, there is a network problem. Please run clone_git.py or manual git clone");
                }
            } else {
                logger.error(filePath + " have invalid line: " + Arrays.toString(line));
            }
        }
        return repoList;
    }
    public void commitTraverser(ArrayList<GithubDetail> repoList, boolean flag) {
        int numThreads = Runtime.getRuntime().availableProcessors()/2; // 获取可用的处理器数量
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        for (GithubDetail repo : repoList) {
            executor.submit(() -> {
                try {
                    traverserThread(repo,flag);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
    public void traverserThread(GithubDetail repo, boolean flag) throws Exception {
        ArrayList<StaticWarning> fixed_all = new ArrayList<>();
        ArrayList<StaticWarning> introduced_all = new ArrayList<>();
        ArrayList<StaticWarning> cur_wr = null;
        ArrayList<StaticWarning> next_wr = null; // next_commit的parent为cur
        String next_commit_id = null;
        ParserCppcheckWarning parser1 = new ParserCppcheckWarning();
        // you can use other parser and generate report.
        RevWalk walk = gitService.createAllRevsWalk(repo.getRepo(), repo.getBranch());
        walk.setRevFilter(RevFilter.ALL); //后续发现遍历没有包含merge，查看这创建walk的源码他自己实现了过滤只能有一个父也就是没merge
        if (walk.next() == null) {
            walk = gitService.createAllRevsWalk(repo.getRepo(), "main");
            walk.setRevFilter(RevFilter.ALL);
            repo.setBranch("main");
        }
        try {
            int commit_count = gitService.countCommits(repo.getRepo(), repo.getBranch());
            logger.warn("\n\nScan repo: " + repo.getGithubLink() + " Commits count: " + commit_count);
            int count = 0;
            for (RevCommit currentCommit : walk) {  //从最新commit到最早的
                if(count++%100==0) logger.warn("current commit:"+count);
                gitService.checkout(repo.getRepo(), currentCommit.getId().getName());
                long startTime = System.currentTimeMillis();
                //GenerateCppcheckXML.report(tempDir,tempDir+"report"+currentCommit+".xml");不用保留每次扫描文件，结果存在fixed all
                String xmlOutputPath = tempDir + repo.getGithubName() + "/cppcheck_report.xml";
                String logPath = tempDir + repo.getGithubName() + "/cppcheck_log";
                GenerateCppcheckXML.report(repo.getLocalTmpPath(), xmlOutputPath, logPath,currentCommit.getId().getName());
                cur_wr = parser1.parseXml(xmlOutputPath, repo.getGithubLink(), currentCommit.getId().getName(), next_commit_id);
                //                System.out.println(next_commit_id);
                next_commit_id = currentCommit.getId().getName();
                ArrayList<StaticWarning> fixed = getDiff(cur_wr, next_wr); // cur - next
                ArrayList<StaticWarning> introduced = getDiff(next_wr, cur_wr); // next - cur 暂时粗略划分为false类型/不像定义修复那样准确
                fixed_all.addAll(fixed);
                introduced_all.addAll(introduced);
                next_wr = cur_wr;
                long elapsedTime =  System.currentTimeMillis() - startTime; // 计算已经经过的时间

                if (flag==Boolean.FALSE && elapsedTime * (commit_count-count) > 60 * 60 * 1000) { // 预估一下最大时间 大于30min的 并记录在日志里
                    logger.warn(repo.getGithubLink()+" run will over 60 Min.  Please remove large c file. Skipping");
                    return; // 跳出循环 并且不调用save | 如果使用break还会调用下面的save
                }
            }
        }catch (DocumentException e){
            logger.error("Check "+tempDir + repo.getGithubName() + "/cppcheck_log"+"，无法解析报告（可能没有c文件）"); // 不会中断
        }catch (NullPointerException e){
            e.printStackTrace();
            logger.error("Check "+tempDir + repo.getGithubName()+"/, 无法check out（可能没有.git目录或分支不是master） ");
        }
        introduced_all.removeAll(fixed_all); // 这里intro - fixed 操作：考虑到某个bug在未来被fix了fixed会记录但introduced_all仍然保留着。
        if (!fixed_all.isEmpty() || !introduced_all.isEmpty()) {
            save(repo.getGithubName(), introduced_all, fixed_all);
        }
    }


    public void save(String repoName, ArrayList<StaticWarning> introduced_all, ArrayList<StaticWarning> fixed_all) throws IOException {
        // 将ArrayList转换为JSON
//        if(count%100 != 0) return; // 每100个count 保存一次 //未实现每50个commit保存一次
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonStr_fixed = null;
        String jsonStr_introduced = null;
        //这里可以添加判断fixed中tool_name来确定TypeToken
        Type CppcheckType = new TypeToken<ArrayList<WarningCppcheck>>() {
        }.getType();
        jsonStr_fixed = gson.toJson(fixed_all, CppcheckType);
        jsonStr_introduced = gson.toJson(introduced_all, CppcheckType);
        logger.warn(repoName + " Cppcheck generate fixed: " + fixed_all.size());
        logger.warn(repoName + " Cppcheck generate introduced: " + introduced_all.size());
        // 将JSON保存到文件
//        String jsonPath_fixed = "GeneratedDataset/ActionableWarning/" + repoName+"_commit_"+(count-100)+"to"+count+".json";
        String jsonPath_fixed = "GeneratedDataset/ActionableWarning/" + repoName+".json";
        String jsonPath_introduced = "GeneratedDataset/NonActionableWarning/" + repoName+".json";
        toJsonFile(jsonPath_fixed, jsonStr_fixed);
        toJsonFile(jsonPath_introduced, jsonStr_introduced);
        introduced_all.clear();
        fixed_all.clear();
    }


    public void toJsonFile(String jsonPath, String jsonStr) throws IOException {
        File fjson = new File(jsonPath);
        File parentDir = fjson.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        FileWriter writer = new FileWriter(fjson);
        writer.write(jsonStr);
        writer.close();
    }

    //    public void diffReport(ArrayList<StaticWarning> pre, ArrayList<StaticWarning> cur){}
    public ArrayList<StaticWarning> getDiff(ArrayList<StaticWarning> reportA, ArrayList<StaticWarning> reportB){
        // 查找消失/新增的元素 （A-B）
        if(reportB == null || reportA==null){ //如果B为空 代表发生在最新的commit，B不应该视为最新的commit下一个版本所有warning移除
            return new ArrayList<>();
        }
        ArrayList<StaticWarning> diffEle = new ArrayList<>();
        for (StaticWarning warning : reportA) {
            if (!reportB.contains(warning)) {
                diffEle.add(warning);
            }
        }
        return diffEle;
    }



//    public void testgitservice(ArrayList<GithubDetail> repoList) throws Exception {
//        FileOutputStream logos = new FileOutputStream("D:/log-brpc-wr" + ".txt", true);
//        for (GithubDetail repo : repoList) {
//            RevWalk walk = gitService.createAllRevsWalk(repo.getRepo(), repo.getBranch());
//            //        Iterable<RevCommit> walk = gitService.createRevsWalkBetweenCommits(repo,"88db699b4d5935d8dcce4daf90b1aa2b28b2a48b","9ad9f45db59bd69a943a7c759859031da2051f8e");
//            //        Iterator<RevCommit> walk = gitService.createRevsWalkBetweenTags(repo,"")
//            int count = 0;
//            ArrayList<StaticWarning> wr_old = new ArrayList<>();
//            walk.setRevFilter(RevFilter.ALL); //后续发现遍历没有包含merge，查看这创建walk的源码他自己实现了过滤只能有一个父也就是没merge
//            for (RevCommit currentCommit : walk) {
//                System.out.println(currentCommit.getId().getName());
//                //            gitService.fileTreeDiff(repo, currentCommit, filePathsBefore, filePathsCurrent, renamedFilesHint);
//                gitService.checkout(repo.getRepo(), currentCommit.getId().getName());
//                //不用写太多文件，根据commitid再扫一次就有了GenerateCppcheckXML.report(projectpath,projectpath+"report"+currentCommit.getId().getName()+".xml",projectpath+"log"+currentCommit.getId().getName());
//                GenerateCppcheckXML.report(repo.getGithubName(), repo.getLocalTmpPath() + "report.xml", repo.getLocalTmpPath() + "log");
//                ArrayList<StaticWarning> wr = new ParserCppcheckWarning().parseXml(repo.getLocalTmpPath() + "report.xml",currentCommit.getId().getName(), currentCommit.getId().getName());
//                System.out.println("warning_nums:" + wr.size());
//                //            warningCppchekRepository.saveAll(wr);
//                //true表示在文件末尾追加
//
////                String log = GenerateCppcheckXML.compare(wr_old, wr);//找出新版本v2减少了哪些warning 对调参数就是找增加了哪些
//
////                logos.write(log.getBytes());
//                logos.write(System.getProperty("line.separator").getBytes());
//                logos.write(System.getProperty("line.separator").getBytes());
//                wr_old = wr;
//                count++;
//                //            if(count>1){break;}
//
//            }
//        }
//        logos.close();
//    }
//        });
}
