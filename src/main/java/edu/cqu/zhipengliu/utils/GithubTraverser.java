package edu.cqu.zhipengliu.utils;

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
 * @description: 遍历c_repos_sorted.txt中描述的GitHub项目，遍历其commit，生成报告
 * @date: 2023/11/4 15:15
 * @version: 1.1
 */

//其实c_repos_sorted描述的项目信息可以更多比如：爬取时date、项目描述、作者、语言类型等等，甚至是个数据库不知道有没有现成的
public class GithubTraverser {
    GitServiceImpl gitService = new GitServiceImpl(); //这里用多态特性实例化GitService，也没什么意义不如直接实例化，主要是也没其他gitservice实现

    public ArrayList<Repository> githubtraverser(String filePath1) throws Exception {
        ArrayList<Repository> repolist = new ArrayList<>();
        String projectpath = "tmp/brpc/";
        String filePath = "D:\\0Workspace\\IDEA-CODE\\SAWMiner\\c_repos_sorted.txt";
        Map<String, String[]> dataMap = new HashMap<>();
        Path path = Paths.get(filePath);  // io.FileReader
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                String[] parts = line.split("\t");
                if (parts.length >= 4) {
                    String id = parts[0];
                    String starCount = parts[1];
                    String link = parts[2];
                    String name = parts[3];
                    String[] values = {starCount, link, name};
                    dataMap.put(id, values);
                } else {
                    logger.warning("无效的行: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        Repository repo = gitService.cloneIfNotExists(
//                projectpath,
//                "https://github.com/chenshuo/muduo.git");
        return repolist;
    } //这个函数就是克隆很多github项目，考虑到网络和io可以优化。
    public void traverser(ArrayList<Repository> repoList) throws FileNotFoundException {
//        for (Repository repo : repoList) {
//            RevWalk walk = gitService.createAllRevsWalk(repo, "master");
//            //        Iterable<RevCommit> walk = gitService.createRevsWalkBetweenCommits(repo,"88db699b4d5935d8dcce4daf90b1aa2b28b2a48b","9ad9f45db59bd69a943a7c759859031da2051f8e");
//            //        Iterator<RevCommit> walk = gitService.createRevsWalkBetweenTags(repo,"")
//            int count = 0;
//            ArrayList<WarningCppcheck> wr_old = new ArrayList<>();
//            FileOutputStream fos = new FileOutputStream("D:/log-brpc-wr" +
//                    ".txt", true);
//            walk.setRevFilter(RevFilter.ALL); //后续发现遍历没有包含merge，查看这创建walk的源码他自己实现了过滤只能有一个父也就是没merge
//            for (RevCommit currentCommit : walk) {
//                System.out.println(currentCommit.getId().getName());
//                //            gitService.fileTreeDiff(repo, currentCommit, filePathsBefore, filePathsCurrent, renamedFilesHint);
//                gitService.checkout(repo, currentCommit.getId().getName());
//                //不用写太多文件，根据commitid再扫一次就有了GenerateCppcheckXML.report(projectpath,projectpath+"report"+currentCommit.getId().getName()+".xml",projectpath+"log"+currentCommit.getId().getName());
//                GenerateCppcheckXML.report(projectpath, projectpath + "report.xml", projectpath + "log");
//                ArrayList<WarningCppcheck> wr = new CppcheckParser().parseWarningsXML(projectpath + "report.xml", currentCommit.getId().getName());
//                System.out.println("warning_nums:" + wr.size());
//                //            warningCppchekRepository.saveAll(wr);
//                //true表示在文件末尾追加
//
//                String log = GenerateCppcheckXML.compare(wr_old, wr);//找出新版本v2减少了哪些warning 对调参数就是找增加了哪些
//
//                fos.write(log.getBytes());
//                fos.write(System.getProperty("line.separator").getBytes());
//                fos.write(System.getProperty("line.separator").getBytes());
//                wr_old = wr;
//                count++;
//                //            if(count>1){break;}
//
//            }
//            fos.close();
//        }
    }
}
