package edu.thss.platform.engine.util;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class GlobalVariables {
    public static Map<String, String> variables = new HashMap<String, String>();

    @PostConstruct
    public void init() {
        System.out.println("加载全局变量-----------------------");
    }
}