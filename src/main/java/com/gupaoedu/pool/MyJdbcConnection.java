package com.gupaoedu.pool;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author whz
 * @create 2018-07-03 6:35
 * @desc ww
 **/
public class MyJdbcConnection {
    private static String driverClass = "com.mysql.jdbc.Driver";

    private static String url = "jdbc:mysql://database.tony.com:3306/12306?useUnicode=true&characterEncoding=UTF-8";

    private static String userName = "tony";

    private static String password = "tony";

    static
    {
        try
        {
            //加载驱动
            Class.forName(driverClass);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private Connection connection;

    public MyJdbcConnection()
    {
        try
        {
            connection = DriverManager.getConnection(url, userName, password);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void close()
    {
        try
        {
            if ((connection != null) && (!connection.isClosed()))
            {
                connection.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}