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

import java.util.Objects;


//从产生的xml文件分析出
//<error id="ConfigurationNotChecked" severity="information" msg="Skipping configuration &apos;NGX_DEBUG;NGX_HAVE_INET6&apos; since the value of &apos;NGX_DEBUG&apos; is unknown." verbose="Skipping configuration &apos;NGX_DEBUG;NGX_HAVE_INET6&apos; since the value of &apos;NGX_DEBUG&apos; is unknown.">
//<location file="src/event/ngx_event.h" line="440" column="0"/>
//</error>
@AllArgsConstructor
@NoArgsConstructor
@Data
public class WarningCppcheck extends StaticWarning{ // extends StaticWarning 不能像接口一样做多态
    private final String tool_name = "cppcheck";
    private String cppcheck_version;

    private String verbose;
    private String cwe;
    private String symbol;
//    private WarningLocation location; //暂时每个warningid对应一个location，如果做成list不好分，对于想计算一个文件有多少bug可以考虑算法。
    private String file_path;
    private String line_number; //cppcheck只有单行
    private String column_number;
// 为了方便猜测不多做表都用基本类型，且一个location对应一个warning。
    @Override
    public boolean equals(Object obj){

        if(this == obj){return true;}
        if(obj == null || getClass() != obj.getClass()){
            return false;
        }
        WarningCppcheck other = (WarningCppcheck) obj;
    //        return Objects.equals(msg, other.getMsg()); 用==而不是equal出来了bug操
        return Objects.equals(bug_severity, other.getBug_severity()) && Objects.equals(this.warning_message, other.getWarning_message()) && Objects.equals(line_number, other.getLine_number()) && Objects.equals(column_number, other.getColumn_number());
    }
}
