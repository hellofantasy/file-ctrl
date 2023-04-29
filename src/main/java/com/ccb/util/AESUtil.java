package com.ccb.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 数据库的验证信息进行加密
 */
@Slf4j
public class AESUtil {

    private static String key = "ccbyy666888";
    /**
     * 加密
     *
     * @param content
     * @param strKey
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(String content, String strKey) throws Exception {
        SecretKeySpec skeySpec = getKey(strKey);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes());
                        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(content.getBytes());
        return encrypted;
    }

    /**
     * 解密
     *
     * @param strKey
     * @param content
     * @return
     * @throws Exception
     */
    public static String decrypt(byte[] content, String strKey) throws Exception {
        SecretKeySpec skeySpec = getKey(strKey);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes());
                        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        byte[] original = cipher.doFinal(content);
        String originalString = new String(original);
        return originalString;
    }

    private static SecretKeySpec getKey(String strKey) throws Exception {
        byte[] arrBTmp = strKey.getBytes();
        byte[] arrB = new byte[16]; // 创建一个空的        16位字节数组（默认值为0）
        for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {
            arrB[i] = arrBTmp[i];
        }
        SecretKeySpec skeySpec = new SecretKeySpec(arrB,
                "AES");
        return skeySpec;
    }


    /**
     * base 64 encode
     *
     * @param bytes 待编码的byte[]
     * @return 编码后的base 64 code
     */

    /**
     * base 64 decode
     *
     * @param base64Code 待解码的base 64 code
     * @return 解码后的byte[]
     * @throws Exception
     */


    /**
     * AES加密为base 64 code
     *
     * @param content 待加密的内容
     * @return 加密后的base 64 code
     * @throws Exception //加密传String类型，返回
    String类型
     */
    public static String aesEncrypt(String content) throws Exception {
        return "";

    }

    /**
     * 将base 64 code AES解密
     *
     * @param encryptStr 待解密的base 64 code
     * @return 解密后的string   //解密传String类�
    ��，返回String类型
     * @throws Exception
     */
    public static String aesDecrypt(String encryptStr) throws Exception {
        return "";
    }

    public static int aaa() {

        return 2 / 0;

    }

    public static void main(String[] args) throws Exception {

        System.out.println(aaa()+"22222");
//            SpringApplication.run(AESUtil.class);
//            if (GU.isNull(args) || args.length == 0) {
//                return;
//            }


//            System.out.println(aesEncrypt("ccda"));
//            System.out.println(aesEncrypt("Password123"
//            if ("E".equals(args[0])) {
//                System.out.println("明文：" + args[1
//                System.out.println("密文：" + encryp
//                System.out.println();
//            } else if ("D".equals(args[0])) {
//                System.out.println("密文：" + args[1
//                String decrypt = aesDecrypt(args[1]);
//                System.out.println("明文：" + decryp
//                System.out.println();
//            } else {
//        } catch (Exception e) {
//            log.error(e.getMessage(),e);
//        } finally {
//            System.exit(0);
//        }

//
//        String decrypt = aesDecrypt("");
//
//        encrypt = aesEncrypt("ccda@W02820");
//        System.out.println(encrypt);
//        decrypt = aesDecrypt(encrypt);
//        System.out.println(decrypt);
//
//        encrypt = aesEncrypt("applaud");
//        System.out.println(encrypt);
//        decrypt = aesDecrypt(encrypt);
//        System.out.println(decrypt);
//
//
//        encrypt = aesEncrypt("readuser");
//        System.out.println(encrypt);
//         decrypt = aesDecrypt(encrypt);
//        System.out.println(decrypt);
//
//        encrypt = aesEncrypt("applaud@W02820");
//        System.out.println(encrypt);
//        decrypt = aesDecrypt(encrypt);
//        System.out.println(decrypt);
//
//        encrypt = aesEncrypt("testUser");
//        System.out.println(encrypt);
//        decrypt = aesDecrypt(encrypt);
//        System.out.println(decrypt);
//
//        encrypt = aesEncrypt("abc");
//        System.out.println(encrypt);
//        decrypt = aesDecrypt(encrypt);
//        System.out.println(decrypt);

    }
}
