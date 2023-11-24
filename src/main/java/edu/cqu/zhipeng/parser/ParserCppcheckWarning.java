package edu.cqu.zhipeng.parser;


import edu.cqu.zhipeng.entity.StaticWarning;
import edu.cqu.zhipeng.entity.WarningCppcheck;
import edu.cqu.zhipeng.utils.CloneUtil;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.parser
 * @className: ParserCppcheckWarning
 * @author: Zhipengliu
 * @description: 针对cppcheck输出报告的解析，目前是2.12版本。目前仅实现对cppcheck已格式化xml的输出，虽然原版还没有json输出但可以考虑用template参数输出json或行格式，用gson库或字符串分割来得到Warning对象
 * 解析代码
 * @date: 2023/11/4 14:26
 * @version: 1.1
 */
//一个想法是复用CXX-sensor，就可以很快解析其他类型SA。另外其是否能借AST提取代码？

public class ParserCppcheckWarning implements ParserWarning {
    @Override
    public ArrayList<StaticWarning> parseXml(String xmlPath) throws DocumentException {
        ArrayList<StaticWarning> warningList = new ArrayList<>();
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(xmlPath));
        Element waringRoot = document.getRootElement();
        String cppcheck_version = waringRoot.element("cppcheck").attributeValue("version");
        for (Iterator i = waringRoot.element("errors").elementIterator(); i.hasNext();){ //waringRoot.element("errors").elementIterator() != waringRoot.elementIterator("error")
            Element error = (Element) i.next();
            WarningCppcheck wr = new WarningCppcheck();
            List<Attribute> error_attrs = error.attributes();
            for(Attribute attr: error_attrs){
                wr.setToolName("cppcheck");
                wr.setCppcheck_version(cppcheck_version);
                if(attr.getName().equals("id")){
                    wr.setWarningType(attr.getValue());
                }else if(attr.getName().equals("msg")){
                    wr.setWarningMessage(attr.getValue());
                }else if(attr.getName().equals("severity")){
                    wr.setWarningSeverity(attr.getValue());
                }else if(attr.getName().equals("verbose")){
                    wr.setVerbose(attr.getValue());
                }else if(attr.getName().equals("cwe")){
                    wr.setCwe(attr.getValue());
                }
            }
            // Traverse the location and symbol under a single error.
            for(Iterator itt = error.elementIterator(); itt.hasNext();){
                Element errorChild = (Element) itt.next();
                if(errorChild.getName().equals("location")){ //not use symbol
//                    WarningLocation location = new WarningLocation();
                    List<Attribute> childattrs = errorChild.attributes();
                    for(Attribute attr: childattrs){
                        if(attr.getName().equals("file")){
                            wr.setFilePath(attr.getValue());
                        }else if(attr.getName().equals("column")){
                            wr.setColumnNumber(attr.getValue());
                        }else if(attr.getName().equals("line")){
                            wr.setLineNumber(attr.getValue());
                        }else if(attr.getName().equals("info")){
                            wr.setInfo(attr.getValue());
                        }
                    }
                    wr.setHashId(wr.computeHash());
                    warningList.add(CloneUtil.clone(wr));
//                    warningList.add(wr); //一边add一边遍历location。 这里有个bug：当list更新后add，之前加入的list也会同时改变，应该用个深拷贝
                }
            }
        }
        return warningList;
    }

    @Override
    public ArrayList<StaticWarning> parseJson(String jsonPath) {
        return null;
    }


//     遍历xml 提取其他信息，非warning列表
//    public ArrayList<WarningLocation> parseSpecificSite(String reginStr) {
//        return null;
//    }
}