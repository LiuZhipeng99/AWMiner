package edu.cqu.zhipengliu.parser;

/**
 * @projectName: SAWMiner
 * @package: edu.cqu.zhipengliu.parser
 * @className: CppcheckParser
 * @author: Zhipengliu
 * @description: TODO
 * @date: 2023/11/4 14:26
 * @version: 1.1
 */
import edu.cqu.zhipengliu.entity.WarningCppcheck;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//一个想法是复用CXX-sensor，就可以很快解析其他类型SA。另外其是否能借AST提取代码？



public class CppcheckParser implements WarningParser {

    public ArrayList<WarningCppcheck> parseWarningsXML(String xmlfilename, String commit_id) throws DocumentException {
        ArrayList<WarningCppcheck> warningList = new ArrayList<>();
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(xmlfilename));
        Element waringRoot = document.getRootElement();
        String cppcheck_version = waringRoot.element("cppcheck").attributeValue("version");
        for (Iterator i = waringRoot.element("errors").elementIterator(); i.hasNext();){ //waringRoot.element("errors").elementIterator() != waringRoot.elementIterator("error")
            Element error = (Element) i.next();
            WarningCppcheck wr = new WarningCppcheck();
            List<Attribute> error_attrs = error.attributes();
            for(Attribute attr: error_attrs){
                wr.setCppcheck_version(cppcheck_version);
                wr.setCommit_id(commit_id);
                if(attr.getName().equals("id")){
                    wr.setId(attr.getValue());
                }else if(attr.getName().equals("msg")){
                    wr.setMsg(attr.getValue());
                }else if(attr.getName().equals("severity")){
                    wr.setSeverity(attr.getValue());
                }else if(attr.getName().equals("verbose")){
                    wr.setVerbose(attr.getValue());
                }else if(attr.getName().equals("cwe")){
                    wr.setCwe(attr.getValue());
                }
            }
            //这里dom层次是在遍历单个error下的location和symbol
            for(Iterator itt = error.elementIterator(); itt.hasNext();){
                Element errorChild = (Element) itt.next();
                if(errorChild.getName().equals("location")){
//                    WarningLocation location = new WarningLocation();
                    List<Attribute> childattrs = errorChild.attributes();
                    for(Attribute attr: childattrs){
                        if(attr.getName().equals("file")){
                            wr.setFile(attr.getValue());
                        }else if(attr.getName().equals("column")){
                            wr.setColumn(attr.getValue());
                        }else if(attr.getName().equals("line")){
                            wr.setLine(attr.getValue());
                        }
                    }
                }else if(errorChild.getName().equals("symbol")){
                    wr.setSymbol(errorChild.getText());
                }
            }
            // 遍历完一个节点，将该节点信息添加到列表中
            warningList.add(wr);
        }
        return warningList;
    }


    // 遍历xml 提取其他信息，非warning列表
//    public ArrayList<WarningLocation> parseSpecificSite(String reginStr) {
//        return null;
//    }
}
