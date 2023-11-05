package edu.cqu.zhipengliu.analysis;

/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.analysis
 * @className: AnalysisCppcheck
 * @author: Zhipengliu
 * @description: 为cppcheck增加的报告的增量分析
 * @date: 2023/11/5 0:15
 * @version: 1.1
 */
public class AnalysisCppcheck {
    public String compute_hash(){
//        cppcheck --xml passcat\sqlite3.c 卡住了 一看有20W行、 虽然这只40个commit但很费时10min，针对此命令调用的时间可以考虑优化：压榨完一台服务器用j参数或者并发
//        本质上还是很低效如果考虑仅扫描GitHub diff patch应该是更优的但这可能存在那种修改删除函数 其他地方问题就消失了（没有做实证研究）
        return null;
    }
}
