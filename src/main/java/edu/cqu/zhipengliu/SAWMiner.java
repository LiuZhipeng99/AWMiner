package edu.cqu.zhipengliu;

import edu.cqu.zhipengliu.entity.GithubDetail;
import org.dom4j.DocumentException;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

import static edu.cqu.zhipengliu.GithubTraverser.tempDir;
// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.

public class SAWMiner {
    static Logger logger = LoggerFactory.getLogger(SAWMiner.class);

    public static void main(String[] args) throws Exception {

//    logger.addHandler(new fileHandler);
//        logger.addHandler(handler);
        // Press Alt+Enter with your caret at the highlighted text to see how
        // IntelliJ IDEA suggests fixing it.


        GithubTraverser ts = new GithubTraverser();
        ArrayList<GithubDetail> gds = new ArrayList<>();
        System.out.println("your args:" + Arrays.toString(args));
        String regexPattern = "^(https?://)?(www\\.)?github\\.com/.*$";
        Pattern pattern = Pattern.compile(regexPattern);
        if(args[0].endsWith(".csv")){
            gds = ts.getGithubSet(args[0]);
        }else if(pattern.matcher(args[0]).matches()){
            GithubDetail hub = getGithubDetail(args[0]);
            gds.add(hub);
        }else {
            logger.error("Args required for running is repo.csv or githubUrl.");
        }
        logger.warn("SAWMiner started, if no output, please run 'python3 clone_git.py "+args[0]+"' first");

        if(args.length>=2 && Objects.equals(args[1], "False")){
            logger.warn("Scan Github Num(in tmp_github）：" + gds.size());
            logger.warn("扫描时间超过60Min就跳过那个repo");
            ts.commitTraverser(gds,Boolean.FALSE);
            return;
        }
        logger.warn("Scan Github Num(in tmp_github）：" + gds.size());
        ts.commitTraverser(gds,Boolean.TRUE);


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

    private static GithubDetail getGithubDetail(String githubLink) throws Exception {
        String[] urlParts = githubLink.split("/");
        String github_name = urlParts[urlParts.length - 1];
        GithubDetail hub = new GithubDetail();
        hub.setGithubLink(githubLink);
        hub.setGithubName(github_name);
        hub.setLocalTmpPath(tempDir + github_name);
        hub.setBranch("master");
        GitServiceImpl gitService = new GitServiceImpl();
        hub.setRepo(gitService.cloneIfNotExists(
                hub.getLocalTmpPath(),hub.getGithubLink()));
        return hub;
    }
}