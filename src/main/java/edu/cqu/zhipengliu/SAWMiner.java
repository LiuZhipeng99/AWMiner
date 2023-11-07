package edu.cqu.zhipengliu;

import edu.cqu.zhipengliu.entity.GithubDetail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.

public class SAWMiner {
    public static final Logger logger = Logger.getLogger("SAWMiner");


    public static void main(String[] args) throws Exception {
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);
//    logger.addHandler(new fileHandler);
//        logger.addHandler(handler);
        // Press Alt+Enter with your caret at the highlighted text to see how
        // IntelliJ IDEA suggests fixing it.
        logger.info("项目启动");


        GithubTraverser ts = new GithubTraverser();
        ArrayList<GithubDetail> gds = ts.getGithubSet(args[0]);
        System.out.println("本次克隆结束数量(已缓存在tmp目录）：" + gds.size());
        ts.commitTraverser(gds);


        //        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
//        miner.detectAll(repo, "master", new RefactoringHandler() {
//            @Override
//            public void handle(String commitId, List<Refactoring> refactorings) {
//                System.out.println("Refactorings at " + commitId);
//                for (Refactoring ref : refactorings) {
////                    System.out.println(ref.toString());
//                }
//            }
    }
}