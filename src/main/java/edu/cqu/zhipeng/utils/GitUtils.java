package edu.cqu.zhipeng.utils;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevWalkUtils;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static edu.cqu.zhipeng.SAWMiner.logger;

/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.utils
 * @className: GitUtils
 * @author: Zhipengliu
 * @description: 分离出Git仓库操作的函数
 * @date: 2023/11/21 16:35
 * @version: 1.1
 */
public class GitUtils {
    public static final String REMOTE_REFS_PREFIX = "refs/remotes/origin/";
    public static Repository cloneIfNotExists(String projectPath, String cloneUrl/*, String branch*/) throws Exception {
        return cloneIfNotExists(projectPath, cloneUrl, null, null);
    }

    public static Repository cloneIfNotExists(String projectPath, String cloneUrl, String username, String token) throws Exception {
        File folder = new File(projectPath);
        Repository repository;
        if (folder.exists()) {
            String[] contents = folder.list();
            boolean dotGitFound = false;
            for(String content : contents) {
                if(content.equals(".git")) {
                    dotGitFound = true;
                    break;
                }
            }
            RepositoryBuilder builder = new RepositoryBuilder();
            repository = builder
                    .setGitDir(dotGitFound ? new File(folder, ".git") : folder)
                    .readEnvironment()
                    .findGitDir()
                    .build();

            //logger.info("Project {} is already cloned, current branch is {}", cloneUrl, repository.getBranch());
        } else {
            logger.info("Cloning {} ...", cloneUrl);
            CredentialsProvider credentialsProvider = null;
            if (username != null && token != null){
                credentialsProvider = new UsernamePasswordCredentialsProvider(username, token);
            }
            Git git = Git.cloneRepository()
                    .setDirectory(folder)
                    .setURI(cloneUrl)
                    .setCloneAllBranches(true)
                    .setCredentialsProvider(credentialsProvider)
                    .call();
            repository = git.getRepository();
            //logger.info("Done cloning {}, current branch is {}", cloneUrl, repository.getBranch());
        }
        return repository;
    }

    public static void checkout(Repository repository, String commitId) throws Exception {
        logger.info("Checking out {} {} ...", repository.getDirectory().getParent().toString(), commitId);
        try (Git git = new Git(repository)) {
            CheckoutCommand checkout = git.checkout().setName(commitId);
            checkout.call();
        }
//		File workingDir = repository.getDirectory().getParentFile();
//		ExternalProcess.execute(workingDir, "git", "checkout", commitId);
    }

    public static void checkout2(Repository repository, String commitId) throws Exception {
        logger.info("Checking out {} {} ...", repository.getDirectory().getParent().toString(), commitId);
        File workingDir = repository.getDirectory().getParentFile();
        String output = ProcessUtils.execute(workingDir, "git", "checkout", commitId);
        if (output.startsWith("fatal")) {
            throw new RuntimeException("git error " + output);
        }
    }

    public static int countAllCommits(Repository repository, String branch) throws Exception {
        RevWalk walk = new RevWalk(repository);
        try {
            Ref ref = repository.findRef(REMOTE_REFS_PREFIX + branch);
            ObjectId objectId = ref.getObjectId();
            RevCommit start = walk.parseCommit(objectId);
            walk.setRevFilter(RevFilter.ALL); // ALL在这
            return RevWalkUtils.count(walk, start, null);
        } finally {
            walk.dispose();
        }
    }

//            Rewalk walk = gitService.createAllRevsWalk(repo.getRepo(), "main");
//            walk.setRevFilter(RevFilter.ALL);
//            for (RevCommit currentCommit : walk)   //从最新commit到最早的自带commit-pair，但不是父子关系且包含merge，下面的方法才能得到父子pair
    public static ArrayList<String[]> createCommitPairList(Repository repository, String branch){
        ArrayList<String[]> commit_pairs = new ArrayList<>(); //直接是个string[]没定义结构默认 第二个是current第一个是parent。
        try {
            RevWalk revWalk = new RevWalk(repository);

            ObjectId lastCommitId = repository.resolve("refs/heads/"+branch); // 等同于refs/remotes/origin/branch
            RevCommit lastCommit = revWalk.parseCommit(lastCommitId);
            revWalk.markStart(lastCommit);
            revWalk.setRevFilter(RevFilter.NO_MERGES);
            for (RevCommit commit : revWalk) {
                RevCommit[] parentCommits = commit.getParents();
                for (RevCommit parent : parentCommits) { //因为限制了没有merge所以只会循环一次 . 如果是初始节点还没有父
                    String currentCommit = commit.getName();
                    String parentCommit = parent.getName();
                    commit_pairs.add(new String[]{commit.getShortMessage(),parentCommit, currentCommit});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return commit_pairs;

    }

    public RevWalk createAllRevsWalk(Repository repository, String branch) throws Exception {
        List<ObjectId> currentRemoteRefs = new ArrayList<ObjectId>();
        for (Ref ref : repository.getRefDatabase().getRefs()) {
            String refName = ref.getName();
            if (refName.startsWith(REMOTE_REFS_PREFIX)) {
                if (branch == null || refName.endsWith("/" + branch)) {
                    currentRemoteRefs.add(ref.getObjectId());
                }
            }
        }

        RevWalk walk = new RevWalk(repository);
        for (ObjectId newRef : currentRemoteRefs) {
            walk.markStart(walk.parseCommit(newRef)); //当walk有多个起始点时，for (RevCommit currentCommit : walk)的currentCommit和单个起始点的有什么区别？GPT答按时间
        }
//        DefaultCommitsFilter commitsFilter = new DefaultCommitsFilter();
//        walk.setRevFilter(commitsFilter);//想自己实现个filter算法而不是使用默认的
//        walk.setRevFilter(RevFilter.NO_MERGES); //和上面DefaultCommitsFilter效果一样
        return walk;
    }
//    private class DefaultCommitsFilter extends RevFilter {
//        @Override
//        public final boolean include(final RevWalk walker, final RevCommit c) {
//            return c.getParentCount() == 1 ;//&& !isCommitAnalyzed(c.getName());
//        }
//
//        @Override
//        public final RevFilter clone() {
//            return this;
//        }
//
//        @Override
//        public final boolean requiresCommitBody() {
//            return false;
//        }
//
//        @Override
//        public String toString() {
//            return "RegularCommitsFilter";
//        }
//    }


    public static String getGitDiff(Repository repository, String commitId) throws Exception {
        // Resolve the commit ID to a RevCommit object
        ObjectId commitObjectId = repository.resolve(commitId);
        String diffText = null;
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(commitObjectId);
            RevCommit parent = revWalk.parseCommit(commit.getParent(0).getId());

            // Prepare the two iterators to compute the diff between
            try (ObjectReader reader = repository.newObjectReader()) {
                AbstractTreeIterator newTreeIter = new CanonicalTreeParser(null, reader, commit.getTree().getId());
                AbstractTreeIterator oldTreeIter = new CanonicalTreeParser(null, reader, parent.getTree().getId());

                // Finally get the list of changed files
                for (DiffEntry entry : getDiffEntries(repository, oldTreeIter, newTreeIter)) {
//                    System.out.println("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        try (DiffFormatter diffFormatter = new DiffFormatter(out)) {
                            diffFormatter.setRepository(repository);
                            diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
                            diffFormatter.setDetectRenames(true);
                            diffFormatter.format(entry);
                            diffText = out.toString();
                        }
                    }
                }
            }
        }
        return diffText;
    }
    private static List<DiffEntry> getDiffEntries(Repository repository, AbstractTreeIterator oldTreeIter, AbstractTreeIterator newTreeIter) throws Exception {
        try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            diffFormatter.setRepository(repository);
            diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
            diffFormatter.setDetectRenames(true);
            return diffFormatter.scan(oldTreeIter, newTreeIter);
        }
    }


}
