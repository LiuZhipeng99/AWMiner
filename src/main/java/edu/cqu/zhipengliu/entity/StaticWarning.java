package edu.cqu.zhipengliu.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.entity
 * @className: StaticWarning
 * @author: Zhipengliu
 * @description: 警告的基本属性，各个工具产生有些特殊的属性。 这里参考标准（标准暂如下，只考虑了后续hash增量分析的需求）添加一些共有变量和方法
 * @date: 2023/7/4 14:26
 * @version: 1.1
 */
// 接口和虚基类区别https://www.cnblogs.com/aishangtaxuefeihong/p/6144005.html
@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class StaticWarning{
    String tool_name; //工具名字如cppcheck、infer
    String git_link; // +commit可以确定代码扫描的结果
    String commit_id; //这可能之后作为外键， 不能从报告中得到
    String commit_id_repair; // commit_id的下一个commit（需要用快慢指针了）
    String warning_message;
    String hash_id; //用于增量分析

    String bug_severity;
    String bug_type;
//    String file_name; 每个工具标识代码location的方法不太一样有的分开路径和文件名有的直接是path
}
