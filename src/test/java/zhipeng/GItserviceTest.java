package zhipeng;


import edu.cqu.zhipengliu.parser.CppcheckParser;
import edu.cqu.zhipengliu.utils.GenerateCppcheckXML;
import edu.cqu.zhipengliu.entity.WarningCppcheck;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.junit.Test;
import org.refactoringminer.util.GitServiceImpl;

import java.io.FileOutputStream;
import java.util.ArrayList;

//@SpringBootTest
//@RunWith(SpringRunner.class)
public class GItserviceTest {
//    @Autowired
//    private WarningCppchekRepository warningCppchekRepository;

    @Test //测试遍历一个项目的commit，//使用jgit的api的过程中意识到，不通过git log解析，用其api做文件diff就行了。甚至有changeType的预定义（看到有加减和rename三种）
    public void testgitservice() throws Exception {
        GitServiceImpl gitService = new GitServiceImpl(); //这里用多态特性实例化GitService，也没什么意义不如直接实例化，主要是也没其他gitservice实现
        String projectpath = "tmp/brpc/";
        Repository repo = gitService.cloneIfNotExists(
                projectpath,
                "https://github.com/chenshuo/muduo.git");
        RevWalk walk = gitService.createAllRevsWalk(repo, "master");
//        Iterable<RevCommit> walk = gitService.createRevsWalkBetweenCommits(repo,"88db699b4d5935d8dcce4daf90b1aa2b28b2a48b","9ad9f45db59bd69a943a7c759859031da2051f8e");
//        Iterator<RevCommit> walk = gitService.createRevsWalkBetweenTags(repo,"")
        int count = 0;
        ArrayList<WarningCppcheck> wr_old=new ArrayList<>();
        FileOutputStream fos = new FileOutputStream("D:/log-brpc-wr" +
                ".txt",true);
        walk.setRevFilter(RevFilter.ALL); //后续发现遍历没有包含merge，查看这创建walk的源码他自己实现了过滤只能有一个父也就是没merge
        for (RevCommit currentCommit : walk) {
            System.out.println(currentCommit.getId().getName());
//            gitService.fileTreeDiff(repo, currentCommit, filePathsBefore, filePathsCurrent, renamedFilesHint);
            gitService.checkout(repo, currentCommit.getId().getName());
//不用写太多文件，根据commitid再扫一次就有了GenerateCppcheckXML.report(projectpath,projectpath+"report"+currentCommit.getId().getName()+".xml",projectpath+"log"+currentCommit.getId().getName());
            GenerateCppcheckXML.report(projectpath,projectpath+"report.xml",projectpath+"log");
            ArrayList<WarningCppcheck> wr = new CppcheckParser().parseWarningsXML(projectpath+"report.xml",currentCommit.getId().getName());
            System.out.println("warning_nums:"+wr.size());
//            warningCppchekRepository.saveAll(wr);
            //true表示在文件末尾追加

            String log = GenerateCppcheckXML.compare(wr_old,wr);//找出新版本v2减少了哪些warning 对调参数就是找增加了哪些

            fos.write(log.getBytes());
            fos.write(System.getProperty("line.separator").getBytes());
            fos.write(System.getProperty("line.separator").getBytes());
            wr_old = wr;
            count++;
//            if(count>1){break;}

        }
        fos.close();

//        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
//        miner.detectAll(repo, "master", new RefactoringHandler() {
//            @Override
//            public void handle(String commitId, List<Refactoring> refactorings) {
//                System.out.println("Refactorings at " + commitId);
//                for (Refactoring ref : refactorings) {
////                    System.out.println(ref.toString());
//                }
//            }
//        });
    }


    @Test //测试遍历多个项目的commit
    public void testtravelgit(){

    }
}
