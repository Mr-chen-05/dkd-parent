package com.dkd;

import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 启动程序
 *
 * @author ruoyi
 */
@EnableFileStorage
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@EnableAspectJAutoProxy(exposeProxy = true)//暴露代理对象，默认是false，如果不暴露，则无法在方法中获取代理对象
public class FuYouApplication
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(FuYouApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  赋优启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}

