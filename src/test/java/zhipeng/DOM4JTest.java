package zhipeng;

import edu.cqu.zhipengliu.entity.WarningCppcheck;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


// 有四种方法解析xml，论文Identification用的jdom，这里用dom4j
public class DOM4JTest {
    @Test
    public void testdom4j(){
        ArrayList<WarningCppcheck> bookList = new ArrayList<WarningCppcheck>();
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(new File("D:\\IDEA-CODE\\SAWarningMiner\\src\\test\\java\\edu\\cqu\\zhipeng\\cppcheckreport-no-addon.xml"));
            Element warningStore = document.getRootElement();
            Iterator it = warningStore.elementIterator();
            // 遍历第一级标签有cppcheck/errors两个, 先把version提出了再遍历error
            Element versionel = (Element) it.next();
            String version = versionel.attributeValue("version");
            Iterator i = warningStore.elementIterator("cppcheck");
            System.out.println(warningStore.element("cppcheck"));
            Iterator errors_it = ((Element) it.next()).elementIterator();
            while (errors_it.hasNext()) {
                Element book = (Element) errors_it.next();
                WarningCppcheck bookData = new WarningCppcheck();
                List<Attribute> bookAttrs = book.attributes();
                for (Attribute attr : bookAttrs) {
                    bookData.setCppcheck_version(version);
                    if(attr.getName().equals("id")){
                        bookData.setId(attr.getValue());
                    }else if(attr.getName().equals("serverity")){
                        bookData.setSeverity(attr.getValue());
                    }else if(attr.getName().equals("verbose")){
                        bookData.setVerbose(attr.getValue());
                    }else if(attr.getName().equals("cwe")){
                        bookData.setCwe(attr.getValue());
                    }
                }
                Iterator itt = book.elementIterator();

                //这里dom层次是在遍历单个error下的location和symbol
                while (itt.hasNext()) {
                    Element bookChild = (Element) itt.next();
//                    System.out.println(bookChild.getName()+ "---" + bookChild.getText());
                    if(bookChild.getName().equals("location")){
                        List<Attribute> childattrs = bookChild.attributes();
                        for(Attribute attr: childattrs){
                            if(attr.getName().equals("file")){
                                bookData.setFile(attr.getValue());
                            }else if(attr.getName().equals("column")){
                                bookData.setColumn(attr.getValue());
                            }else if(attr.getName().equals("line")){
                                bookData.setLine(attr.getValue());
                            }
                        }
                    }else if(bookChild.getName().equals("symbol")){
                        bookData.setSymbol(bookChild.getText());
                    }
                }
                // 遍历完一个节点，将该节点信息添加到列表中
                bookList.add(bookData);
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        // 输出保存在内存中XML信息
//        for(CppcheckWarning book:bookList){
//            System.out.println(book.getId());
//            System.out.println("id=" + book.getId());
////            System.out.println(book.getAuthor());
////            System.out.println(book.getYear());
////            System.out.println(book.getPrice());
////            System.out.println(book.getLanguage());
//        }
        System.out.println(bookList.size()); // 检查xml中的error数量一致即可
    }
}
