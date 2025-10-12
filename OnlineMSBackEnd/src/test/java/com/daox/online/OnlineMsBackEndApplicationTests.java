package com.daox.online;

import com.daox.online.uilts.HybridIdGenerator;
import com.daox.online.uilts.SecondaryHybridIdGenerator;
import com.daox.online.uilts.UserIdUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

@SpringBootTest(
        classes = {HybridIdGenerator.class, SecondaryHybridIdGenerator.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OnlineMsBackEndApplicationTests {

    @Resource
    private SecondaryHybridIdGenerator SecondaryHybridIdGenerator;

    @Resource
    private HybridIdGenerator hybridIdGenerator;
    @Test
    void contextLoads() {
        // id？
        System.out.println("hybridIdGenerator:\n"+hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator 1:\n"+ hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator 2:\n"+ hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator 3:\n"+ hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator 4:\n"+ hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator 5:\n"+ hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator 6:\n"+ hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator 7:\n"+ hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator 8:\n"+ hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator 9:\n"+ hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator A:\n"+ hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator B:\n"+ hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator C:\n"+ hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator D:\n"+ hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator E:\n"+ hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator F:\n"+ hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator G:\n"+ hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator H:\n"+ hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator I:\n"+ hybridIdGenerator.generateId());

        UserIdUtil.OrderedUserIds
                orderedUserIds = UserIdUtil.
                orderUserIds(
                        "56e4247d-8c0c-40c0-9bc5-a8eda6053da11b53ffdf6c2c48de97798c8d5bcfa5a201944072002210791425",
                        "20eca8c7-2fdb-484d-9cb7-1ee6fd84402586d33efc841241ecbbc313431f8d5ce201942988393504522241");
        System.out.println("One:"+orderedUserIds.userOneId());
        System.out.println("Two:"+orderedUserIds.userTwoId());
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