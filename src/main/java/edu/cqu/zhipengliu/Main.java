package edu.cqu.zhipengliu;

import edu.cqu.zhipengliu.entity.GithubDetail;
import edu.cqu.zhipengliu.utils.GithubTraverser;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.

public class Main {
    public static final Logger logger = Logger.getLogger("SAWMiner");




    public static void main(String[] args) throws Exception {
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);
//    logger.addHandler(new fileHandler);
//        logger.addHandler(handler);
        // Press Alt+Enter with your caret at the highlighted text to see how
        // IntelliJ IDEA suggests fixing it.
        logger.info("项目启动");

        // Press Shift+F10 or click the green arrow button in the gutter to run the code.
        for (int i = 1; i <= 5; i++) {

            // Press Shift+F9 to start debugging your code. We have set one breakpoint
            // for you, but you can always add more by pressing Ctrl+F8.
            System.out.println("i = " + i);
        }
        GithubTraverser ts = new GithubTraverser();
        ArrayList<GithubDetail> gds = ts.getGithubSet("D:\\0Workspace\\IDEA-CODE\\SAWMiner\\c_repos_sorted.txt");
        ts.commitTraverser(gds);
    }
}