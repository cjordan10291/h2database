/*
 * Copyright 2004-2008 H2 Group. Multiple-Licensed under the H2 License, 
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.test.db;

import java.sql.Connection;
import java.sql.SQLException;

import org.h2.constant.ErrorCode;
import org.h2.test.TestBase;

/**
 * Test the deadlock detection mechanism.
 */
public class TestDeadlock extends TestBase {

    Connection c1, c2, c3;
    volatile SQLException lastException;
    
    public void test() throws Exception {
        deleteDb("deadlock");
        
        testDiningPhilosophers();
        testLockUpgrade();
        testThreePhilosophers();
        testNoDeadlock();
    }
    
    void init() throws Exception {
        c1 = getConnection("deadlock");
        c2 = getConnection("deadlock");
        c3 = getConnection("deadlock");
        c1.createStatement().execute("SET LOCK_TIMEOUT 1000000");
        c2.createStatement().execute("SET LOCK_TIMEOUT 1000000");
        c3.createStatement().execute("SET LOCK_TIMEOUT 1000000");
        c1.setAutoCommit(false);
        c2.setAutoCommit(false);
        c3.setAutoCommit(false);
        lastException = null;
    }
    
    void end() throws SQLException {
        c1.close();
        c2.close();
        c3.close();
    }
    
    /**
     * This class wraps exception handling to simplify creating small threads
     * that execute a statement.
     */
    abstract class DoIt extends Thread {
        abstract void execute() throws SQLException;
        public void run() {
            try {
                execute();
            } catch (SQLException e) {
                catchDeadlock(e);
            }
        }
    }
    
    void catchDeadlock(SQLException e) {
        if (lastException != null) {
            lastException.setNextException(e);
        } else {
            lastException = e;
        }
    }
    
    void testNoDeadlock() throws Exception {
        init();
        c1.createStatement().execute("CREATE TABLE TEST_A(ID INT PRIMARY KEY)");
        c1.createStatement().execute("CREATE TABLE TEST_B(ID INT PRIMARY KEY)");
        c1.createStatement().execute("CREATE TABLE TEST_C(ID INT PRIMARY KEY)");
        c1.commit();
        c1.createStatement().execute("INSERT INTO TEST_A VALUES(1)");
        c2.createStatement().execute("INSERT INTO TEST_B VALUES(1)");
        c3.createStatement().execute("INSERT INTO TEST_C VALUES(1)");
        DoIt t2 = new DoIt() {
            public void execute() throws SQLException {
                c1.createStatement().execute("DELETE FROM TEST_B");
                c1.commit();
            }
        };
        t2.start();
        DoIt t3 = new DoIt() {
            public void execute() throws SQLException {
                c2.createStatement().execute("DELETE FROM TEST_C");
                c2.commit();
            }
        };
        t3.start();
        Thread.sleep(500);
        try {
            c3.createStatement().execute("DELETE FROM TEST_C");
            c3.commit();
        } catch (SQLException e) {
            catchDeadlock(e);
        }
        t2.join();
        t3.join();
        if (lastException != null) {
            throw lastException;
        }
        c1.commit();
        c2.commit();
        c3.commit();
        c1.createStatement().execute("DROP TABLE TEST_A, TEST_B, TEST_C");
        end();

    }
    
    void testThreePhilosophers() throws Exception {
        if (config.mvcc) {
            return;
        }
        init();
        c1.createStatement().execute("CREATE TABLE TEST_A(ID INT PRIMARY KEY)");
        c1.createStatement().execute("CREATE TABLE TEST_B(ID INT PRIMARY KEY)");
        c1.createStatement().execute("CREATE TABLE TEST_C(ID INT PRIMARY KEY)");
        c1.commit();
        c1.createStatement().execute("INSERT INTO TEST_A VALUES(1)");
        c2.createStatement().execute("INSERT INTO TEST_B VALUES(1)");
        c3.createStatement().execute("INSERT INTO TEST_C VALUES(1)");
        DoIt t2 = new DoIt() {
            public void execute() throws SQLException {
                c1.createStatement().execute("DELETE FROM TEST_B");
                c1.commit();
            }
        };
        t2.start();
        DoIt t3 = new DoIt() {
            public void execute() throws SQLException {
                c2.createStatement().execute("DELETE FROM TEST_C");
                c2.commit();
            }
        };
        t3.start();
        try {
            c3.createStatement().execute("DELETE FROM TEST_A");
            c3.commit();
        } catch (SQLException e) {
            catchDeadlock(e);
        }
        t2.join();
        t3.join();
        checkDeadlock();
        c1.commit();
        c2.commit();
        c3.commit();
        c1.createStatement().execute("DROP TABLE TEST_A, TEST_B, TEST_C");
        end();
    }

    void testLockUpgrade() throws Exception {
        if (config.mvcc) {
            return;
        }
        init();
        c1.createStatement().execute("CREATE TABLE TEST(ID INT PRIMARY KEY)");
        c1.createStatement().execute("INSERT INTO TEST VALUES(1)");
        c1.commit();
        c1.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        c2.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        c1.createStatement().executeQuery("SELECT * FROM TEST");
        c2.createStatement().executeQuery("SELECT * FROM TEST");
        Thread t1 = new DoIt() {
            public void execute() throws SQLException {
                c1.createStatement().execute("DELETE FROM TEST");
                c1.commit();
            }
        };
        t1.start();
        try {
            c2.createStatement().execute("DELETE FROM TEST");
            c2.commit();
        } catch (SQLException e) {
            catchDeadlock(e);
        }
        t1.join();
        checkDeadlock();
        c1.commit();
        c2.commit();
        c1.createStatement().execute("DROP TABLE TEST");
        end();
    }
    
    void testDiningPhilosophers() throws Exception {
        if (config.mvcc) {
            return;
        }
        init();
        c1.createStatement().execute("CREATE TABLE T1(ID INT)");
        c1.createStatement().execute("CREATE TABLE T2(ID INT)");
        c1.createStatement().execute("INSERT INTO T1 VALUES(1)");
        c2.createStatement().execute("INSERT INTO T2 VALUES(1)");
        DoIt t1 = new DoIt() {
            public void execute() throws SQLException {
                c1.createStatement().execute("INSERT INTO T2 VALUES(2)");
                c1.commit();
            }
        };
        t1.start();
        try {
            c2.createStatement().execute("INSERT INTO T1 VALUES(2)");
        } catch (SQLException e) {
            catchDeadlock(e);
        }
        t1.join();
        checkDeadlock();
        c1.commit();
        c2.commit();
        c1.createStatement().execute("DROP TABLE T1, T2");
        end();
    }
    
    private void checkDeadlock() throws Exception {
        assertTrue(lastException != null);
        assertKnownException(lastException);
        assertEquals(ErrorCode.DEADLOCK_1, lastException.getErrorCode());
        SQLException e2 = lastException.getNextException();
        if (e2 != null) {
            // we have two exception, but there may only be one
            throw e2;
        }
    }

}
