package org.example;

import java.io.File;
//。。。
public class rdNtfy {

    public static void main(String[] args) {
        listFiles("~/storage/shared/Download/aNtfy");
    }

    //列出文件
    private static void listFiles(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            System.out.println("目录不存在: " + dir);
            return;
        }

        if (file.isFile()) {
            System.out.println(file.getAbsolutePath());
            return;
        }

        File[] files = file.listFiles();
        if (files == null) {
            return;
        }

        for (File f : files) {
            if (f.isDirectory()) {
                listFiles(f.getAbsolutePath());
            } else {
                System.out.println(f.getAbsolutePath());
            }
        } }
}
