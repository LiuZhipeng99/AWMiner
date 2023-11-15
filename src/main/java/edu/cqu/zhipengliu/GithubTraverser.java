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
import edu.cqu.zhipengliu.utils.ThreadSpecificLoggerFactory;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

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
    public final static String dataSetDir = "GeneratedDataset/ActionableWarning/";
    public final static String dataSetDir2 = "GeneratedDataset/NonActionableWarning/";

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
            if (line.length >= 3) {  //直接用line有两处：恢复扫描和加入列表
                File directory = new File(dataSetDir);
                String githubName = line[0];
                if (directory.exists() && directory.isDirectory()) { // 增加继续扫描的能力（导致重扫需要删文件）
                    File[] files = directory.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.isFile() && file.getName().equals(githubName+".json")) {
                                System.out.print(githubName + "已存在于" + dataSetDir+"  ");
                                line = reader.readNext(); // 有可能line变为null导致while报错
                            }
                        }
                    }
                }
                try {
                    GithubDetail hub = new GithubDetail();
                    hub.setGithubName(line[0]);
                    hub.setGithubLink(line[1]);
                    hub.setBranch(line[2]);
                    if(line.length>=4) hub.setStarCount(line[3]);
                    hub.setLocalTmpPath(tempDir + line[0]); //考虑用user_name的形式避免fork项目一样的文件夹
                    hub.setRepo(gitService.cloneIfNotExists(
                            hub.getLocalTmpPath(), hub.getGithubLink()));
                    repoList.add(hub);
                } catch (Exception e) {
                    //                        throw new RuntimeException(e); //打印出错误栈能知道详细信息 国内环境大概率需要魔法。
                    logger.error(line[1] + " Try git clone failed, there is a network problem. Please run Script_clone_github.py");
                }
            } else {
                logger.error(filePath + " have invalid line: " + Arrays.toString(line));
            }
        }
        return repoList;
    }
    public void commitTraverser(ArrayList<GithubDetail> repoList, int numThread) {
//        int numThreads = Runtime.getRuntime().availableProcessors() / 8; // 获取可用的处理器数量 ， 考虑到不好控制如果用cpu数量那么每个cpu最大负载是cpuNum个cppcheck
        ExecutorService executor = Executors.newFixedThreadPool(numThread); //因为cppcheck命令总是用并发，但并发是单独扫描上下限差异大会有单个线程等很久的cppcheck（超时一定程度避免），这里并发让同时扫描等待长的个数增长到Thread个
        List<Future<?>> futures = new ArrayList<>();

        for (GithubDetail repo : repoList) {
            Future<?> future = executor.submit(() -> {
                try {
                    traverserThread(repo);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            futures.add(future);
        }
//        设置扫描每个repo的超时逻辑 ， 首先每个repo能忍受的时间能上百小时
//        总体超时时间，但流程是计时-cppcheck-结束，应该继续优化检测cppcheck不能太长。 限制了单个commit超时时间这里也没什么用了
        long timeout = 60 * 60 * 720 ; // 超时时间（单位：秒）
        for (Future<?> future : futures) {
            try {
                future.get(timeout, TimeUnit.SECONDS); //通过commit数量进行预估
            } catch (TimeoutException e) {
                // 任务超时处理逻辑
                future.cancel(true); // 取消任务
                // 其他处理代码...
            } catch (Exception e) {
                // 其他异常处理逻辑
            }
        }
        executor.shutdown(); // 关闭线程池
    }
    public void traverserThread(GithubDetail repo) throws Exception {
        Logger logger = ThreadSpecificLoggerFactory.getLogger();
        String repoName = repo.getGithubName();
        ArrayList<StaticWarning> fixed_all = new ArrayList<>();
        ArrayList<StaticWarning> introduced_all = new ArrayList<>();
        ArrayList<StaticWarning> cur_wr = null;
        ArrayList<StaticWarning> next_wr = null; // next_commit的parent为cur
        String next_commit_id = null;
        ParserCppcheckWarning parser1 = new ParserCppcheckWarning();
        // you can use other parser and generate report.
        RevWalk walk = gitService.createAllRevsWalk(repo.getRepo(), repo.getBranch());
        walk.setRevFilter(RevFilter.ALL); //后续发现遍历没有包含merge，查看这创建walk的源码他自己实现了过滤只能有一个父也就是没merge
        if (walk.next() == null) { //git 2.28 默认分支变为main，这里尝试兼容
            walk = gitService.createAllRevsWalk(repo.getRepo(), "main");
            walk.setRevFilter(RevFilter.ALL);
            repo.setBranch("main");
        }
        long startTime = System.currentTimeMillis();
        try {
            int commitCount = gitService.countCommits(repo.getRepo(), repo.getBranch());
            int count = 0;
            for (RevCommit currentCommit : walk) {  //从最新commit到最早的
                if(count++ % (commitCount/8)==0) logger.warn(repoName+": Scanning progress "+count+"/"+commitCount); //打印十次文本形式进度条
//                currentCommit.getShortMessage();
                String curCommitId = currentCommit.getId().getName();
                gitService.checkout(repo.getRepo(), curCommitId);
                String xmlOutputPath = tempDir + repo.getGithubName() + "/cppcheck_report.xml";
                String logPath = tempDir + repo.getGithubName() + "/cppcheck_log"; //不用保留每次扫描文件 覆盖此文件，用数据结构保留
                GenerateCppcheckXML.report(repo.getLocalTmpPath(), xmlOutputPath, logPath, curCommitId);
                cur_wr = parser1.parseXml(xmlOutputPath, repo.getGithubLink(), curCommitId, next_commit_id);
                //                System.out.println(next_commit_id);
                next_commit_id = currentCommit.getId().getName();
                ArrayList<StaticWarning> fixed = getDiff(cur_wr, next_wr); // cur - next //考虑下next为空的初始状态含义
                ArrayList<StaticWarning> introduced = getDiff(next_wr, cur_wr); // next - cur 暂时粗略划分为false类型/不像定义修复那样准确
                fixed_all.addAll(fixed);
                introduced_all.addAll(introduced);
                next_wr = cur_wr;
//                if (flag==Boolean.FALSE && elapsedTime * (commit_count-count) > 60 * 60 * 1000) { // 预估一下最大时间（单个commit耗时*总数）做timeout
//                    logger.error(repo.getGithubLink()+" run will over 60 Min.  Please remove large c file. Skipping");
//                    return; // 跳出循环 并且不调用save | 如果使用break还会调用下面的save
//                } 实现了cppcheck等二进制调用超时控制
            }
        }catch (DocumentException e){
            logger.error("Check "+tempDir + repoName + "/cppcheck_log"+", can't parse report."); // 不会中断
        }catch (NullPointerException e){
            logger.error("Check "+tempDir + repoName+"/, can't git checkout（可能没有.git目录或分支不是 " + repo.getBranch());
        }
//                long elapsedTime =  System.currentTimeMillis() - startTime; // 计算已经经过的时间
        introduced_all.removeAll(fixed_all); // 这里intro - fixed 操作：考虑到某个bug在未来被fix了fixed会记录但introduced_all仍然保留着。
        if (!fixed_all.isEmpty() || !introduced_all.isEmpty()) { //是否保存的条件，同时前面会出现解析异常和git空指针的异常 此处选择全部认为
            save(repoName, introduced_all, fixed_all);
            logger.warn(repoName + " generate fixed: " + fixed_all.size()+" introduced: " + introduced_all.size());
        }else  logger.warn(repoName + " have no fixed or introduced warning.");

        logger.warn(repoName + " runs for "+ (System.currentTimeMillis() - startTime)/1000 + " seconds.\n\n\n");
    }


    public void save(String repoName, ArrayList<StaticWarning> introduced_all, ArrayList<StaticWarning> fixed_all) throws IOException {
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


    public void toJsonFile(String jsonPath, String jsonStr) throws IOException {
        File fjson = new File(jsonPath);
        File parentDir = fjson.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean exit = parentDir.mkdirs();
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
