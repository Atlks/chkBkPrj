package org.example;

import java.io.File;
import java.io.IOException;

//。。。
//cd
//cd src/chkBkPrj

public class rdNtfy {

    public static void main(String[] args) throws Exception {
        listFiles("/storage/emulated/0/Download/aNtfy");
        playMp3("/storage/emulated/0/Alarms/alm_lowBtry.mp3");
    }

    private static void playMp3(String f) throws  Exception {
        System.out.println("fun playMp3(f="+f);
//        ProcessBuilder pb =
//                new ProcessBuilder("mpv", f);
//        pb.start();
//        Process p =
//                new ProcessBuilder(
//                        "sh",
//                        "-c",
//                        "mpv /storage/emulated/0/Alarms/alm_lowBtry.mp3"
//                ).start();
//
//        System.out.println(p.waitFor());

        new ProcessBuilder(
                "mpv",
                "--no-terminal",
                f
        ).inheritIO()
                .start()
                .waitFor();
        System.out.println("endfun playMp3");
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
