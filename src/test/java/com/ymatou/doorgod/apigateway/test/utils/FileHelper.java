/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.apigateway.test.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;

/**
 * @author luoshiqian 2016/10/8 18:24
 */
public class FileHelper {

    public static List<String> loadIp()throws Exception{

        return FileUtils.readLines(new File(FileHelper.class.getResource("/IP.txt").getFile()));
    }

    public static List<String> loadDeviceId()throws Exception{

        return FileUtils.readLines(new File(FileHelper.class.getResource("/DeviceId.txt").getFile()));
    }


    public static void main(String[] args) {
        try {
            System.out.println(loadIp());
            System.out.println(loadDeviceId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
