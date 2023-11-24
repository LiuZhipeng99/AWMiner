package edu.cqu.zhipeng.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.entity
 * @className: StaticWarning
 * @author: Zhipengliu
 * @description: 警告的基本属性，各个工具产生有些特殊的属性。 这里参考标准（标准暂如下，只考虑了后续hash增量分析的需求）添加一些共有变量和方法
 * 优化可以考虑做一些对象嵌套, 同时定义为Abstract类
 * @date: 2023/7/4 14:26
 * @version: 1.1
 */
// 接口和虚基类区别https://www.cnblogs.com/aishangtaxuefeihong/p/6144005.html
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StaticWarning implements Serializable {  //这里定义了警告必要信息（可以都放在子类里，在这里面方便标准化，但不利于分离。）和警告相关的信息
    String hashId; //用于增量分析
    String toolName; //工具名字如cppcheck、infer
    String warningMessage;
    String warningSeverity;
    String warningType;
    String filePath; //每个工具标识代码location的方法不太一样有的分开路径和文件名有的直接是path
    String lineNumber;
    String columnNumber;

    String commitId; // 这两个id作为pair对是关键，是挖掘的核心。
    String commitChildId; // （最开始是一次遍历所有commit用快慢指针，时间代价在调用扫描上所以先获得个commit-pair列表，不仅可控而且能做更多操作比如两次commit差量文件单独扫描。
    String commitTitle;
    String githubCommitLink;
    String warningTraceLink; // 直接拼个源文件行号链接出来, 在这里和committitle一样是冗余信息就算再加上before/after都是，通过本地checkout和GitHub直接提取。但通过AST等操作还有的说。

    String gitDiffText;
    String warningTraceContext;
//    String fix_before;
//    String fix_after;
    // 这里的代码信息和git相关，用JGit库比较方便如果用爬虫还得处理下



    @Override
    public boolean equals(Object obj){

        if(this == obj){return true;}
        if(obj == null || getClass() != obj.getClass()){
            return false;
        }
        WarningCppcheck other = (WarningCppcheck) obj;
//      AWR paper
//        return Objects.equals(this.hash_id, other.getHash_id()) && sim(warning_message,other.warning_message) ;
        return Objects.equals(this.hashId, other.getHashId()) ; // 后续发现没有考虑移除文件导致的警告修复（后续可以结合commit标题和diff是否有remove/rename进行判断）。 如果在这简单的排除filepath容易使一个警告和其他文件相同msg的警告有相同特征，即使这个警告被修复，仍然被认为还在。
    }
    public String computeHash() {
        Pattern numPattern = Pattern.compile("\\(:\\)[0-9]+");
        Pattern qualifierPattern = Pattern.compile("(line |column |:|parameter |\\$)[0-9]+");

//        String baseFilename = Paths.get(file).getFileName().toString();
//        String hashableProcedureName = hashableName(procName);

//        String locationIndependentProcName = numPattern.matcher(procName).replaceAll("\\$_");
        String locationIndependentQualifier = qualifierPattern.matcher(this.warningMessage).replaceAll("\\$_");

        String[] data = {this.warningSeverity, this.warningType, this.filePath,locationIndependentQualifier};

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(String.join(",", data).getBytes(StandardCharsets.UTF_8));
            StringBuilder hashValue = new StringBuilder();
            for (byte b : hashBytes) {
                hashValue.append(String.format("%02x", b));
            }
            return hashValue.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
