package edu.cqu.zhipengliu.entity;
/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.entity
 * @className: CommitWarningCppcheck
 * @author: Zhipengliu
 * @description: TODO
 * @date: 2023/7/4 14:26
 * @version: 1.1
 */
import java.util.ArrayList;
import java.util.Date;

public class CommitWarningCppcheck {
    private ArrayList<WarningCppcheck> warnings;
    private String cppcheck_version;
    private String commit_id;
    private String commit_msg;
    private Date commit_time;
}
