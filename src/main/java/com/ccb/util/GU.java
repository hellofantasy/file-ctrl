package com.ccb.util;



import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class GU {

    /**
     * 判断对象是否为空
     *
     * @param obj
     * @return
     */
    public static boolean isNull(Object obj) {
        if (obj instanceof String) {
            String s = (String) obj;
            if (s == null || s.length() <= 0) {
                return true;
            } else {
                return false;
            }
        }
        if (obj instanceof List) {
            List list = (List) obj;
            if (list == null || list.size() <= 0) {
                return true;
            } else {
                return false;
            }
        }
        if (obj instanceof Set) {
            Set set = (Set) obj;
            if (set == null || set.size() <= 0) {
                return true;
            } else {
                return false;
            }
        }
        if (obj instanceof Map) {
            Map map = (Map) obj;
            if (map.isEmpty()) {
                return true;
            } else {
                return false;
            }
        }
        return (obj == null);
    }

    public static boolean isNull(Object... objs) {
        boolean r = false;
        for (Object obj : objs) {
            r = r || isNull(obj);
        }
        return r;
    }

    public static boolean isNotNull(Object obj) {
        if (GU.isNull(obj)) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isNotNull(Object... objs) {
        boolean r = true;
        for (Object obj : objs) {
            r = r && isNotNull(obj);
        }
        return r;
    }
    public static String getTimestamp(String pattern){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(new Date());
    }
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    public static String getTimestamp(){
        return dateFormat.format(new Date());
    }
    public static boolean isEqual(Object str1, Object str2) {
        if (isNull(str1, str2)) {
            return false;
        }
        if (str1 instanceof String && str2 instanceof String) {
            String s1 = (String) str1;
            String s2 = (String) str2;
            if (s1.contains(s2) || s2.contains(s1)) {
                return true;
            } else if (s1.startsWith(s2)) {
                return true;
            } else if (s1.startsWith(s2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查email格式
     *
     * @param email
     * @return
     * @throws Exception
     */
    public static boolean isEmail(String email) throws Exception {
        if (null == email || "".equals(email)) return false;
        //    Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配
        Pattern p = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * 判断数字是否是正负整数小数
     *
     * @param str
     * @param type 1:正整数;2:正小数;3:负整数;4:负小数
     * @return
     * @throws Exception
     */
    public static boolean isNumber1(String str, int type) {
        boolean sf = true;
        Pattern pattern;
        Matcher isNum;
        switch (type) {
            case 1:
                pattern = Pattern.compile("^\\d+$");
                isNum = pattern.matcher(str);
                if (!isNum.matches()) {
                    sf = false;
                }
                break;
            case 2:
                pattern = Pattern.compile("^\\d+\\.\\d+$");
                isNum = pattern.matcher(str);
                if (!isNum.matches()) {
                    sf = false;
                }
                break;
            case 3:
                pattern = Pattern.compile("-\\d+$");
                isNum = pattern.matcher(str);
                if (!isNum.matches()) {
                    sf = false;
                }
                break;
            case 4:
                pattern = Pattern.compile("^-\\d+\\.\\d+$");
                isNum = pattern.matcher(str);
                if (!isNum.matches()) {
                    sf = false;
                }
                break;
        }
        return sf;
    }




    /**
     * 判断数字是否是正负整数小数
     *
     * @param str
     * @return
     * @throws Exception
     */


    /**
     * 验证身份证号码
     *
     * @param idCard 居民身份证号码15位或18位，最后一位可能是数字或字母
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean isIdCard(String idCard) {
        String regex = "[1-9]\\d{13,16}[a-zA-Z0-9]{1}";
        return Pattern.matches(regex, idCard);
    }

    /**
     * 验证手机号码（支持国际格式，+86135xxxx...（中国内地），+00852137xxxx...（中国香港））
     *
     * @param mobile 移动、联通、电信运营商的号码段
     *               <p>移动的号段：134(0-8)、135、136、137、138、139、147（预计用于TD上网卡）
     *               、150、151、152、157（TD专用）、158、159、187（未启用）、188（TD专用）</p>
     *               <p>联通的号段：130、131、132、155、156（世界风专用）、185（未启用）、186（3g）</p>
     *               <p>电信的号段：133、153、180（未启用）、189</p>
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean isMobile(String mobile) {
        String regex = "(\\+\\d+)?1[34578]\\d{9}$";
        return Pattern.matches(regex, mobile);
    }

    /**
     * 验证固定电话号码
     *
     * @param phone 电话号码，格式：国家（地区）电话代码 + 区号（城市代码） + 电话号码，如：+8602085588447
     *              <p><b>国家（地区） 代码 ：</b>标识电话号码的国家（地区）的标准国家（地区）代码。它包含从 0 到 9 的一位或多位数字，
     *              数字之后是空格分隔的国家（地区）代码。</p>
     *              <p><b>区号（城市代码）：</b>这可能包含一个或多个从 0 到 9 的数字，地区或城市代码放在圆括号——
     *              对不使用地区或城市代码的国家（地区），则省略该组件。</p>
     *              <p><b>电话号码：</b>这包含从 0 到 9 的一个或多个数字 </p>
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean isPhone(String phone) {
        String regex = "(\\+\\d+)?(\\d{3,4}\\-?)?\\d{7,8}$";
        return Pattern.matches(regex, phone);
    }

    /**
     * URL检查<br>
     * <br>
     *
     * @param pInput 要检查的字符串<br>
     * @return boolean   返回检查结果<br>
     */
    public static boolean isUrl(String pInput) {
        if (pInput == null) {
            return false;
        }
        String regEx = "^(http|https|ftp)\\://([a-zA-Z0-9\\.\\-]+(\\:[a-zA-" + "Z0-9\\.&%\\$\\-]+)*@)?((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{"
                + "2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}"
                + "[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|"
                + "[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-"
                + "4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|([a-zA-Z0"
                + "-9\\-]+\\.)*[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,4})(\\:[0-9]+)?(/"
                + "[^/][a-zA-Z0-9\\.\\,\\?\\'\\\\/\\+&%\\$\\=~_\\-@]*)*$";

        Pattern p = Pattern.compile(regEx);
        Matcher matcher = p.matcher(pInput);
        return matcher.matches();
    }

    /**
     * @作者 YYZ
     * @电邮 yyzvolat@163.com
     * @日期 2013-4-17
     * @方法说明 判断文件后缀是否是不允许上传的类型
     * 值为真是不允许上传
     */
    public static boolean isBanFileType(String filename) throws Exception {
        boolean isok = false;
        String fileType = "";
        if (filename.indexOf(".") >= 0) {
            fileType = filename.substring(filename.indexOf(".")).toLowerCase();
            if (".jsp".equalsIgnoreCase(fileType)) {
                isok = true;
            } else if (".js".equalsIgnoreCase(fileType)) {
                isok = true;
            } else if (".com".equalsIgnoreCase(fileType)) {
                isok = true;
            } else if (".exe".equalsIgnoreCase(fileType)) {
                isok = true;
            } else if (".bat".equalsIgnoreCase(fileType)) {
                isok = true;
            } else if (".vbs".equalsIgnoreCase(fileType)) {
                isok = true;
            }
        } else {
            isok = true;
        }
        return isok;
    }

    /**
     * 判断字符串中是否有非法字符
     *
     * @param str
     * @return
     * @throws Exception
     * @throws Exception
     */
    public static boolean isfilterStr(String str) {
        boolean sf = true;
        String help = "~`!@#$%^&()_-+={}[]|\\:;',./<>?\"";
        if (!isNull(str)) {
            for (int i = 0; i < str.length(); i++) {
                int b = help.indexOf(str.substring(i, i + 1));
                if (b >= 0) {
                    sf = false;
                    break;
                }
            }

        }
        return sf;
    }


    /**
     * 转16进制
     *
     * @param buffer
     * @return
     */
    public static String toHex(byte buffer[]) {
        StringBuffer sb = new StringBuffer();
        String s = null;
        for (int i = 0; i < buffer.length; i++) {
            s = Integer.toHexString((int) buffer[i] & 0xff);
            if (s.length() < 2) sb.append('0');//后零or前零
            sb.append(s);
        }
        return (sb.toString());
    }


    /**
     * 生成MD5
     *
     * @param str
     * @return
     */
    public static String getMD5(String str) throws Exception {
        try {
            MessageDigest ss11 = MessageDigest.getInstance("MD5");
            ss11.update(str.getBytes());
            StringBuffer ss12 = new StringBuffer(toHex(ss11.digest()).substring(0, 32));
            while (ss12.length() < 32) {
                ss12.append('0');
            }
            return ss12.toString();
        } catch (Exception e) {
            throw new Exception("MD5失败");
        }
    }


    /**
     * @作者 YYZ
     * @电邮 yyzvolat@qq.com
     * @日期 2016-9-10
     * @方法说明 生成随机颜色
     */




    /**
     * 将容易引起xss漏洞的半角字符直接替换成全角字符
     *
     * @param s
     * @return
     */
    public static String xssEncode(String s) {
        if (s == null || "".equals(s)) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '>':
                    sb.append('＞');//全角大于号
                    break;
                case '<':
                    sb.append('＜');//全角小于号
                    break;
                case '\'':
                    sb.append('‘');//全角单引号
                    break;
                case '\"':
                    sb.append('“');//全角双引号
                    break;
                case '&':
                    sb.append('＆');//全角
                    break;
                //            case '\\':
                //               sb.append('＼');//全角斜线
                //               break;
                case '#':
                    sb.append('＃');//全角井号
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * @作者 YYZ
     * @电邮 yyzvolat@163.com
     * @日期 2016-9-6
     * @方法说明 组装返回JSON格式的操作提示信息
     * 参数1:(0:失败,1:成功)
     * 参数2:(参数1的文字说明)
     */

    public static String sendJsonMsg(String str, String str1) throws Exception {
        String ss = "{\"code\":\"" + str + "\",\"msg\":\"" + str1 + "\"}";
        return ss;
    }

    /**
     * 利用打乱成密文，生成ID
     *
     * @return @throws
     * Exception
     */
    public static String generalFileID() throws Exception {
        try {
            UUID uuid = UUID.randomUUID();
            return uuid.toString().replaceAll("-", "");
        } catch (Exception e) {
            throw new Exception("生成编号失败", e);
        }
    }

    /**
     * 将比特B换算成KB或者MB或者GB
     *
     * @return
     * @throws Exception
     */
    public static String changBtoKBorMBorGB(long size) throws Exception {
        String result = "";
        DecimalFormat df = new DecimalFormat("#.00");
        if (size > 1024 * 1024 * 1024) {
            size = size / (1024 * 1024 * 1024);
            result = size + "GB";
        } else if (size > 1024 * 1024) {
            size = size / (1024 * 1024);
            result = size + "MB";
        } else if (size > 1024) {
            size = size / (1024);
            result = size + "KB";
        } else {
            result = size + "B";
        }
        return result;
    }

    /**
     * @作者 YYZ
     * @电邮 yyzvolat@qq.com
     * @日期 2016-9-13
     * @方法说明 从字符串中找出@后面的值,并返回数组
     */

    public static List getAtValue(String content) throws Exception {
        List list = new ArrayList();
        // 微博内容中的at正则表达式
        Pattern AT_PATTERN = Pattern.compile("@[\\u4e00-\\u9fa5\\w\\-]+");
        Matcher m = AT_PATTERN.matcher(content);
        int i = 0;
        while (m.find()) {
            String atUserName = m.group();
            list.add(atUserName.replaceAll("@", ""));
        }
        return list;
    }

    /**
     * @作者 YYZ
     * @电邮 yyzvolat@qq.com
     * @日期 2016-9-13
     * @方法说明 从字符串中找出#话题#中间的值, 并返回数组
     */

    public static List getTagValue(String content) throws Exception {
        List list = new ArrayList();
        // 微博内容中的at正则表达式
        Pattern TAG_PATTERN = Pattern.compile("#([^\\#|.]+)#");
        Matcher m = TAG_PATTERN.matcher(content);
        int i = 0;
        while (m.find()) {
            String tagNameMatch = m.group();
            list.add(tagNameMatch.replaceAll("#", ""));
        }
        return list;
    }


    /**
     * 转换返回值类型为UTF-8格式.
     *
     * @param is
     * @return
     */
    public static String convertStreamToString(InputStream is) {
        StringBuilder sb1 = new StringBuilder();
        byte[] bytes = new byte[4096];
        int size = 0;

        try {
            while ((size = is.read(bytes)) > 0) {
                String str = new String(bytes, 0, size, "UTF-8");
                sb1.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb1.toString();
    }

    /**
     * 得到网页中图片的地址
     */
    public static List<String> getImgStr(String htmlStr) {
        String img = "";
        Pattern p_image;
        Matcher m_image;
        List<String> pics = new ArrayList<String>();

        String regEx_img = "<img.*src=(.*?)[^>]*?>"; //图片链接地址
        p_image = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE);
        m_image = p_image.matcher(htmlStr);
        while (m_image.find()) {
            img = img + "," + m_image.group();
            Matcher m = Pattern.compile("src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>").matcher(img); //匹配src
            while (m.find()) {
                pics.add(m.group(1));
            }
        }
        return pics;
    }

    /**
     * @作者 YYZ
     * @电邮 yyzvolat@163.com
     * @日期 Mar 25, 2011
     * @方法说明 过滤html标签
     */

    public static String Html2Text(String inputString) throws Exception {
        String htmlStr = inputString; //含html标签的字符串
        String textStr = "";
        Pattern p_script;
        Matcher m_script;
        Pattern p_style;
        Matcher m_style;
        Pattern p_html;
        Matcher m_html;

        try {
            String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>"; //定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script> }
            String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>"; //定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style> }
            String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式

            p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
            m_script = p_script.matcher(htmlStr);
            htmlStr = m_script.replaceAll(""); //过滤script标签

            p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
            m_style = p_style.matcher(htmlStr);
            htmlStr = m_style.replaceAll(""); //过滤style标签

            p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
            m_html = p_html.matcher(htmlStr);
            htmlStr = m_html.replaceAll(""); //过滤html标签

            textStr = htmlStr;
            textStr = textStr.replaceAll("\\s", ""); //过滤换行符
            textStr = textStr.replaceAll("&nbsp;", ""); //过滤转义字符 空格
            textStr = textStr.replaceAll(" ", ""); //过滤转义字符 空格
            textStr = textStr.replaceAll("　", ""); //过滤转义字符 空格

        } catch (Exception e) {
            throw new Exception("HTML转换TEXT失败！");
        }

        return textStr;//返回文本字符串
    }

    /**
     * @作者 YYZ
     * @电邮 yyzvolat@163.com
     * @日期 Mar 25, 2011
     * @方法说明 过滤script标签
     */

    public static String filterSscript(String inputString) throws Exception {
        String htmlStr = inputString; //含script标签的字符串
        String textStr = "";
        Pattern p_script;
        Matcher m_script;

        try {
            String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>"; //定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script> }
            p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
            m_script = p_script.matcher(htmlStr);
            htmlStr = m_script.replaceAll(""); //过滤script标签
            textStr = htmlStr;
        } catch (Exception e) {
            throw new Exception("过滤script标签失败！");
        }

        return textStr;//返回文本字符串
    }

    /**
     * 获取文件列表及其子文件目录和文件 非递归
     *
     * @param path
     * @return
     */
    public List<String> scanFiles(String path) {
        List<String> filePaths = new ArrayList<String>();
        LinkedList<File> list = new LinkedList<File>();
        File dir = new File(path);
        File[] file = dir.listFiles();

        for (int i = 0; i < file.length; i++) {
            if (file[i].isDirectory()) {
                // 把第一层的目录，全部放入链表
                list.add(file[i]);
            }
            filePaths.add(file[i].getAbsolutePath());
        }
        // 循环遍历链表
        while (!list.isEmpty()) {
            // 把链表的第一个记录删除
            File tmp = list.removeFirst();
            // 如果删除的目录是一个路径的话
            if (tmp.isDirectory()) {
                // 列出这个目录下的文件到数组中
                file = tmp.listFiles();
                if (file == null) {// 空目录
                    continue;
                }
                // 遍历文件数组
                for (int i = 0; i < file.length; ++i) {
                    if (file[i].isDirectory()) {
                        // 如果遍历到的是目录，则将继续被加入链表
                        list.add(file[i]);
                    }
                    filePaths.add(file[i].getAbsolutePath());
                }
            }
        }
        return filePaths;
    }

    /**
     * 将字符串表示的ip地址转换为long表示.
     *
     * @param ip ip地址
     * @return 以32位整数表示的ip地址
     */
    public static final long ip2Long(final String ip) {
        final String[] ipNums = ip.split("\\.");
        return (Long.parseLong(ipNums[0]) << 24) + (Long.parseLong(ipNums[1]) << 16) + (Long.parseLong(ipNums[2]) << 8) + (Long.parseLong(ipNums[3]));
    }

    /**
     * 将整数表示的ip地址转换为字符串表示.
     *
     * @param ip 32位整数表示的ip地址
     * @return 点分式表示的ip地址
     */
    public static final String long2Ip(final long ip) {
        final long[] mask = {0x000000FF, 0x0000FF00, 0x00FF0000, 0xFF000000};
        final StringBuilder ipAddress = new StringBuilder();
        for (int i = 0; i < mask.length; i++) {
            ipAddress.insert(0, (ip & mask[i]) >> (i * 8));
            if (i < mask.length - 1) {
                ipAddress.insert(0, ".");
            }
        }
        return ipAddress.toString();
    }


    public static String sqlFilter(String hql) {
        int s1 = hql.toLowerCase().indexOf(" order");
        if (s1 > 0) {
            hql = hql.substring(0, s1);
        }
        return hql;
    }

    public static void playVoice(String path) {
        File sound1 = new File("C:\\Users\\adminstrator\\Desktop\\voice.wav");//java只支持wav格式
        AudioClip sound_choose;
        try {
            sound_choose = Applet.newAudioClip(sound1.toURL());
            sound_choose.play();//播放
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public static String toStr(Object obj, String defaultValue) {
        if (isNull(obj)) {
            return defaultValue;
        }
        return (String) obj;
    }

    public static Integer toInt(Object obj, Integer defaultValue) {
        if (isNull(obj)) {
            return defaultValue;
        }
        return (Integer) obj;
    }

    public static void print(String data) {
        System.out.println(data);
    }


    /**
     * 检查指定的字符串是否为空。
     * <ul>
     * <li>SysUtils.isEmpty(null) = true</li>
     * <li>SysUtils.isEmpty("") = true</li>
     * <li>SysUtils.isEmpty("   ") = true</li>
     * <li>SysUtils.isEmpty("abc") = false</li>
     * </ul>
     *
     * @param value 待检查的字符串
     * @return true/false
     */
    public static boolean isEmpty(String value) {
        int strLen;
        if (value != null && (strLen = value.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(value.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public static boolean areNotEmpty(String... values) {
        boolean result = true;
        if (values != null && values.length != 0) {
            String[] arr$ = values;
            int len$ = values.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                String value = arr$[i$];
                result &= !isEmpty(value);
            }
        } else {
            result = false;
        }

        return result;
    }

    public static boolean areEmpty(String... values) {
        return !areNotEmpty(values);
    }

    public static boolean equals(String a, String b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }

    public static ArrayList arrayToList(Object[] from) {
        ArrayList<Object> objects = new ArrayList<>();
        for (Object obj : from) {
            objects.add(obj);
        }
        return objects;
    }

    public static String append(String taget, String... source) {
        StringBuffer stringBuffer = new StringBuffer(taget);
        for (String str : source) {
            stringBuffer.append(str);
        }
        return stringBuffer.toString();
    }

    public static Object[] list2Array(List<Object> from) {
        if (isNull(from)) {
            return null;
        }
        String[] array = new String[from.size()];
        array = from.toArray(array);
        return array;
    }
}