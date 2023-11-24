package edu.cqu.zhipeng.entity;

import java.util.ArrayList;
import java.util.Date;
/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.entity
 * @className: CommitDetail
 * @author: Zhipengliu
 * @description: TODO 这个目前没什么作用，可以考虑降低结构耦合性，但初步开发先全放StaticWarning和其子类里。
 * @date: 2023/7/4 14:26
 * @version: 1.1
 */
public class CommitDetail {
    private ArrayList<StaticWarning> warningInfer; //可以两种警告列表都有
    private String commit_id;
    private String commit_msg;
    private Date commit_time;
    private String commit_author;
}
