package zhipeng;

import edu.cqu.zhipeng.entity.GithubDetail;
import edu.cqu.zhipeng.entity.StaticWarning;
import edu.cqu.zhipeng.utils.CloneUtil;
import edu.cqu.zhipeng.utils.GitUtils;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class TestGitservice
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }
    public static class Person implements Serializable {
        String warning_message;
        String warning_severity;

        // 添加一个复制构造函数
//        public Person(StaticWarning other) {
//            this.warning_message = other.getWarning_message();
//        }

        public Person() {
            this.warning_message = "init";
        }
        // 添加一个克隆方法

        @Override
        public String toString() {
            return warning_message;
        }
    }
    @Test
    public void test() throws Exception {
        // Assume that the repository is initialized
        Repository repository = GitUtils.cloneIfNotExists(
                "tmp_github/HdrHistogram_c", "https://github.com/HdrHistogram/HdrHistogram_c");

        String commitId = "";
//        getDiff(repository,"b619f348e840a42d935c4dfacbf852faff3653c9");


        //测试深拷贝
        ArrayList<Person> originalList = new ArrayList<>();
        Person warning1 = new Person();
        originalList.add(warning1);

        // 浅拷贝列表 只有对象列表是String等常数时候才能这样拷贝，这只有一层，如果对象列表是对象需要实现其clone去决定它自己的变量如何复制，可能有嵌套的，用CloneUtil可以真拷贝
        ArrayList<Person> copiedList = new ArrayList<>(originalList);
        // 深拷贝 基本都需要for一下
        ArrayList<Person> copiedL2 = new ArrayList<>();
        for (Person originalWarning : originalList) {
            copiedL2.add(CloneUtil.clone(originalWarning));
        }
        originalList.get(0).warning_message = "TTTTTTTT";
        // 验证
        System.out.println("Original List: " + originalList);
        System.out.println("Copied List: " + copiedList);
        System.out.println("Copied List: " + copiedL2);

    }

    public void getDiff(Repository repository, String commitId) throws Exception {
        // Resolve the commit ID to a RevCommit object
        ObjectId commitObjectId = repository.resolve(commitId);
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(commitObjectId);
            RevCommit parent = revWalk.parseCommit(commit.getParent(0).getId());

            // Prepare the two iterators to compute the diff between
            try (ObjectReader reader = repository.newObjectReader()) {
                AbstractTreeIterator newTreeIter = new CanonicalTreeParser(null, reader, commit.getTree().getId());
                AbstractTreeIterator oldTreeIter = new CanonicalTreeParser(null, reader, parent.getTree().getId());

                // Finally get the list of changed files
                for (DiffEntry entry : getDiffEntries(repository, oldTreeIter, newTreeIter)) {
                    System.out.println("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        try (DiffFormatter diffFormatter = new DiffFormatter(out)) {
                            diffFormatter.setRepository(repository);
                            diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
                            diffFormatter.setDetectRenames(true);
                            diffFormatter.format(entry);
                            String diffText = out.toString();
                            System.out.println(diffText);

                            FileHeader fileHeader = diffFormatter.toFileHeader(entry);
                            List<HunkHeader> hunks = (List<HunkHeader>) fileHeader.getHunks();
                            for (HunkHeader hunk : hunks) {
                                EditList editList = hunk.toEditList();
                                for (Edit edit : editList) {
                                    System.out.println(edit);
                                    ObjectLoader oldLoader = repository.open(entry.getOldId().toObjectId());
                                    RawText oldText = new RawText(oldLoader.getBytes());
                                    ObjectLoader newLoader = repository.open(entry.getNewId().toObjectId());
                                    RawText newText = new RawText(newLoader.getBytes());
                                    String oldLines = oldText.getString(edit.getBeginA(), edit.getEndA(), false);
                                    String newLines = newText.getString(edit.getBeginB(), edit.getEndB(), false);
                                    System.out.println("Old lines: " + oldLines);
                                    System.out.println("New lines: " + newLines);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private List<DiffEntry> getDiffEntries(Repository repository, AbstractTreeIterator oldTreeIter, AbstractTreeIterator newTreeIter) throws Exception {
        try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            diffFormatter.setRepository(repository);
            diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
            diffFormatter.setDetectRenames(true);
            return diffFormatter.scan(oldTreeIter, newTreeIter);
        }
    }

    public void testgitservice(ArrayList<GithubDetail> repoList) throws Exception {
//        FileOutputStream logos = new FileOutputStream("D:/log-brpc-wr" + ".txt", true);
//        for (GithubDetail repo : repoList) {
//            RevWalk walk = gitService.createAllRevsWalk(repo.getRepo(), repo.getBranch());
//            //        Iterable<RevCommit> walk = gitService.createRevsWalkBetweenCommits(repo,"88db699b4d5935d8dcce4daf90b1aa2b28b2a48b","9ad9f45db59bd69a943a7c759859031da2051f8e");
//            //        Iterator<RevCommit> walk = gitService.createRevsWalkBetweenTags(repo,"")
//            int count = 0;
//            ArrayList<StaticWarning> wr_old = new ArrayList<>();
//            walk.setRevFilter(RevFilter.ALL); //后续发现遍历没有包含merge，查看这创建walk的源码他自己实现了过滤只能有一个父也就是没merge
//            for (RevCommit currentCommit : walk) {
//                System.out.println(currentCommit.getId().getName());
//                //            gitService.fileTreeDiff(repo, currentCommit, filePathsBefore, filePathsCurrent, renamedFilesHint);
//                gitService.checkout(repo.getRepo(), currentCommit.getId().getName());
//                //不用写太多文件，根据commitid再扫一次就有了GenerateCppcheckXML.report(projectpath,projectpath+"report"+currentCommit.getId().getName()+".xml",projectpath+"log"+currentCommit.getId().getName());
//                GenerateCppcheckXML.report(repo.getGithubName(), repo.getLocalTmpPath() + "report.xml", repo.getLocalTmpPath() + "log");
//                ArrayList<StaticWarning> wr = new ParserCppcheckWarning().parseXml(repo.getLocalTmpPath() + "report.xml",currentCommit.getId().getName(), currentCommit.getId().getName());
//                System.out.println("warning_nums:" + wr.size());
//                //            warningCppchekRepository.saveAll(wr);
//                //true表示在文件末尾追加
//
//    //                String log = GenerateCppcheckXML.compare(wr_old, wr);//找出新版本v2减少了哪些warning 对调参数就是找增加了哪些
//
//    //                logos.write(log.getBytes());
//                logos.write(System.getProperty("line.separator").getBytes());
//                logos.write(System.getProperty("line.separator").getBytes());
//                wr_old = wr;
//                count++;
//                //            if(count>1){break;}
//
//            }
//        }
//        logos.close();
}
}
