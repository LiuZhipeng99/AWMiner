package edu.cqu.zhipeng;



import edu.cqu.zhipeng.entity.GithubDetail;
import edu.cqu.zhipeng.entity.StaticWarning;
import edu.cqu.zhipeng.parser.ParserCppcheckWarning;
import edu.cqu.zhipeng.utils.CloneUtil;
import edu.cqu.zhipeng.utils.FileUtils;
import edu.cqu.zhipeng.report.GenerateCppcheckXML;
import edu.cqu.zhipeng.utils.GitUtils;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

import static edu.cqu.zhipeng.utils.FileUtils.tempDir;


/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.utils
 * @className: GithubTraverser
 * @author: Zhipengliu
 * @description: c_repos_sorted.txt中描述的GitHub项目实例化为GithubDetail，遍历其commit，生成报告
 * @date: 2023/11/4 15:15
 * @version: 1.1
 */


public class GithubTraverser {
    //只用到了clone和遍历commit的功能现在自己造轮子
//    GitServiceImpl gitService = new GitServiceImpl(); //这里用多态特性实例化GitService，也没什么意义不如直接实例化，主要是也没其他gitservice实现

    Logger logger = LoggerFactory.getLogger(GithubTraverser.class);
    Map<String, ArrayList<StaticWarning>> map = new HashMap<>(); //作为缓存至少减少了一半的扫描时间。从原来的快慢指针变为pair遍历 report次数会翻倍，通过保留xml减少次数面临存储和同步问题。想到用以commit构建set，减少扫描次数。
    public void commitTraverserThread(GithubDetail repo) throws Exception {
        String repoName = repo.getGithubName();
        GitUtils.checkout(repo.getRepo(),repo.getBranch());
        Logger logger = ThreadSpecificLoggerFactory.getLogger();
        ArrayList<StaticWarning> fixed_all = new ArrayList<>();
        ArrayList<StaticWarning> introduced_all = new ArrayList<>();

        // Step1: 对此GitHub仓库生成commit-pair序列
        ArrayList<String[]> commitPairList = GitUtils.createCommitPairList(repo.getRepo(), repo.getBranch());
        long startTime = System.currentTimeMillis();
        logger.warn("######### "+repoName+"  Scanning (0/"+commitPairList.size()+") #########");
        try {
            int count = 0;
            for (String[] commit_pair : commitPairList) {  //从最新commit到最早的 ,
//                logger.warn(String.valueOf(count));
                if (count++ > 8 && count % (commitPairList.size() / 8) == 0)
                    logger.warn(repoName + ": Scanning progress " + count + "/" + commitPairList.size()); //打印十8次文本形式进度条
                String commitTitle = commit_pair[0];
                String currentCommitId = commit_pair[1];
                String nextCommitId = commit_pair[2];
                // Step2: 对两个版本分别SPA和解析
                //v1
                long commitStart = System.currentTimeMillis();
                GitUtils.checkout(repo.getRepo(), currentCommitId);
                ArrayList<StaticWarning> cur_wr = SPA(repo, currentCommitId);
                // 预估时间是否超过12小时（12小时 = 12 * 60 * 60 * 1000 毫秒）
                if((System.currentTimeMillis() - startTime) * commitPairList.size() > 3 * 60 * 60 * 1000){
                    break;
                }
                //v2
                GitUtils.checkout(repo.getRepo(), nextCommitId);
                ArrayList<StaticWarning> next_wr = SPA(repo, nextCommitId);

                // Step3：差量分析
                ArrayList<StaticWarning> fixed = getDiff(cur_wr, next_wr); // cur - next //考虑下next为空的初始状态含义
                ArrayList<StaticWarning> introduced = getDiff(next_wr, cur_wr); // next - cur 暂时粗略划分为false类型/不像定义修复那样准确
                ArrayList<StaticWarning> fixedFilter = new ArrayList<>();
                //这里进行过滤，考虑删除文件很新增文件的影响，删除文件导致的fix可以过滤掉；新增文件导致的intro策略是在后面removeAll保留那些新增文件引入且后续没删除的情况。这里有个问题是初始化引入的没有加进去，最新版本引入但没来得及修复的加进去了
                for (StaticWarning warning : fixed) {
                    File file = new File(warning.getFilePath()); // 刚好目前是child分支不需要check
                    if (file.exists()) {
//                        System.out.println("The file exists.");
                        fixedFilter.add(warning);
                    }
                }
                for (StaticWarning warning : fixedFilter) { //给warning增加除警告attr以外的信息
                    String warningFilePathfileName = warning.getFilePath();
                    warning.setCommitTitle(commitTitle);
                    warning.setCommitChildId(nextCommitId);
                    warning.setCommitId(currentCommitId);
                    warning.setGithubCommitLink(repo.getGithubLink()+"/commit/"+nextCommitId);
                    warning.setGitDiffText(GitUtils.getGitDiff(repo.getRepo(), nextCommitId));
                    int repoIndex = warningFilePathfileName.indexOf(repo.getGithubName());
                    if (repoIndex != -1) {
                        String filepath = warningFilePathfileName.substring(repoIndex + repo.getGithubName().length()).replace("\\","/");
                        String link = repo.getGithubLink() + "/blob/" + currentCommitId + filepath + "#L" + warning.getLineNumber();
                        warning.setWarningTraceLink(link);
                    }
//查看GetBugTrace分析为什么不在这里获得上下文 warning.setWarningtracecontext(GetBugTrace.getwarningtrace(warning.getFile_path(), Integer.parseInt(warning.getLine_number())));
                }
                for (StaticWarning warning : introduced) { //给warning增加除警告attr以外的信息\
                    String warningFilePathfileName = warning.getFilePath();
                    warning.setCommitTitle(commitTitle);
                    warning.setCommitChildId(nextCommitId);
                    warning.setCommitId(currentCommitId);
                    warning.setGithubCommitLink(repo.getGithubLink()+"/commit/"+nextCommitId);
                    warning.setGitDiffText(GitUtils.getGitDiff(repo.getRepo(), nextCommitId));
                    int repoIndex = warningFilePathfileName.indexOf(repo.getGithubName());
                    if (repoIndex != -1) {
                        String filepath = warningFilePathfileName.substring(repoIndex + repo.getGithubName().length()).replace("\\","/");
                        String link = repo.getGithubLink() + "/blob/" + currentCommitId + filepath + "#L" + warning.getLineNumber();
                        warning.setWarningTraceLink(link);
                    }
                }
                fixed_all.addAll(fixedFilter);
                introduced_all.addAll(introduced);
            }
        }catch (DocumentException e){
            logger.error("Check "+tempDir + repoName + "/cppcheck_log"+", can't parse report."); // 不会中断
        }catch (NullPointerException e){
            logger.error("Check "+tempDir + repoName+"/, can't git checkout "+ repo.getBranch()+". (maybe no. git directory or commit.");
            e.printStackTrace();

        }

//        System.out.println(introduced_all.size());
        introduced_all.removeAll(fixed_all); // 这里intro - fixed 操作：考虑到某个bug在未来被fix了fixed会记录但introduced_all仍然保留着。
        if (!fixed_all.isEmpty() || !introduced_all.isEmpty()) { //是否保存的条件，同时前面会出现解析异常和git空指针的异常 此处选择全部认为
            FileUtils.save(repoName, introduced_all, fixed_all);
            logger.warn(repoName + " generate fixed: " + fixed_all.size()+" introduced: " + introduced_all.size());
        }else  logger.warn(repoName + " have no fixed or introduced warning.");

        logger.warn(repoName + " runs for "+ (System.currentTimeMillis() - startTime)/1000 + " seconds.\n\n\n");
//        System.out.println(map.size());
//        System.out.println(map.get("5dcf11c98273eb7f329bd4fc5cd37ddccf90da68"));
    }

    public void commitTraverser(ArrayList<GithubDetail> repoList, int numThread) {
        ExecutorService executor = Executors.newFixedThreadPool(numThread); //因为cppcheck命令总是用并发，但并发是单独扫描上下限差异大会有单个线程等很久的cppcheck（超时一定程度避免），这里并发让同时扫描等待长的个数增长到Thread个
        List<Future<?>> futures = new ArrayList<>();

        for (GithubDetail repo : repoList) {
            Future<?> future = executor.submit(() -> {
                try {
                    commitTraverserThread(repo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            futures.add(future);
        }
//        设置扫描每个repo的超时逻辑 ， 首先每个repo能忍受的时间能上百小时
//        总体超时时间，但流程是计时-cppcheck-结束，应该继续优化检测cppcheck不能太长。 目前限制了单个commit中cppcheck的超时
//        这里也没什么用了。或者限制单个commit耗时*commit数量的预估时间，都比这里超时更节约时间。
//        long timeout = 60 * 60 * 720 ; // 超时时间（单位：秒）
//        for (Future<?> future : futures) {
//            try {
//                future.get(timeout, TimeUnit.SECONDS); //通过commit数量进行预估
//            } catch (TimeoutException e) {
//                // 任务超时处理逻辑
//                future.cancel(true); // 取消任务
//                // 其他处理代码...
//            } catch (Exception e) {
//                // 其他异常处理逻辑
//            }
//        }
        executor.shutdown(); // 等待线程池结束，shutdownNow才是立即关闭
    }

    public ArrayList<StaticWarning> SPA(GithubDetail repo, String commitId) throws Exception {
        // you can use other parser and generate report.
        ParserCppcheckWarning parser1 = new ParserCppcheckWarning();
        String xmlOutputPath = tempDir + repo.getGithubName() + "/cppcheck_report.xml";
        String logPath = tempDir + repo.getGithubName() + "/cppcheck_log";
        ArrayList<StaticWarning> warningArrayList = new ArrayList<>();
        ArrayList<StaticWarning> warningArrayListCopy = new ArrayList<>();
        if (!map.containsKey(commitId)) {
            GenerateCppcheckXML.report(repo.getLocalTmpPath(), xmlOutputPath, logPath);
            warningArrayList = parser1.parseXml(xmlOutputPath);
            map.put(commitId, warningArrayList);
        }else {
            warningArrayList = (map.get(commitId)); //解决忽视了修改引起 更高一层对象内容变化.
//            System.out.println(tmp == warningArrayList);
        }
        for (StaticWarning originalWarning : warningArrayList) {
            warningArrayListCopy.add(CloneUtil.clone(originalWarning));
        }
//        if(Objects.equals(commitId, "b619f348e840a42d935c4dfacbf852faff3653c9")){
//            System.out.println(warningArrayList);
//        } //深拷贝问题debug了半天. 然后解决也解决了半天，这里必须用for复制到一个新的list上
        return warningArrayListCopy;
    }
    //    public void diffReport(ArrayList<StaticWarning> pre, ArrayList<StaticWarning> cur){}
    public ArrayList<StaticWarning> getDiff(ArrayList<StaticWarning> reportA, ArrayList<StaticWarning> reportB){
        // 查找消失/新增的元素 （A-B）
//        if(reportB == null ) return new ArrayList<>(); //如果B为空且发生在最新的commit，B不应该视为最新的commit下一个版本所有warning移除
//
//        }
//        if(reportA==null) return new ArrayList<>(); //如果A为空且发生在最新的commit，可将当前警告列表B认为全部是引入的警告||return reportB; 如果返回B而不是空 会导致引入警告中有重复项这是为什么

        // 由于从快慢指针变为commitpair 不考虑父子关系 空的report也按照A-B定义返回（不处理报空指针错）
        if(reportA==null) return new ArrayList<>();
        if(reportB==null) return reportA;
        ArrayList<StaticWarning> diffEle = new ArrayList<>();
        for (StaticWarning warning : reportA) {
            if (!reportB.contains(warning)) {
                diffEle.add(warning);
            }
        }
        return diffEle;
    }
}
