package com.bitcola.dataservice.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Component
public class SensitiveWord {
    private StringBuilder replaceAll;//初始化
    private String encoding = "UTF-8";
    private String replaceStr = "*";
    private int replaceSize = 500;
    private String fileName = "limit.txt";
    private List<String> arrayList;
    public Set<String> sensitiveWordSet;//包含的敏感词列表，过滤掉重复项
    public List<String> sensitiveWordList;//包含的敏感词列表，包括重复项，统计次数

    /**
     * 文件要求路径在src或resource下，默认文件名为CensorWords.txt
     *
     * @param fileName 词库文件名(含后缀)
     */
    public SensitiveWord(String fileName) {
        this.fileName = fileName;
        InitializationWork();
    }

    /**
     * @param replaceStr  敏感词被转换的字符
     * @param replaceSize 初始转义容量
     */
    public SensitiveWord(String replaceStr, int replaceSize) {
        this.replaceStr = fileName;
        this.replaceSize = replaceSize;
    }

    public SensitiveWord() {
        InitializationWork();
    }

    public boolean contain(String str) {
        if (StringUtils.isBlank(str)){
            return false;
        }
        StringBuilder buffer = new StringBuilder(str);
        String temp;
        for (int x = 0; x < arrayList.size(); x++) {
            temp = arrayList.get(x);
            int i = buffer.indexOf(temp);
            if (i > -1) {
                return true;
            }
        }
        return false;
    }


    /**
     * @param str 将要被过滤信息
     * @return 过滤后的信息
     */
    public String filterInfo(String str) {
        sensitiveWordSet = new HashSet<String>();
        sensitiveWordList = new ArrayList<>();
        StringBuilder buffer = new StringBuilder(str);
        HashMap<Integer, Integer> hash = new HashMap<Integer, Integer>(arrayList.size());
        String temp;
        for (int x = 0; x < arrayList.size(); x++) {
            temp = arrayList.get(x);
            int findIndexSize = 0;
            for (int start = -1; (start = buffer.indexOf(temp, findIndexSize)) > -1; ) {
                //System.out.println("###replace="+temp);
                findIndexSize = start + temp.length();//从已找到的后面开始找
                Integer mapStart = hash.get(start);//起始位置
                if (mapStart == null || (mapStart != null && findIndexSize > mapStart))//满足1个，即可更新map
                {
                    hash.put(start, findIndexSize);
                    //System.out.println("###敏感词："+buffer.substring(start, findIndexSize));
                }
            }
        }
        Collection<Integer> values = hash.keySet();
        for (Integer startIndex : values) {
            Integer endIndex = hash.get(startIndex);
            //获取敏感词，并加入列表，用来统计数量
            String sensitive = buffer.substring(startIndex, endIndex);
            //System.out.println("###敏感词："+sensitive);
            if (!sensitive.contains("*")) {//添加敏感词到集合
                sensitiveWordSet.add(sensitive);
                sensitiveWordList.add(sensitive);
            }
            buffer.replace(startIndex, endIndex, replaceAll.substring(0, endIndex - startIndex));
        }
        hash.clear();
        return buffer.toString();
    }

    /**
     * 初始化敏感词库
     */
    public void InitializationWork() {
        replaceAll = new StringBuilder(replaceSize);
        for (int x = 0; x < replaceSize; x++) {
            replaceAll.append(replaceStr);
        }
        //加载词库
        arrayList = new ArrayList<String>();
        InputStreamReader read = null;
        BufferedReader bufferedReader = null;
        try {
            read = new InputStreamReader(SensitiveWord.class.getClassLoader().getResourceAsStream(fileName), encoding);
            bufferedReader = new BufferedReader(read);
            for (String txt = null; (txt = bufferedReader.readLine()) != null; ) {
                if (!arrayList.contains(txt))
                    arrayList.add(txt);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != bufferedReader)
                    bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (null != read)
                    read.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public StringBuilder getReplaceAll() {
        return replaceAll;
    }

    public void setReplaceAll(StringBuilder replaceAll) {
        this.replaceAll = replaceAll;
    }

    public String getreplaceStr() {
        return replaceStr;
    }

    public void setreplaceStr(String replaceStr) {
        this.replaceStr = replaceStr;
    }

    public int getreplaceSize() {
        return replaceSize;
    }

    public void setreplaceSize(int replaceSize) {
        this.replaceSize = replaceSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<String> getArrayList() {
        return arrayList;
    }

    public void setArrayList(List<String> arrayList) {
        this.arrayList = arrayList;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
