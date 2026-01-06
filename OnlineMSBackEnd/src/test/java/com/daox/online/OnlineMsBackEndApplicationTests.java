package com.daox.online;

import com.daox.online.uilts.HybridIdGenerator;
import com.daox.online.uilts.SecondaryHybridIdGenerator;
import com.daox.online.uilts.UserIdUtil;
import org.junit.jupiter.api.Test;

class OnlineMsBackEndApplicationTests {
    @Test
    void contextLoads() {
        HybridIdGenerator hybridIdGenerator = HybridIdGenerator.getInstance();
        SecondaryHybridIdGenerator secondaryHybridIdGenerator = new SecondaryHybridIdGenerator();

        hybridIdGenerator.generateId();
        secondaryHybridIdGenerator.generateId();

        UserIdUtil.OrderedUserIds
                orderedUserIds = UserIdUtil.
                orderUserIds(
                        "56e4247d-8c0c-40c0-9bc5-a8eda6053da11b53ffdf6c2c48de97798c8d5bcfa5a201944072002210791425",
                        "20eca8c7-2fdb-484d-9cb7-1ee6fd84402586d33efc841241ecbbc313431f8d5ce201942988393504522241");
        orderedUserIds.userOneId();
        orderedUserIds.userTwoId();
    }

    /*@Test
    void contextUUID() {
        // id？
        System.out.println("UUID  :\n"+ UUID.randomUUID());
        System.out.println("UUID 1:\n"+ UUID.randomUUID());
        System.out.println("UUID 2:\n"+ UUID.randomUUID());
        System.out.println("UUID 3:\n"+ UUID.randomUUID());
        System.out.println("UUID 4:\n"+ UUID.randomUUID());
        System.out.println("UUID 5:\n"+ UUID.randomUUID());
        System.out.println("UUID 6:\n"+ UUID.randomUUID());
        System.out.println("UUID 7:\n"+ UUID.randomUUID());
        System.out.println("UUID 8:\n"+ UUID.randomUUID());
        System.out.println("UUID 9:\n"+ UUID.randomUUID());
        System.out.println("UUID A:\n"+ UUID.randomUUID());
        System.out.println("UUID B:\n"+ UUID.randomUUID());
        System.out.println("UUID C:\n"+ UUID.randomUUID());
        System.out.println("UUID D:\n"+ UUID.randomUUID());
        System.out.println("UUID E:\n"+ UUID.randomUUID());
        System.out.println("UUID F:\n"+ UUID.randomUUID());
        System.out.println("UUID G:\n"+ UUID.randomUUID());
        System.out.println("UUID H:\n"+ UUID.randomUUID());
        System.out.println("UUID I:\n"+ UUID.randomUUID());
    }

    @Test
    void PWD(){
        // 密码
        System.out.println("admin:\n"+new BCryptPasswordEncoder().encode("admin"));
        // 密码 - 教师
        System.out.println("teacher:\n"+new BCryptPasswordEncoder().encode("teacher"));
        // 密码 - 学生
        System.out.println("student:\n"+new BCryptPasswordEncoder().encode("student"));
    }*/

}
