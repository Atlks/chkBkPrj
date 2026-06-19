package chkbkPKg;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class S3FileCheck {

    public static void main(String[] args) throws Exception {

        // 1️⃣ 读取配置
        Map<String, String> conf = loadIni("/cfg/bkchk_key.ini");

        String accessKey = conf.get("CONFIG_S3_ACCESS_KEY_ID");
        String secretKey = conf.get("CONFIG_S3_SECRET_ACCESS_KEY").replace("\"", "");
        String region = conf.get("CONFIG_S3_REGION");

        // 2️⃣ 创建 S3 client
        S3Client s3 = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();

        // 3️⃣ 解析 S3 path
        String s3Path = "s3://amzn-s3-dbbackup/elasticsearch-snapshots/index.latest";
        String bucket = parseBucket(s3Path);
        String key = parseKey(s3Path);
        String bucket1 = "amzn-s3-dbbackup";


        String iso = getBktime4es(bucket, key, s3);
        System.out.println("=============esbk: " + iso);
//      //  System.out.println(iso);


        String prefix = "wltPrj_Aws_MysqlBk/";

        System.out.println("==============wltBk,"+getBktime4wlt(bucket1,prefix,s3));
        System.out.println("==============dcPgBk,"+getWlgBktimeLast(bucket1,"dc3tx/dtssbk/basebackups_005/",s3));


        System.out.println("==============dcDrsbk,"+getDrsBktimeLast(bucket1,"dc3drs/datacenter/__palo_repository_s3_repo/",s3));



    }

    private static String getDrsBktimeLast(String bucket, String prefix, S3Client s3) {

        // 1️⃣ 拉取所有对象（分页处理）
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build();


        //获取最后一个子文件夹

//        List<String> folders = s3.listObjectsV2Paginator(request)
//                .stream()
//                .flatMap(r -> r.contents().stream())
//                .map(S3Object::key)
//                .filter(key -> key.endsWith("UTCp8/"))
//                .toList();
//       //返回此子文件名称
//        return folders.isEmpty() ? null : folders.getLast();
       // String prefix = "dc3drs/datacenter/__palo_repository_s3_repo/";

        List<String> names = s3.listObjectsV2(ListObjectsV2Request.builder()
                        .bucket(bucket)
                        .prefix(prefix)
                        .delimiter("/")
                        .build())
                .commonPrefixes()
                .stream()
                .map(CommonPrefix::prefix)
                .map(p -> p.substring(prefix.length(), p.length() - 1)) // 去掉前缀 + 最后 /
                .toList();


            return  names.get(names.size()-1);

    }


    private static String getWlgBktimeLast( String bucket,String prefix, S3Client s3) {
        // 1️⃣ 拉取所有对象（分页处理）
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build();




        S3Object latest = s3.listObjectsV2Paginator(request).stream()
                .flatMap(r -> r.contents().stream())
                .filter(o -> o.key().endsWith(".json"))
                .max(Comparator.comparing(S3Object::lastModified))
                .orElse(null);

        // 3️⃣ 输出结果
//        System.out.println("Latest file:");
         System.out.println("Key: " + latest.key());
//        System.out.println("LastModified: " + latest.lastModified());
        return  toTimeIsoFmt(latest.lastModified());
    }


    private static String getBktime4wlt( String bucket,String prefix, S3Client s3) {
        // 1️⃣ 拉取所有对象（分页处理）
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build();
//
//        ListObjectsV2Response response = s3.listObjectsV2(request);
//
//        List<S3Object> gzFiles = response.contents().stream()
//                .filter(obj -> obj.key().endsWith(".gz"))
//                .collect(Collectors.toList());
//
//        if (gzFiles.isEmpty()) {
//            System.out.println("No .gz files found");
//            return "0";
//        }
//
//
//
//        // 2️⃣ 按 lastModified 倒序
//        S3Object latest = gzFiles.stream()
//                .min(Comparator.comparing(S3Object::lastModified))
//                .orElse(null);



        S3Object latest = s3.listObjectsV2Paginator(request).stream()
                .flatMap(r -> r.contents().stream())
                .filter(o -> o.key().endsWith(".gz"))
                .max(Comparator.comparing(S3Object::lastModified))
                .orElse(null);

        // 3️⃣ 输出结果
        System.out.println("Latest file:");
        System.out.println("Key: " + latest.key());
        System.out.println("LastModified: " + latest.lastModified());
   return  toTimeIsoFmt(latest.lastModified());
    }

    private static String getBktime4es(String bucket, String key, S3Client s3) {
        // 4️⃣ 查询 metadata（核心）
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        HeadObjectResponse response = s3.headObject(request);

        Instant lastModified = response.lastModified();
        String iso = toTimeIsoFmt(lastModified);
        return iso;
    }

    private static String toTimeIsoFmt(Instant lastModified) {
        ZonedDateTime zdt = lastModified.atZone(ZoneId.of("Asia/Kuala_Lumpur"));

//        String formatted = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
//                .format(zdt);
        String iso = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                .withZone(ZoneId.of("Asia/Kuala_Lumpur"))
                .format(lastModified);
        return iso;
    }

    // -------------------------
    // 解析 ini
    // -------------------------
    static Map<String, String> loadIni(String file) throws IOException {
        Map<String, String> map = new HashMap<>();

        for (String line : Files.readAllLines(Paths.get(file))) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] arr = line.split("=", 2);
            if (arr.length == 2) {
                map.put(arr[0].trim(), arr[1].trim());
            }
        }
        return map;
    }

    // -------------------------
    // s3 bucket
    // -------------------------
    static String parseBucket(String s3) {
        // s3://bucket/key...
        return s3.split("/")[2];
    }

    // -------------------------
    // s3 key
    // -------------------------
    static String parseKey(String s3) {
        int idx = s3.indexOf("/", 5); // skip "s3://"
        return s3.substring(idx + 1);
    }
}
