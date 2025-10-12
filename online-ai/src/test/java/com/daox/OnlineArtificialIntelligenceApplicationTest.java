package com.daox;

import com.daox.ai.utils.HybridIdGenerator;
import com.mongodb.internal.VisibleForTesting;
import jakarta.annotation.Resource;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple OnlineArtificialIntelligenceApplication.
 */
public class OnlineArtificialIntelligenceApplicationTest
        extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public OnlineArtificialIntelligenceApplicationTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(OnlineArtificialIntelligenceApplicationTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        assertTrue(true);
    }

    public void testMain() {

        HybridIdGenerator hybridIdGenerator = HybridIdGenerator.getInstance();

        // id？
        System.out.println("hybridIdGenerator:\n" + hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator 1:\n" + hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator 2:\n" + hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator 3:\n" + hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator 4:\n" + hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator 5:\n" + hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator 6:\n" + hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator 7:\n" + hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator 8:\n" + hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator 9:\n" + hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator A:\n" + hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator B:\n" + hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator C:\n" + hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator D:\n" + hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator E:\n" + hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator F:\n" + hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator G:\n" + hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator H:\n" + hybridIdGenerator.generateId());
        System.out.println("hybridIdGenerator I:\n" + hybridIdGenerator.generateId());

        System.out.println();
    }
}
