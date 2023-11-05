package edu.cqu.zhipengliu.parser;

import edu.cqu.zhipengliu.entity.StaticWarning;
import org.dom4j.DocumentException;

import java.util.ArrayList;

/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.parser
 * @className: ParserInferWarning
 * @author: Zhipengliu
 * @description: 针对Infer输出报告的解析
 * @date: 2023/11/5 13:06
 * @version: 1.1
 */
public class ParserInferWarning implements ParserWarning {


    @Override
    public ArrayList<StaticWarning> parseXml(String xmlPath, String git_name, String commit_id) throws DocumentException {
        return null;
    }

    @Override
    public ArrayList<StaticWarning> parseJson(String jsonPath, String git_name, String commit_id) {
        return null;
    }
}
