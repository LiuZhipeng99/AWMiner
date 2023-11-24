package edu.cqu.zhipeng.entity;

/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.entity
 * @className: WarningCppcheck
 * @author: Zhipengliu
 * @description: 根据cppcheck产生的输出属性进行定义，参考xml文件在Parser里进行了属性解析
 * @date: 2023/9/4 10:26
 * @version: 1.1
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

//从产生的xml文件分析出
//<error id="ConfigurationNotChecked" severity="information" msg="Skipping configuration &apos;NGX_DEBUG;NGX_HAVE_INET6&apos; since the value of &apos;NGX_DEBUG&apos; is unknown." verbose="Skipping configuration &apos;NGX_DEBUG;NGX_HAVE_INET6&apos; since the value of &apos;NGX_DEBUG&apos; is unknown.">
//<location file="src/event/ngx_event.h" line="440" column="0"/>
//</error>
@AllArgsConstructor
@NoArgsConstructor
@Data
public class WarningCppcheck extends StaticWarning implements Serializable { // extends StaticWarning 不能像接口一样做多态
//    private final String tool_name = "cppcheck"; 这和父类重复了
    private String cppcheck_version;
    private String verbose;
    private String cwe;
//    private String symbol; //和location同级xml对象，目前看小于等于一个。 看到msg包含了symbol就舍弃
//    private ArrayList<Location> locations; //每个warningid对应多个location。 如果用String[]后续比较麻烦。  一个error标签视为一个Warning->> 一个location实例化一个Wr

//    private String file_path;
//    private String line_number; //cppcheck只有单行
//    private String column_number;
    private String info;





    // 拷贝函数(非拷贝构造函数)，用于深拷贝

}
