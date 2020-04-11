package com.atguigu.gmall.order;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GmallOrderWebApplicationTests {

	@Test
	public void contextLoads() {
		int i1 = 10, i2 = 10;
		System.out.println("i1 + i2 = " + i1 + i2);
		System.out.println("i1 + i2 = " + i1 * i2);
		System.out.println("i1 + i2 = " + i1 / i2);
	}

}
