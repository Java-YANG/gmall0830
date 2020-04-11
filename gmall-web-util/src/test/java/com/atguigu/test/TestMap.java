package com.atguigu.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestMap {
    public static void main(String[] args) {
        Map<Object,Object> map = new HashMap<>();
        List<String> list = new ArrayList<>();
        map.put(list,1);

        char c = '汉';
        System.out.println(c);
    }
}
