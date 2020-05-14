package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    public void testArray() {
        //定义一个测试数组
        int testArray[] = {3, 4, 5, 6, 7, 8, 9, 10};
        //定义高位、低位记录表示
        int low = 0;
        int high = testArray.length - 1;
        //输入需要求和的值
        int value = 4;
        Boolean bool = true;
        while (low <= high) {
            int subValue = testArray[low] + testArray[high];
            if (testArray[low] > value || value > 2 * testArray[high]) {
                System.out.println("该数组不存在两个数相加为" + value + "的组合");
                bool = false;
                break;
            }
            if (subValue == value) {
                System.out.println("数组第[" + low + "]号元素" + testArray[low] + ",第[" + high + "]号元素" + testArray[high] + "相加和为" + value);
                bool = false;
                break;
            }
            if (subValue > value) {
                high--;
            }
            if (subValue < value) {
                low++;
            }
        }
        if (bool) {
            System.out.println("该数组不存在两个数相加为" + value + "的组合");
        }
    }

    @Test
    public void find() {
        //初始化一个过半数值相同的数组
        int a[] = {2, 7, 3, 3, 3, 3, 3, 4, 6, 2, 3};
        int t = a[0];
        int count = 0;
        for (int i = 0; i < a.length; ++i) {
            if (count == 0) {
                t = a[i];
                count = 1;
            } else {
                if (a[i] == t) {
                    count++;
                } else {
                    count--;
                }
            }
        }
        System.out.println("数组中过半元素是："+t);
    }
}
