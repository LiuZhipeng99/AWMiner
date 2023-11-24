package zhipeng;

import edu.cqu.zhipeng.utils.GitUtils;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.junit.Test;

/**
 * @projectName: SAWMiner
 * @package: zhipeng
 * @className: TestCommitPair
 * @author: Zhipengliu
 * @description: TODO
 * @date: 2023/11/21 15:56
 * @version: 1.1
 */
public class TestCommitPair {
    @Test
    public void testfun() throws Exception {

        Repository re = GitUtils.cloneIfNotExists(
                "tmp_github/HdrHistogram_c", "https://github.com/HdrHistogram/HdrHistogram_c");
        System.out.println(re.getFullBranch() + GitUtils.countAllCommits(re,re.getBranch()));

//        ArrayList<String[]> fff =  GitUtils.createCommitPairList(GitUtils.cloneIfNotExists(
//                "tmp/", "https://github.com/tmux/tmux"), "master");
//        for(String[] a:fff){
//            System.out.println(a[0]);
//            System.out.println(a[1]);
//        }
    }

    @Test
    public void testGetCommitPair(){
        try {
            Repository repository = GitUtils.cloneIfNotExists(
               "tmp/", "https://github.com/tmux/tmux");
            RevWalk revWalk = new RevWalk(repository);

            ObjectId lastCommitId = repository.resolve("refs/heads/"+repository.getBranch()); // replace "main" with your branch name
            RevCommit lastCommit = revWalk.parseCommit(lastCommitId);
            revWalk.markStart(lastCommit);
            revWalk.setRevFilter(RevFilter.NO_MERGES);// No merge情况下的countpair 数量就是all-merge的commit数量减去only merge数量// Only-merge情况下countpair数量是only merge的commit数量两倍
            int countpair =0;
            for (RevCommit commit : revWalk) {
                RevCommit[] parentCommits = commit.getParents();
                for (RevCommit parent : parentCommits) {
                    countpair++;
                    System.out.println("Child commit: " + commit.getName());
                    System.out.println("Parent commit: " + parent.getName());
                    System.out.println("-----------------------------------");
                }
            }
            System.out.println(repository.getBranch());
            System.out.println(countpair);
            System.out.println("==");
            System.out.println(countMergeCommits(repository));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public int countMergeCommits(Repository repository) throws Exception {
        int mergeCommitsCount = 0;
        try (RevWalk walk = new RevWalk(repository)) {
            walk.markStart(walk.parseCommit(repository.resolve(Constants.HEAD)));
            for (RevCommit commit : walk) {
                if (commit.getParentCount() > 1) {
                    mergeCommitsCount++;
                }
            }
        }
        return mergeCommitsCount;
    }


}
