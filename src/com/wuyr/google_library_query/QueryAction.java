package com.wuyr.google_library_query;

/**
 * @author wuyr
 * @github https://github.com/wuyr/GoogleLibraryVersionQuerier
 * @since 2020-05-04 15:11
 */

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.JsonObject;

/**
 * Java版本代码
 */
//public class QueryAction extends AnAction {
//
//    private static final String BASE_URL = "https://wanandroid.com/maven_pom/index?k=";
//
//    @Override
//    public void actionPerformed(@NotNull AnActionEvent event) {
//        Editor editor = event.getData(CommonDataKeys.EDITOR);
//
//        if (editor != null) {
//            com.intellij.openapi.editor.Document document = editor.getDocument();
//            CaretModel caretModel = editor.getCaretModel();
//
//            int lineStart = caretModel.getVisualLineStart();
//            int lineEnd = caretModel.getVisualLineEnd();
//            String currentLine = document.getText(new TextRange(lineStart, lineEnd));
//            String semicolon = ":";
//            if (currentLine.contains(semicolon)) {
//                int startIndex, endIndex;
//                String singleQuotation = "'";
//                String doubleQuotation = "\"";
//                boolean isSingleQuotation = currentLine.contains(singleQuotation);
//                if (isSingleQuotation) {
//                    startIndex = currentLine.indexOf(singleQuotation);
//                    endIndex = currentLine.lastIndexOf(singleQuotation);
//                } else {
//                    startIndex = currentLine.indexOf(doubleQuotation);
//                    endIndex = currentLine.lastIndexOf(doubleQuotation);
//                }
//                if (startIndex >= 0 && endIndex > 0) {
//                    String[] libraryInfo = currentLine.substring(startIndex + 1, endIndex).split(semicolon);
//                    String libraryGroup = libraryInfo[0];
//                    String libraryName = libraryInfo[1];
//                    System.out.println(libraryGroup);
//                    System.out.println(libraryName);
//
//                    String latestVersion = null;
//                    try {
//                        latestVersion = getLatestVersionByJsoup(libraryGroup, libraryName);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    if (latestVersion == null) {
//                        Messages.showErrorDialog("Please check if the library belongs to Google.", "Library Not Found!");
//                    } else {
//
//                        System.out.println(latestVersion);
//
//                        int oldVersionStart = currentLine.lastIndexOf(semicolon);
//                        int oldVersionEnd = currentLine.lastIndexOf(isSingleQuotation ? singleQuotation : doubleQuotation);
//                        String oldVersion = currentLine.substring(oldVersionStart, oldVersionEnd);
//
//                        System.out.println("Old: " + currentLine);
//                        currentLine = currentLine.replace(oldVersion, semicolon + latestVersion);
//                        System.out.println("New: " + currentLine);
//
//                        if (Messages.showOkCancelDialog(String.format(Locale.getDefault(),
//                                "The latest version is:\r\n%s\r\nDo you want to replace it?", latestVersion),
//                                "Library Found", "OK", "&No", Messages.getQuestionIcon()) == Messages.OK) {
//                            String finalCurrentLine = currentLine;
//                            WriteCommandAction.runWriteCommandAction(event.getProject(), () ->
//                                    document.replaceString(lineStart, lineEnd, finalCurrentLine));
//                        }
//                    }
//
//                }
//            }
////            System.out.println(currentLine);
//        }
//    }
//
//    public static void main(String[] args) throws Exception {
//        String libraryGroup = "androidx.asynclayoutinflater";
//        String libraryName = "asynclayoutinflater";
////        String libraryGroup = "androidx.cardview";
////        String libraryName = "cardview";
//        String latestVersion = getLatestVersionByJsoup(libraryGroup, libraryName);
//        System.out.println(latestVersion);
//    }
//
//    private static String getLatestVersionByJsoup(String libraryGroup, String libraryName) throws Exception {
//        String latestVersion = null;
//        Document document = Jsoup.connect(BASE_URL + libraryGroup).get();
//        Elements elements = document.select("li[class=pom_item]")
//                .get(0).children().get(1).children().get(1).children();
//
//        boolean libraryFound = false;
//        for (Element e : elements) {
//            Elements children = e.children();
//            if (children.size() == 3) {
//                if (libraryFound) {
//                    break;
//                }
//                if (children.get(0).text().equals(libraryName)) {
//                    libraryFound = true;
//                }
//            }
//            if (libraryFound) {
//                latestVersion = children.get(children.size() - 2).text();
//            }
//        }
//        return latestVersion;
//    }
//
//}