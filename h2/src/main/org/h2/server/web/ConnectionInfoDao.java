package org.h2.server.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.h2.util.StringUtils;

public class ConnectionInfoDao
{

    /**
     * Keep in mind that the user's EUID might not be the same as the username they are trying to
     * connect to a given db with.
     * 
     * @param euid
     * @return
     */
    public ArrayList<ConnectionInfo> getConnectionInfosForEuid(String euid)
    {

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {

            ArrayList<ConnectionInfo> results = new ArrayList<ConnectionInfo>();

            Connection connection = ConnectionInViewFilter.getConnection();

            String sql = "select * from connection_info where euid = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, euid);

            rs = stmt.executeQuery();

            while (rs.next())
            {
                ConnectionInfo connectionInfo = new ConnectionInfo();
                connectionInfo.euid = rs.getString("euid");
                connectionInfo.driver = rs.getString("connection_driver");
                connectionInfo.url = rs.getString("connection_url");
                connectionInfo.user = rs.getString("connection_user");
                connectionInfo.name = rs.getString("connection_name");
                connectionInfo.id = rs.getLong("id");
                results.add(connectionInfo);
            }

            return results;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error getting connectionInfos for euid:"
                + euid, e);
        }
        finally
        {
            try
            {
                rs.close();
            }
            catch (Exception e)
            {
                // Just log it
                System.out.println("Error closing resultset!");
                e.printStackTrace(System.out);
            }

            try
            {
                if (null != stmt)
                {
                    stmt.close();
                }

            }
            catch (Exception e)
            {
                // Just log it
                System.out.println("Error closing prepared statement!");
                e.printStackTrace(System.out);
            }
        }
    }

    private Long saveConnectionInfo(String euid, String connectionName, String connectionDriverName,
        String connectionUrl, String connectionUser)
    {
        Connection connection = ConnectionInViewFilter.getConnection();

        StringBuilder sqlSb = new StringBuilder(
            // "insert into connection_info (euid, connection_name, connection_driver,
            // connection_url, connection_user) Values (");
            "insert into connection_info (euid, connection_name, connection_driver, connection_url, connection_user) Values (?, ? , ?, ?, ?)");

        PreparedStatement stmt = null;
        
        Long result = null;

        try
        {
            stmt = connection.prepareStatement(sqlSb.toString(), Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, euid);
            stmt.setString(2, connectionName);
            stmt.setString(3, connectionDriverName);
            stmt.setString(4, connectionUrl);
            stmt.setString(5, connectionUser);
            
            stmt.execute();
            ResultSet keys = stmt.getGeneratedKeys();
            
            if (keys.next())
            {
                result =  keys.getLong(1);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error saving connectionInfos for euid:"
                + euid, e);
        }
        finally
        {
            try
            {
                if (null != stmt)
                {
                    stmt.close();
                }
            }
            catch (Exception e)
            {
                // Just log it
                System.out.println("Error closing prepared statement!");
                e.printStackTrace(System.out);
            }
        }
        
        return result;
    }
    
    
    private Long updateConnectionInfo(Long id, String euid, String connectionName, String connectionDriverName,
        String connectionUrl, String connectionUser)
    {
        
        ConnectionInfo connectionInfo = getConnectionInfoById(id);
        Long result = null;
        
        if (null == connectionInfo)
        {
            throw new RuntimeException("Connection info with id " + id + " was not found.  It may have already been removed.");
        }
            
        if (!StringUtils.equals(euid, connectionInfo.euid))
        {
            throw new RuntimeException("Connection Information with id of " + id + " is not owned by " + euid);
        }

        Connection connection = ConnectionInViewFilter.getConnection();

        StringBuilder sqlSb = new StringBuilder(
            // "insert into connection_info (euid, connection_name, connection_driver,
            // connection_url, connection_user) Values (");
            "insert into connection_info (euid, connection_name, connection_driver, connection_url, connection_user) Values (?, ? , ?, ?, ?)");

        PreparedStatement stmt = null;

        try
        {
            stmt = connection.prepareStatement(sqlSb.toString());
            stmt.setString(1, euid);
            stmt.setString(2, connectionName);
            stmt.setString(3, connectionDriverName);
            stmt.setString(4, connectionUrl);
            stmt.setString(5, connectionUser);
            
            stmt.execute();
            
            result = id;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error saving connectionInfos for euid:"
                + euid, e);
        }
        finally
        {
            try
            {
                if (null != stmt)
                {
                    stmt.close();
                }
            }
            catch (Exception e)
            {
                // Just log it
                System.out.println("Error closing prepared statement!");
                e.printStackTrace(System.out);
            }
        }
        
        return result;
    }



    public Long persistConnectionInfo(Long connectionId, String euid, String connectionName,
        String connectionDriverName, String connectionUrl, String connectionUser)
    {


        if (connectionId == null)
        {
            return saveConnectionInfo(euid, connectionName, connectionDriverName, connectionUrl,
                connectionUser);
        }
        else
        {
            return updateConnectionInfo(connectionId, euid, connectionName, connectionDriverName,
                connectionUrl, connectionUser);
        }


    }

    public void deleteConnectionInfo(Long connectionId, String euid)
    {
        
        if ( null == connectionId)
        {
            return;
        }
        ConnectionInfo info = getConnectionInfoById(connectionId);
        
        if (null == info )
        {
            throw new RuntimeException("Connection info with id of " + connectionId +  " does not exist, please refresh");
        }
        
        Connection connection = ConnectionInViewFilter.getConnection();
        PreparedStatement statement = null;
        
        String sql = "delete from connection_info where id = ?;";
        try {
            statement = connection.prepareStatement(sql);
            statement.setLong(1, connectionId);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error deleting connectionInfo with id:" + connectionId, e);
        }
        finally
        {
            try
            {
                if (null != statement)
                {
                    statement.close();
                }
            }
            catch (Exception e)
            {
                // Just log it
                System.out.println("Error closing prepared statement!");
                e.printStackTrace(System.out);
            }
        }
        
        return;
    }


    private void addStringParameter(StringBuilder sb, String stringParameter)
    {
        sb.append("'");
        sb.append(stringParameter);
        sb.append("'");
    }


    private void addAnotherStringParam(StringBuilder sb, String stringParameter)
    {
        sb.append(",");
        addStringParameter(sb, stringParameter);
    }


    public ConnectionInfo getConnectionInfoById(Long id)
    {
        PreparedStatement stmt=null;
        ResultSet rs = null;
        ConnectionInfo connectionInfo = null;
        try
        {
            Connection connection = ConnectionInViewFilter.getConnection();

            String sql = "select * from connection_info where id = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setLong(1, id);

            rs = stmt.executeQuery();
            if (rs.next())
            {
                connectionInfo = new ConnectionInfo();
                connectionInfo.euid = rs.getString("euid");
                connectionInfo.driver = rs.getString("connection_driver");
                connectionInfo.url = rs.getString("connection_url");
                connectionInfo.user = rs.getString("connection_user");
                connectionInfo.name = rs.getString("connection_name");
                connectionInfo.id = rs.getLong("id");
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error getting connectionInfos by id:"
                + id, e);
        }
        finally
        {
            try
            {
                rs.close();
            }
            catch (Exception e)
            {
                // Just log it
                System.out.println("Error closing resultset!");
                e.printStackTrace(System.out);
            }

            try
            {
                if (null != stmt)
                {
                    stmt.close();
                }

            }
            catch (Exception e)
            {
                // Just log it
                System.out.println("Error closing prepared statement!");
                e.printStackTrace(System.out);
            }
        }
        return connectionInfo;
    }

}
