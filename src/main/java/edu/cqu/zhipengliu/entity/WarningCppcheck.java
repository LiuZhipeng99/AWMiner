package edu.cqu.zhipengliu.entity;

/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.entity
 * @className: WarningCppcheck
 * @author: Zhipengliu
 * @description: TODO
 * @date: 2023/9/4 10:26
 * @version: 1.1
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;


//从产生的xml文件分析出
//<error id="ConfigurationNotChecked" severity="information" msg="Skipping configuration &apos;NGX_DEBUG;NGX_HAVE_INET6&apos; since the value of &apos;NGX_DEBUG&apos; is unknown." verbose="Skipping configuration &apos;NGX_DEBUG;NGX_HAVE_INET6&apos; since the value of &apos;NGX_DEBUG&apos; is unknown.">
//<location file="src/event/ngx_event.h" line="440" column="0"/>
//</error>
@AllArgsConstructor
@NoArgsConstructor
@Data
public class WarningCppcheck { // extends StaticWarning 不能像接口一样做多态
    private String cppcheck_version;
    private String commit_id; //这可能 之后作为外键
    private String id; //这个id不是唯一id，类似ConfigurationNotChecked
    private String severity;
    private String msg;
    private String verbose;
    private String cwe;
    private String symbol;

//    private WarningLocation location; //暂时每个warningid对应一个location，如果做成list不好分，对于想计算一个文件有多少bug可以考虑算法。
    private String file;
    private String line; //cppcheck只有单行
    private String  column;
// 为了方便猜测不多做表都用基本类型，且一个location对应一个warning。
    @Override
    public boolean equals(Object obj){

        if(this == obj){return true;}
        if(obj == null || getClass() != obj.getClass()){
            return false;
        }
        WarningCppcheck other = (WarningCppcheck) obj;
//        return Objects.equals(msg, other.getMsg()); 用==而不是equal出来了bug操
        return Objects.equals(severity, other.getSeverity()) && Objects.equals(msg, other.getMsg()) && Objects.equals(line, other.getLine()) && Objects.equals(column, other.getColumn());
    }
}
