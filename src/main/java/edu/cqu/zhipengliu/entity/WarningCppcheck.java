package edu.cqu.zhipengliu.entity;

/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.entity
 * @className: WarningCppcheck
 * @author: Zhipengliu
 * @description: 根据cppcheck产生的输出属性进行定义
 * @date: 2023/9/4 10:26
 * @version: 1.1
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;


//从产生的xml文件分析出
//<error id="ConfigurationNotChecked" severity="information" msg="Skipping configuration &apos;NGX_DEBUG;NGX_HAVE_INET6&apos; since the value of &apos;NGX_DEBUG&apos; is unknown." verbose="Skipping configuration &apos;NGX_DEBUG;NGX_HAVE_INET6&apos; since the value of &apos;NGX_DEBUG&apos; is unknown.">
//<location file="src/event/ngx_event.h" line="440" column="0"/>
//</error>
@AllArgsConstructor
@NoArgsConstructor
@Data
public class WarningCppcheck extends StaticWarning implements Cloneable{ // extends StaticWarning 不能像接口一样做多态
    private final String tool_name = "cppcheck";
    private String cppcheck_version;

    private String verbose;
    private String cwe;
//    private String symbol; //和location同级xml对象，目前看小于等于一个。 看到msg包含了symbol就舍弃
//    private ArrayList<Location> locations; //每个warningid对应多个location。 如果用String[]后续比较麻烦。  一个error标签视为一个Warning->> 一个location实例化一个Wr

    private String file_path;
    private String line_number; //cppcheck只有单行
    private String column_number;
    private String info;




    @Override
    public boolean equals(Object obj){

        if(this == obj){return true;}
        if(obj == null || getClass() != obj.getClass()){
            return false;
        }
        WarningCppcheck other = (WarningCppcheck) obj;
    //        return Objects.equals(msg, other.getMsg()); 用==而不是equal出来了bug操
        return Objects.equals(this.hash_id, other.getHash_id()) ;
    }
    public String computeHash() {
        Pattern numPattern = Pattern.compile("\\(:\\)[0-9]+");
        Pattern qualifierPattern = Pattern.compile("(line |column |:|parameter |\\$)[0-9]+");

//        String baseFilename = Paths.get(file).getFileName().toString();
//        String hashableProcedureName = hashableName(procName);

//        String locationIndependentProcName = numPattern.matcher(procName).replaceAll("\\$_");
        String locationIndependentQualifier = qualifierPattern.matcher(this.warning_message).replaceAll("\\$_");

        String[] data = {this.bug_severity, this.bug_type, this.file_path, this.info,locationIndependentQualifier};

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
    // 拷贝函数(非拷贝构造函数)，用于深拷贝
    @Override
    public WarningCppcheck clone() {
        try {
            return (WarningCppcheck) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
