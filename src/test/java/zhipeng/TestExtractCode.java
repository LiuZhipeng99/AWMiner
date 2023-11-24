package zhipeng;

import edu.cqu.zhipeng.utils.GetBugTrace;
import org.junit.Test;

import java.io.*;

public class TestExtractCode {
    //线程数
    @Test
    public void testfun() throws IOException {
        int lineNUM = 47;
        String fileName = "src/test/java/zhipeng/hdr_histogram.c";
        File file = new File(fileName);
        if(file.exists()) {
            System.out.println("The file exists.");
        } else {
            System.out.println(System.getProperty("user.dir"));
        }

        System.out.println(GetBugTrace.getwarningtrace(fileName,lineNUM)); //只需要两个参数.
    }


    public static void main() throws FileNotFoundException {
        String project = "HdrHistogram_c";
//        String warning_json1 = "D:\\0Workspace\\IDEA-CODE\\SAWMiner\\GeneratedDataset\\ActionableWarning\\" + project + ".json";
//        String warning_json2 = "D:\\0Workspace\\IDEA-CODE\\SAWMiner\\GeneratedDataset\\NonActionableWarning\\" + project + ".json";
//        String output = "D:\\0Workspace\\IDEA-CODE\\SAWMiner\\GeneratedDataset\\.json";
//        System.out.println("tmp_github\\HdrHistogram_c\\src\\main\\c\\hdr_histogram.c".endsWith(".c"));

//        if (args.length >= 3) {
//            warning_json = args[0];
//            output = args[1];
//            NUM_THREADS= Integer.parseInt(args[2]);
//        }
//        else{
//            System.out.println("参数不够！");
//            return;
//        }

//        processConcurrently(warning_json1, output);

    }
//    public static String processConcurrently(String warning_json, String output) {
//        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
//        try {
//            Set<Error> concurrentErrors = ConcurrentHashMap.newKeySet();
//            JsonElement jsonElement = JsonParser.parseReader(new FileReader(warning_json));
//            JsonArray jsonArray = jsonElement.getAsJsonArray();
//
//            for (JsonElement element : jsonArray) {
////            Pattern pattern = Pattern.compile("^Rule.*");
//                JsonObject jsonObject = element.getAsJsonObject();
//                String commitId = String.valueOf(jsonObject.get("commit_id"));
//                ProcessBuilder processBuilder = new ProcessBuilder("git", "checkout", commitId);
//                processBuilder.directory(new File("D:\\0Workspace\\IDEA-CODE\\SAWMiner\\tmp_github\\HdrHistogram_c"));
//                try {
//                    Process process = processBuilder.start();
//                    int exitCode = process.waitFor();
//                    if (exitCode == 0) {
//                        System.out.println("Successfully checked out to commit: " + commitId);
//                    } else {
//                        System.out.println("Error occurred while checking out to commit: " + commitId);
//                    }
//                } catch (IOException | InterruptedException e) {
//                    e.printStackTrace();
//                }
//                String msg = String.valueOf(jsonObject.get("warning_message"));
////                Matcher matcher = pattern.matcher(msg); //可以匹配如果id不符合Misra直接返回
//                executor.submit(() -> {
//                    Error error = new Error();
//                    error.setMsg(msg);
//                    String fileAdr = String.valueOf(jsonObject.get("file_path"));
//                    error.setFilePath(fileAdr);
//                    Integer line = jsonObject.get("line_number").getAsInt();
//                    error.setLine(line);
////                    System.out.println(error);
////                    System.out.println(error.getFilePath());
//                    if (true){
//                        ExtractCodeBlockForC extractCodeBlock = new ExtractCodeBlockForC("..\\SAWMiner\\tmp_github\\HdrHistogram_c\\src\\main\\c\\hdr_histogram.c");
//                        String extractCodeBlockForC = extractCodeBlock.getCodeBlockByAst(error.getLine());
//                        if ("200".equals(extractCodeBlockForC))
//                        {
//                            String code = extractCodeBlock.getCode();
//                            Integer startLine = extractCodeBlock.getStartLine();
//                            Integer endLine = extractCodeBlock.getEndLine();
//                            error.setContext(code);
//                            error.setBeginLine(startLine);
//                            error.setEndLine(endLine);
//
//                        }else {
//                            try {
//                                String code =getAcode(fileAdr,line);
//                                error.setContext(code);
//                                error.setBeginLine(line);
//                                error.setEndLine(line);
//                            } catch (IOException e) {
//                                throw new RuntimeException(e);
//                            }
//                        }
//
//                    }else if (isCppFile(error.getFilePath()))
//                    {
//                        ExtractCodeBlockForCpp extractCodeBlock = new ExtractCodeBlockForCpp(error.getFilePath());
//                        String existCodeBlockByAst = extractCodeBlock.getCodeBlockByAst(error.getLine());
//                        if ("200".equals(existCodeBlockByAst))
//                        {
//
//                            String code = extractCodeBlock.getCode();
//                            Integer startLine = extractCodeBlock.getStartLine();
//                            Integer endLine = extractCodeBlock.getEndLine();
//                            error.setContext(code);
//                            error.setBeginLine(startLine);
//                            error.setEndLine(endLine);
//                        }else {
//
//                            try {
//                                String code =getAcode(fileAdr,line);
//                                error.setContext(code);
//                                error.setBeginLine(line);
//                                error.setEndLine(line);
//                            } catch (IOException e) {
//                                throw new RuntimeException(e);
//                            }
//                        }
//                    }
//
//
//                    concurrentErrors.add(error);
//                });
//
//            }
//
//
//            executor.shutdown();
//            while (!executor.isTerminated()) {
//                // 等待所有线程完成
//            }
//            System.out.println("concurrentErrors");
//            System.out.println(concurrentErrors);
//            return concurrentErrors.toString();
////            String jsonString = JSON.toJSONString(concurrentErrors);
////            try (PrintWriter printWriter = new PrintWriter(output)) {
////                printWriter.println(jsonString);
////            } catch (FileNotFoundException e) {
////                throw new RuntimeException(e);
////            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    public static List<Error> getErrorList(String path)  {
//        // 获取文件的输入流对象
//        FileInputStream fileInputStream = null;
//        try {
//            fileInputStream = new FileInputStream(path);
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        int len;
//        byte[] bytes = new byte[1024];
//        StringBuilder stringBuffer = new StringBuilder();
//        while (true) {
//            try {
//                if (!((len = fileInputStream.read(bytes)) != -1)) break;
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            // 添加字符串到缓冲区
//            stringBuffer.append(new String(bytes, 0, len));
//        }
//        // 关闭资源
//        try {
//            fileInputStream.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        // 使用fastjson将字符串转换为JSON
//        List<Error> errors = JSONObject.parseArray(stringBuffer.toString(), Error.class);
//
//        return errors;
//
//    }




}
