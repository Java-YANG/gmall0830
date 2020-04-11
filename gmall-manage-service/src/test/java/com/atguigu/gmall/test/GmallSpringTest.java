package com.atguigu.gmall.test;

import com.atguigu.gmall.util.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

@SpringBootTest(classes=GmallSpringTest.class)
@RunWith(SpringRunner.class)
@ComponentScan(basePackages = "com.atguigu.gmall.config")
public class GmallSpringTest {
    @Autowired
    private RedisUtil redisUtil;

    @Test
    public void testRedisUtil(){
        Jedis jedis = redisUtil.getJedis();
        String ping = jedis.ping();

        System.out.println(ping);
    }
}
