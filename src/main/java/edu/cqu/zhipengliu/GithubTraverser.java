package edu.cqu.zhipengliu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.cqu.zhipengliu.entity.GithubDetail;
import edu.cqu.zhipengliu.entity.StaticWarning;
import edu.cqu.zhipengliu.entity.WarningCppcheck;
import edu.cqu.zhipengliu.parser.ParserCppcheckWarning;
import edu.cqu.zhipengliu.utils.GenerateCppcheckXML;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.refactoringminer.util.GitServiceImpl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static edu.cqu.zhipengliu.SAWMiner.logger;


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
    public final String tempDir = "tmp_github/"; //克隆下来的目录设置（目前写死）

    public ArrayList<GithubDetail> getGithubSet(String filePath) throws IOException {
        //TODO 添加GitHub镜像功能。 函数克隆很多github项目》返回repolist，考虑到网络和io可以优化。克隆时间是否应该区分，为方便更新代码（简单实现就是把目录加上date比如带月或者日）
        //TODO 这个函数紧密依赖txt文件行存储 读入用字符串split，用格式化如json会更好。目前用长度判断行是否有效（以后可以匹配模式）
        // 先读文件再克隆优化为边读边克隆 同时也可以实例化GitHubDetail
        ArrayList<GithubDetail> repoList = new ArrayList<>();
        Path path = Paths.get(filePath);  // io.FileReader
        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
            String[] parts = line.split("\t");
            if (parts.length >= 4) {
                String id = parts[0];
                try {
                    GithubDetail hub = new GithubDetail();
                    hub.setGithubName(parts[3]);
                    hub.setGithubLink(parts[2]);
                    hub.setStarCount(parts[1]);
                    hub.setBranch(parts.length >= 5 ? parts[4] : "master");//git 2.28版默认变为main
                    hub.setLocalTmpPath(tempDir + parts[3]);
                    hub.setRepo(gitService.cloneIfNotExists(
                            hub.getLocalTmpPath(),hub.getGithubLink()));
                    repoList.add(hub);
                } catch (Exception e) {
//                        throw new RuntimeException(e); //打印出错误栈能知道详细信息 如果国内环境大概率需要魔法。
                    logger.warning(parts[2] + "  Git clone failed. Please check the link. There is a network problem, you can use the mirror website.");
                }
            } else {
                logger.warning(filePath + " Invalid line:: " + line);
            }
        }
        return repoList;
    }
    public void commitTraverser(ArrayList<GithubDetail> repoList) throws Exception {
        for (GithubDetail repo : repoList) {
            ArrayList<StaticWarning> fixed_all = new ArrayList<>();
            ArrayList<StaticWarning> pre_wr = new ArrayList<>();
            ParserCppcheckWarning parser1 =  new ParserCppcheckWarning();
            RevWalk walk = gitService.createAllRevsWalk(repo.getRepo(), repo.getBranch());
            walk.setRevFilter(RevFilter.ALL); //后续发现遍历没有包含merge，查看这创建walk的源码他自己实现了过滤只能有一个父也就是没merge
            for (RevCommit currentCommit : walk) {
                gitService.checkout(repo.getRepo(), currentCommit.getId().getName());
                //GenerateCppcheckXML.report(tempDir,tempDir+"report"+currentCommit+".xml");不用保留每次扫描文件，结果存在fixed all
                String xmlOutputPath = tempDir+ repo.getGithubName() + "/cppcheck_report.xml";
                String logPath = tempDir+ repo.getGithubName() + "/cppcheck_log";
                GenerateCppcheckXML.report(repo.getLocalTmpPath(), xmlOutputPath, logPath);
                ArrayList<StaticWarning> cur_wr = parser1.parseXml(xmlOutputPath, repo.getGithubName(), currentCommit.getId().getName());

                ArrayList<StaticWarning> fixed_cur = getFixed(pre_wr,cur_wr);
                ArrayList<StaticWarning> introduced = getIntroduced(pre_wr,cur_wr); //此工具没有研究引入的，只关注fixed warning
                fixed_all.addAll(fixed_cur);
                pre_wr = cur_wr;
//                for (WarningCppcheck warning : fixed) {
//                    System.out.println(warning.getHash_id());
//                    System.out.println(warning);
//                }
            }
            if(!fixed_all.isEmpty()) {
                // 将ArrayList转换为JSON
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = null;
                if (Objects.equals(fixed_all.get(0).getTool_name(), "cppcheck")) {
                    Type CppcheckType = new TypeToken<ArrayList<WarningCppcheck>>() {
                    }.getType();
                    json = gson.toJson(fixed_all, CppcheckType);
                    System.out.println(repo.getGithubName() + "Cppcheck generate fixed:" + fixed_all.size());

                }
                // 将JSON保存到文件
                String jsonPath = "GeneratedDataset/AWarning_" + repo.getGithubName() + ".json";
                File file = new File(jsonPath);
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                FileWriter writer = new FileWriter(file);
                writer.write(json);
                writer.close();
            }
        }
    }

    //    public void diffReport(ArrayList<StaticWarning> pre, ArrayList<StaticWarning> cur){}
    public ArrayList<StaticWarning> getFixed(ArrayList<StaticWarning> pre, ArrayList<StaticWarning> cur){
        // 查找消失的元素
        ArrayList<StaticWarning> removedElements = new ArrayList<>();
        for (StaticWarning warning : pre) {
            if (!cur.contains(warning)) {
                removedElements.add(warning);
            }
        }
        return removedElements;
    }
    public ArrayList<StaticWarning> getIntroduced(ArrayList<StaticWarning> pre, ArrayList<StaticWarning> cur){
        // 查找新增的元素
        ArrayList<StaticWarning> addedElements = new ArrayList<>();
        for (StaticWarning warning : cur) {
            if (!pre.contains(warning)) {
                addedElements.add(warning);
            }
        }
        return addedElements;
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
