package edu.cqu.zhipengliu.entity;

import java.util.ArrayList;
import java.util.Date;
/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.entity
 * @className: CommitDetail
 * @author: Zhipengliu
 * @description: TODO 这个目前什么作用后面再用
 * @date: 2023/7/4 14:26
 * @version: 1.1
 */
public class CommitDetail {
    private ArrayList<WarningCppcheck> warningCppcheck;
    private ArrayList<WarningInfer> warningInfer; //可以两种警告列表都有
    private String commit_id;
    private String commit_msg;
    private Date commit_time;
    private String commit_author;
}
