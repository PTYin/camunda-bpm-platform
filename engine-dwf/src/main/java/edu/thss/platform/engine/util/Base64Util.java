package edu.thss.platform.engine.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Base64Util {
    private static char[] base64EncodeChars = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    private static byte[] base64DecodeChars = new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1};

    public Base64Util() {
    }

    private static String decodeBase64ToStr(String str) throws UnsupportedEncodingException {
        byte[] data = str.getBytes();
        int len = data.length;
        ByteArrayOutputStream buf = new ByteArrayOutputStream(len);
        int i = 0;

        while(i < len) {
            byte b1;
            do {
                b1 = base64DecodeChars[data[i++]];
            } while(i < len && b1 == -1);

            if (b1 == -1) {
                break;
            }

            byte b2;
            do {
                b2 = base64DecodeChars[data[i++]];
            } while(i < len && b2 == -1);

            if (b2 == -1) {
                break;
            }

            buf.write(b1 << 2 | (b2 & 48) >>> 4);

            byte b3;
            do {
                b3 = data[i++];
                if (b3 == 61) {
                    return new String(buf.toByteArray(), StandardCharsets.UTF_8);
                }

                b3 = base64DecodeChars[b3];
            } while(i < len && b3 == -1);

            if (b3 == -1) {
                break;
            }

            buf.write((b2 & 15) << 4 | (b3 & 60) >>> 2);

            byte b4;
            do {
                b4 = data[i++];
                if (b4 == 61) {
                    return new String(buf.toByteArray(), StandardCharsets.UTF_8);
                }

                b4 = base64DecodeChars[b4];
            } while(i < len && b4 == -1);

            if (b4 == -1) {
                break;
            }

            buf.write((b3 & 3) << 6 | b4);
        }

        return new String(buf.toByteArray(), StandardCharsets.UTF_8);
    }

    private static String decodeUnicodeToStr(final String unicode) {
        StringBuilder resultStr = new StringBuilder();
        String[] hex = unicode.split("\\\\u");
        String[] var3 = hex;
        int var4 = hex.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String substr = var3[var5];

            try {
                if (substr.length() >= 4 && unicode.contains("\\u" + substr.substring(0, 4))) {
                    try {
                        int chr = Integer.parseInt(substr.substring(0, 4), 16);
                        if (isChinese((char)chr)) {
                            resultStr.append((char)chr);
                            resultStr.append(substr.substring(4));
                        } else {
                            resultStr.append(substr);
                        }
                    } catch (NumberFormatException var8) {
                        resultStr.append(substr);
                    }
                } else {
                    resultStr.append(substr);
                }
            } catch (NumberFormatException var9) {
                resultStr.append(substr);
            }
        }

        return resultStr.toString();
    }

    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    public static String decode(String sql) {
        if (sql == null) {
            return null;
        } else {
            String returnStr;
            try {
                returnStr = decodeBase64ToStr(sql);
                returnStr = matchUnicode(returnStr);
                returnStr = URLDecoder.decode(returnStr.replaceAll("\\+", "%2B"), "UTF-8");
                returnStr = decodeUnicodeToStr(returnStr);
            } catch (Exception var3) {
                returnStr = null;
            }

            return returnStr;
        }
    }

    private static String matchUnicode(String str) {
        Pattern pattern = Pattern.compile("%u([0-9a-fA-F]{4})");

        String matchedStr;
        for(Matcher matcher = pattern.matcher(str); matcher.find(); str = str.replace(matchedStr, matchedStr.replace("%", "\\"))) {
            matchedStr = matcher.group();
        }

        return str.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
    }

    public static String removePrefixSuffix(String str) {
        if (str.length() < 8) {
            return str;
        } else {
            str = str.substring(3);
            str = str.substring(0, str.length() - 5);
            return str;
        }
    }

    public static String encode(String str) {
        byte[] data = str.getBytes();
        StringBuffer sb = new StringBuffer();
        int len = data.length;
        int i = 0;

        while(i < len) {
            int b1 = data[i++] & 255;
            if (i == len) {
                sb.append(base64EncodeChars[b1 >>> 2]);
                sb.append(base64EncodeChars[(b1 & 3) << 4]);
                sb.append("==");
                break;
            }

            int b2 = data[i++] & 255;
            if (i == len) {
                sb.append(base64EncodeChars[b1 >>> 2]);
                sb.append(base64EncodeChars[(b1 & 3) << 4 | (b2 & 240) >>> 4]);
                sb.append(base64EncodeChars[(b2 & 15) << 2]);
                sb.append("=");
                break;
            }

            int b3 = data[i++] & 255;
            sb.append(base64EncodeChars[b1 >>> 2]);
            sb.append(base64EncodeChars[(b1 & 3) << 4 | (b2 & 240) >>> 4]);
            sb.append(base64EncodeChars[(b2 & 15) << 2 | (b3 & 192) >>> 6]);
            sb.append(base64EncodeChars[b3 & 63]);
        }

        return sb.toString();
    }
}

