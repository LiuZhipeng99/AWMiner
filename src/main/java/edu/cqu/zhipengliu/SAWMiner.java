package edu.cqu.zhipengliu;

import com.opencsv.exceptions.CsvValidationException;
import edu.cqu.zhipengliu.entity.GithubDetail;
import org.dom4j.DocumentException;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

import static edu.cqu.zhipengliu.GithubTraverser.tempDir;
// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.

public class SAWMiner {
    static Logger logger = LoggerFactory.getLogger(SAWMiner.class);

    public static void main(String[] args) throws CsvValidationException, IOException {

//    logger.addHandler(new fileHandler);
//        logger.addHandler(handler);
        // Press Alt+Enter with your caret at the highlighted text to see how
        // IntelliJ IDEA suggests fixing it.
        GithubTraverser ts = new GithubTraverser();
        ArrayList<GithubDetail> gds = new ArrayList<>();
        String regexPattern = "^(https?://)?(www\\.)?github\\.com/.*$";
        Pattern pattern = Pattern.compile(regexPattern);
        int numThread = 1;
        if(args[0].endsWith(".csv")){
            logger.warn("Start load " + args[0] + "\nIf no output, please run 'python3 Script_clone_github.py "+args[0]+"' first");
            gds = ts.getGithubSet(args[0]);
            if(args.length==2 && args[1].matches("\\d+")) numThread = Integer.parseInt(args[1]);
        }else if(pattern.matcher(args[0]).matches()){
            if(args.length!=2) System.out.println(" Example command: java -jar miner.jar https://github.com/xxx/x main");
            logger.warn("Start load " + args[0] + "\nIf no output, please git clone first");
            GithubDetail hub = getGithubDetail(args[0]);
            hub.setBranch("master");
            gds.add(hub);
        }else {
            System.out.println("Need args. Example: java -jar miner.jar repo_test.csv 12 or java -jar miner.jar https://github.com/xxx/x main");
        }
        logger.warn(numThread + " threads start scanning (./tmp_github): " + gds.size());
        ts.commitTraverser(gds, numThread);





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
        String[] urlParts = githubLink.split("/");
        String github_name = urlParts[urlParts.length - 1].replace(".git", "");
        GithubDetail hub = new GithubDetail();
        hub.setGithubLink(githubLink);
        hub.setGithubName(github_name);
        hub.setLocalTmpPath(tempDir + github_name);
        hub.setBranch("master");
        GitServiceImpl gitService = new GitServiceImpl();
        try {
            hub.setRepo(gitService.cloneIfNotExists(
                    hub.getLocalTmpPath(),githubLink));
        } catch (Exception e) {
            logger.error("Try [git clone "+githubLink+" ./tmp_github/"+github_name+"] failed, there is a network problem. Please manual git clone");
        }
        return hub;
    }
}