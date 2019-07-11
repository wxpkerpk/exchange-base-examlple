package com.bitcola.exchange.security.common.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * @author zkq
 * @create 2018-10-11 17:13
 **/
public class CSVUtil {

    /**
     * 下载 cvs
     * @param os 输出流
     * @param headers  头
     * @param items  数据项
     * @throws IOException
     */
    public static void downloadCVS(OutputStream os, String[] headers, List<Object[]> items) throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(os);
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(headers);
        CSVPrinter csvPrinter = new CSVPrinter(osw, csvFormat);
        for (Object[] objects : items) {
            csvPrinter.printRecord(objects);
        }
        csvPrinter.flush();
        csvPrinter.close();
    }





}
