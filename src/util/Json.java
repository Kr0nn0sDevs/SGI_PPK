package util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Soporta: objetos {}, arrays [], strings "", numbers, booleans, null.
 */
public class Json {

    // TIPOS

    public static class JArray extends ArrayList<Object> {
        public JObj getObj(int i)    { return (JObj) get(i); }
        public JArray getArr(int i)  { return (JArray) get(i); }
        public String getStr(int i)  { return (String) get(i); }
        public double getNum(int i)  { return ((Number) get(i)).doubleValue(); }
        public boolean getBool(int i){ return (Boolean) get(i); }
    }

    public static class JObj extends LinkedHashMap<String, Object> {
        public JObj getObj(String k)    { return (JObj) get(k); }
        public JArray getArr(String k)  { return (JArray) getOrDefault(k, new JArray()); }
        public String getStr(String k)  { return (String) getOrDefault(k, ""); }
        public double getNum(String k)  {
            Object v = get(k);
            return v instanceof Number ? ((Number)v).doubleValue() : 0;
        }
        public int getInt(String k)     { return (int) getNum(k); }
        public boolean getBool(String k){
            Object v = get(k);
            return v instanceof Boolean ? (Boolean)v : false;
        }
        public boolean has(String k)    { return containsKey(k) && get(k) != null; }
    }

    // PARSER

    private final String src;
    private int pos;

    private Json(String src) { this.src = src; this.pos = 0; }

    public static Object parse(String json) {
        return new Json(json.trim()).parseValue();
    }

    public static JArray parseArray(String json) {
        Object r = parse(json);
        return r instanceof JArray ? (JArray)r : new JArray();
    }

    private Object parseValue() {
        skipWs();
        if (pos >= src.length()) return null;
        char c = src.charAt(pos);
        if (c == '{') return parseObj();
        if (c == '[') return parseArr();
        if (c == '"') return parseStr();
        if (c == 't') { pos += 4; return true; }
        if (c == 'f') { pos += 5; return false; }
        if (c == 'n') { pos += 4; return null; }
        return parseNum();
    }

    private JObj parseObj() {
        JObj obj = new JObj();
        pos++; // '{'
        skipWs();
        while (pos < src.length() && src.charAt(pos) != '}') {
            skipWs();
            String key = parseStr();
            skipWs();
            pos++; // ':'
            Object val = parseValue();
            obj.put(key, val);
            skipWs();
            if (pos < src.length() && src.charAt(pos) == ',') pos++;
            skipWs();
        }
        if (pos < src.length()) pos++; // '}'
        return obj;
    }

    private JArray parseArr() {
        JArray arr = new JArray();
        pos++; // '['
        skipWs();
        while (pos < src.length() && src.charAt(pos) != ']') {
            arr.add(parseValue());
            skipWs();
            if (pos < src.length() && src.charAt(pos) == ',') pos++;
            skipWs();
        }
        if (pos < src.length()) pos++; // ']'
        return arr;
    }

    private String parseStr() {
        pos++; // '"'
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char c = src.charAt(pos++);
            if (c == '"') break;
            if (c == '\\' && pos < src.length()) {
                char e = src.charAt(pos++);
                switch (e) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    default:  sb.append(e);
                }
            } else sb.append(c);
        }
        return sb.toString();
    }

    private Number parseNum() {
        int start = pos;
        while (pos < src.length() && "0123456789.-+eE".indexOf(src.charAt(pos)) >= 0) pos++;
        String s = src.substring(start, pos);
        return s.contains(".") || s.contains("e") || s.contains("E")
            ? Double.parseDouble(s) : Long.parseLong(s);
    }

    private void skipWs() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
    }

    // WRITER

    public static String toJson(Object val) {
        if (val == null) return "null";
        if (val instanceof String) return "\"" + escape((String)val) + "\"";
        if (val instanceof Boolean || val instanceof Number) return val.toString();
        if (val instanceof JArray || val instanceof List) {
            StringBuilder sb = new StringBuilder("[");
            List<?> list = (List<?>) val;
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(list.get(i)));
            }
            return sb.append("]").toString();
        }
        if (val instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?,?> e : ((Map<?,?>)val).entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escape(e.getKey().toString())).append("\":")
                  .append(toJson(e.getValue()));
                first = false;
            }
            return sb.append("}").toString();
        }
        return "\"" + escape(val.toString()) + "\"";
    }

    private static String escape(String s) {
        return s.replace("\\","\\\\").replace("\"","\\\"")
                .replace("\n","\\n").replace("\r","\\r").replace("\t","\\t");
    }

    // I/O

    public static JArray readArray(String path) {
        File f = new File(path);
        if (!f.exists()) return new JArray();
        try {
            byte[] b = new FileInputStream(f).readAllBytes();
            return parseArray(new String(b, StandardCharsets.UTF_8));
        } catch (Exception e) { return new JArray(); }
    }

    public static void writeArray(String path, JArray arr) {
        new File(path).getParentFile().mkdirs();
        try (Writer w = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8)) {
            // Escritura pretty-print sencilla
            w.write("[\n");
            for (int i = 0; i < arr.size(); i++) {
                w.write("  " + toJson(arr.get(i)));
                if (i < arr.size()-1) w.write(",");
                w.write("\n");
            }
            w.write("]");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static String newId(String prefix) {
        return prefix + System.currentTimeMillis() + (int)(Math.random()*1000);
    }
}
