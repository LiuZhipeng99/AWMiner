package edu.cqu.zhipengliu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.jgit.lib.Repository;

/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.entity
 * @className: GithubDetail
 * @author: Zhipengliu
 * @description: 因为分离了获取github集合和遍历每个项目commit两个功能，中间传递的数据不方便list和map组合着用，repo定义是本地git 这需要带着几个github属性
 * @date: 2023/11/4 22:51
 * @version: 1.1
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GithubDetail {
    private Repository repo;
//    private String[] values = {starCount, githubLink, githubName}; //can also use list 如果要组合成map将就着用可以，用类就不合适了
    private String starCount;
    private String githubLink;
    private String githubName;
    private String branch ;
    private String localTmpPath;
}
//其实c_repos_sorted描述的项目信息可以更多比如：爬取时date、项目描述、作者、语言类型等等。
//不知道有没有现成的数据库供查询就不需要文件描述了， 现成的commit遍历工具。
