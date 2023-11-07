package edu.cqu.zhipengliu.parser;

import edu.cqu.zhipengliu.entity.StaticWarning;
import org.dom4j.DocumentException;
import java.util.ArrayList;
/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.parser
 * @className: ParserWarning
 * @author: Zhipengliu
 * @description: 定义这些Parser要实现的共同方法，比如解析xml/json/行格式的报告
 * @date: 2023/11/5 13:06
 * @version: 1.1
 */
public interface ParserWarning {

    ArrayList<StaticWarning> parseXml(String xmlPath, String git_name, String commit_id)  throws DocumentException;
    ArrayList<StaticWarning> parseJson(String jsonPath, String git_name, String commit_id);

}
