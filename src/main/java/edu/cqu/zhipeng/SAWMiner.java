package edu.cqu.zhipeng;

import edu.cqu.zhipeng.entity.GithubDetail;
import edu.cqu.zhipeng.utils.FileUtils;
import edu.cqu.zhipeng.utils.GitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static edu.cqu.zhipeng.utils.FileUtils.tempDir;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.

public class SAWMiner {
    public static Logger logger = LoggerFactory.getLogger(SAWMiner.class);

    public static void main(String[] args) throws Exception {

//    logger.addHandler(new fileHandler);
//        logger.addHandler(handler);
        // Press Alt+Enter with your caret at the highlighted text to see how
        // IntelliJ IDEA suggests fixing it.
        GithubTraverser ts = new GithubTraverser();

        String regexPattern = "^(https?://)?(www\\.)?github\\.com/.*$";
        Pattern pattern = Pattern.compile(regexPattern);
        int numThread = 1;
//        int numThreads = Runtime.getRuntime().availableProcessors() / 8; // 获取可用的处理器数量 ， 考虑到不好控制如果用cpu数量那么每个cpu最大负载是cpuNum个cppcheck
        if(args[0].endsWith(".csv")){
            logger.warn("Start load " + args[0] + "\n  If there is no output for a while, please run 'python3 Script_clone_github.py "+args[0]+"' first");
            ArrayList<GithubDetail> gds = FileUtils.getGithubSet(args[0]);
            if(args.length==2 && args[1].matches("\\d+")) numThread = Integer.parseInt(args[1]);
            logger.warn(numThread + " threads start scanning (./tmp_github): " + gds.size());
            ts.commitTraverser(gds, numThread);

        }else if(pattern.matcher(args[0]).matches()){
            if(args.length!=2) System.out.println(" Example command: java -jar miner.jar https://github.com/xxx/x main");
            logger.warn("Start load " + args[0] + "\n  If there is no output for a while, please git clone first");
            GithubDetail hub = getGithubDetail(args[0]);
            hub.setBranch(args[1]);
            ts.commitTraverserThread(hub);
        }else {
            System.out.println("Need args. Example: java -jar miner.jar repo_test.csv 6 or java -jar miner.jar https://github.com/xxx/x main");
        }
        System.out.println("ALL DONE. Please check ./GeneratedDataset");



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

    private static GithubDetail getGithubDetail(String githubLink){
        githubLink = githubLink.replace(".git", "");
        String[] urlParts = githubLink.split("/");
        String github_name = urlParts[urlParts.length - 1];
        GithubDetail hub = new GithubDetail();
        hub.setGithubLink(githubLink);
        hub.setGithubName(github_name);
        hub.setLocalTmpPath(tempDir + github_name);
        try {
            hub.setRepo(GitUtils.cloneIfNotExists(
                    hub.getLocalTmpPath(),githubLink));
        } catch (Exception e) {
            logger.error("Try [git clone "+githubLink+" ./tmp_github/"+github_name+"] failed, there is a network problem. Please manual git clone");
        }
        return hub;
    }
}

