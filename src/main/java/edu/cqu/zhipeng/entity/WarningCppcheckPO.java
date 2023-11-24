

//用JPA做数据库想法移除、将数据库和工具本身分离，产出文本比数据库更适合研究领域



// package edu.cqu.zhipengliu;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.util.Objects;
//
//@Entity
//@Table(name = "WarningCppcheck_dev2")
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class WarningCppcheckPO {
//
//// 能不能直接代替DO，这PO只多了个自然增长ID，应该可以当作正常类用
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id", nullable = false)
//    private int id_auto;
//
//    @Column(name = "cppcheck_version", nullable = false)
//    private String cppcheck_version;
//
//    @Column(name = "commit_id", nullable = false)
//    private String commit_id; //这可能 之后作为外键
//
//    @Column(name = "cppcheck_id", nullable = false)
//    private String id; //这个id不是唯一id，类似ConfigurationNotChecked
//
//    @Column(name = "cppcheck_severity", nullable = false)
//    private String severity;
//
//    @Column(name = "cppcheck_msg", nullable = false, length = 1000)
//    private String msg;
//
//    @Column(name = "cppcheck_verbose", length = 1000)
//    private String verbose;
//
//    @Column(name = "cppcheck_cwe")
//    private String cwe;
//
//    @Column(name = "cppcheck_symbol")
//    private String symbol;
//
//    @Column(name = "cppcheck_file")
//    private String file;
//    @Column(name = "cppcheck_line")
//    private String line; //cppcheck只有单行
//    @Column(name = "cppcheck_column")
//    private String  column;
//
//    @Override
//    public boolean equals(Object obj){
//
//        if(this == obj){return true;}
//        if(obj == null || getClass() != obj.getClass()){
//            return false;
//        }
//        WarningCppcheckPO other = (WarningCppcheckPO) obj;
////        return Objects.equals(msg, other.getMsg()); 用==而不是equal出来了bug操
//        return Objects.equals(severity, other.getSeverity()) && Objects.equals(msg, other.getMsg()) && Objects.equals(line, other.getLine()) && Objects.equals(column, other.getColumn());
//    }
//}
