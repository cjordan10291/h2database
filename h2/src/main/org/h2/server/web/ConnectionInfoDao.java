package org.h2.server.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class ConnectionInfoDao {

	/**
	 * Keep in mind that the user's EUID might not be the same as the username
	 * they are trying to connect to a given db with.
	 * 
	 * @param euid
	 * @return
	 */
	public ArrayList<ConnectionInfo> getConnectionInfosForEuid(String euid) {

		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {

			ArrayList<ConnectionInfo> results = new ArrayList<ConnectionInfo>();

			Connection connection = ConnectionInViewFilter.getConnection();

			String sql = "select * from connection_info where euid = ?";
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, euid);

			rs = stmt.executeQuery();

			while (rs.next()) {
				ConnectionInfo connectionInfo = new ConnectionInfo();
				connectionInfo.driver = rs.getString("connection_driver");
				connectionInfo.url = rs.getString("connection_url");
				connectionInfo.user = rs.getString("connection_user");
				connectionInfo.name = rs.getString("connection_name");
				connectionInfo.id = rs.getLong("id");
				results.add(connectionInfo);
			}

			return results;
		} catch (Exception e) {
			throw new RuntimeException("Error getting connectionInfos for euid:" + euid, e);
		} finally {
			try {
				rs.close();
			} catch (Exception e) {
				// Just log it
				System.out.println("Error closing resultset!");
				e.printStackTrace(System.out);
			}

			try {
				stmt.close();
			} catch (Exception e) {
				// Just log it
				System.out.println("Error closing prepared statement!");
				e.printStackTrace(System.out);
			}
		}
	}

}
