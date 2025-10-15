package com.dkd.test;

import com.dkd.generator.util.VelocityInitializer;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.FileWriter;
import java.util.List;


public class VelocityDemoTest {
    public static void main(String[] args) throws Exception {
        // 1.初始化模版引擎
        VelocityInitializer.initVelocity();
        // 2.准备数据模型
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("message", "王强不加油！！");
        // 创建区域对象
        Region region1 = new Region(1L, "上海陆家嘴");
        Region region2 = new Region(2L, "江苏盐城");
        velocityContext.put("region", region1);
        List<Region> list = List.of(region1, region2);
        velocityContext.put("regionList", list);
        // 3.读取模版
        Template template = Velocity.getTemplate("vm/index.html.vm", "UTF-8");
        // 4.渲染模版
        FileWriter fileWriter = new FileWriter("E:\\ideaProgram\\dkd\\index.html");// 输出到文件
        template.merge(velocityContext,fileWriter);
        fileWriter.close();
    }
}
