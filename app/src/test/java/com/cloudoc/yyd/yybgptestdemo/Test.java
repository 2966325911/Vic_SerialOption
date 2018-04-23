package com.cloudoc.yyd.yybgptestdemo;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author : Vic
 *         time   : 2018/01/04
 *         desc   :
 */

public class Test {
    public void testLinkedHashMapOne(){
        Map<String,String> linkedMap = new LinkedHashMap<>();
        linkedMap.put("b","2");
        linkedMap.put("a","1");
        linkedMap.put("c","3");
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        String jsonStr1 = gson.toJson(linkedMap);
        System.out.println(jsonStr1);
    }

    public void testLinkedHashMap(){
        Map<String,String> linkedMap = new LinkedHashMap<>();
        linkedMap.put("b","2");
        linkedMap.put("a","1");
        linkedMap.put("c","3");
        String jsonStr = JSON.toJSONString(linkedMap);
        System.out.print(jsonStr);
    }
    @org.junit.Test
    public void testHashTable(){
        Hashtable<String,String> table = new Hashtable<>();
        table.put("1","A");
        table.put("2","B");
        table.put("3","C");


        Enumeration<String> en1 = table.keys();
        while(en1.hasMoreElements()) {
            System.out.print(en1.nextElement() + " , ");
        }

        System.out.println();
        Enumeration<String> en2 = table.elements();
        while (en2.hasMoreElements()) {
            System.out.print(en2.nextElement() + " , ");
        }
        System.out.println();

        Iterator<String> it1 = table.keySet().iterator();
        while (it1.hasNext()){
            System.out.print(it1.next() + " , ");
        }
        System.out.println();

        Iterator<Map.Entry<String,String>> it2 = table.entrySet().iterator();
        while (it2.hasNext()){
            System.out.print(it2.next() + " , ");
        }
        System.out.println();
    }

}
