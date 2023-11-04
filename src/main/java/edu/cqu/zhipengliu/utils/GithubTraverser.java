package edu.cqu.zhipengliu.utils;

import edu.cqu.zhipengliu.entity.GithubDetail;
import edu.cqu.zhipengliu.entity.WarningCppcheck;
import edu.cqu.zhipengliu.parser.CppcheckParser;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.refactoringminer.util.GitServiceImpl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.cqu.zhipengliu.Main.logger;


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
                    hub.setLocalTmpPath("githubTmp/" + parts[3]); //克隆下来的目录设置在./githubTmp（目前写死）
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
            RevWalk walk = gitService.createAllRevsWalk(repo.getRepo(), repo.getBranch());
            int count = 0;
            ArrayList<WarningCppcheck> wr_old = new ArrayList<>();
            walk.setRevFilter(RevFilter.ALL); //后续发现遍历没有包含merge，查看这创建walk的源码他自己实现了过滤只能有一个父也就是没merge
            for (RevCommit currentCommit : walk) {
                logger.info(currentCommit.getId().getName());
                gitService.checkout(repo.getRepo(), currentCommit.getId().getName());
                //不用写太多文件，根据commitid再扫一次就有了GenerateCppcheckXML.report(projectpath,projectpath+"report"+currentCommit.getId().getName()+".xml",projectpath+"log"+currentCommit.getId().getName());
//                GenerateCppcheckXML.report(repo.getGithubName(), repo.getLocalTmpPath() + "report.xml", repo.getLocalTmpPath() + "log");
//                ArrayList<WarningCppcheck> wr = new CppcheckParser().parseWarningsXML(repo.getLocalTmpPath() + "report.xml", currentCommit.getId().getName());
                count++;
                //            if(count>1){break;}

            }
        }
    }
    public void tmps(ArrayList<GithubDetail> repoList) throws Exception {
        FileOutputStream logos = new FileOutputStream("D:/log-brpc-wr" + ".txt", true);
        for (GithubDetail repo : repoList) {
            RevWalk walk = gitService.createAllRevsWalk(repo.getRepo(), repo.getBranch());
            //        Iterable<RevCommit> walk = gitService.createRevsWalkBetweenCommits(repo,"88db699b4d5935d8dcce4daf90b1aa2b28b2a48b","9ad9f45db59bd69a943a7c759859031da2051f8e");
            //        Iterator<RevCommit> walk = gitService.createRevsWalkBetweenTags(repo,"")
            int count = 0;
            ArrayList<WarningCppcheck> wr_old = new ArrayList<>();
            walk.setRevFilter(RevFilter.ALL); //后续发现遍历没有包含merge，查看这创建walk的源码他自己实现了过滤只能有一个父也就是没merge
            for (RevCommit currentCommit : walk) {
                System.out.println(currentCommit.getId().getName());
                //            gitService.fileTreeDiff(repo, currentCommit, filePathsBefore, filePathsCurrent, renamedFilesHint);
                gitService.checkout(repo.getRepo(), currentCommit.getId().getName());
                //不用写太多文件，根据commitid再扫一次就有了GenerateCppcheckXML.report(projectpath,projectpath+"report"+currentCommit.getId().getName()+".xml",projectpath+"log"+currentCommit.getId().getName());
                GenerateCppcheckXML.report(repo.getGithubName(), repo.getLocalTmpPath() + "report.xml", repo.getLocalTmpPath() + "log");
                ArrayList<WarningCppcheck> wr = new CppcheckParser().parseWarningsXML(repo.getLocalTmpPath() + "report.xml", currentCommit.getId().getName());
                System.out.println("warning_nums:" + wr.size());
                //            warningCppchekRepository.saveAll(wr);
                //true表示在文件末尾追加

                String log = GenerateCppcheckXML.compare(wr_old, wr);//找出新版本v2减少了哪些warning 对调参数就是找增加了哪些

                logos.write(log.getBytes());
                logos.write(System.getProperty("line.separator").getBytes());
                logos.write(System.getProperty("line.separator").getBytes());
                wr_old = wr;
                count++;
                //            if(count>1){break;}

            }
        }
        logos.close();
    }
}
