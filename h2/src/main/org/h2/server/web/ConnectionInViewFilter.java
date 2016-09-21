package org.h2.server.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;




public class ConnectionInViewFilter implements Filter {

	private static final String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
	
	private static final String DB_URL = "jdbc:mysql://10.3.188.85:3306/cf_03fc7b21_27cf_4bb5_b7fe_d8ce98e25bb8?user=uPKhp11qR6KI2cya&password=B7YZaqrpFGMl057L";
	
	private static final String DB_USER = "uPKhp11qR6KI2cya";
	
	private static final String DB_PASS = "B7YZaqrpFGMl057L";
	
	private FilterConfig filterConfig = null;

	private static ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<Connection>();
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {

		HttpServletRequest httpServletRequest = (HttpServletRequest) request;

		try {
			Class.forName(DRIVER_CLASS_NAME);
			
			Connection connection = DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
			connection.setAutoCommit(true);

			connectionThreadLocal.set(connection);
			
			filterChain.doFilter(request, response);

			try {
				System.out.println(httpServletRequest.getRequestURI() + "Closing connection for thread:" + Thread.currentThread().getName() + ", " + connection);

				connection.close();

			} catch (Exception e) {
				// Log and fall through.
				System.out.println("Error closing the database connection for connection:" + connection);
				e.printStackTrace(System.out);
			}

		} catch (Exception e) {
			System.out.println("Error setting up database connection");
			e.printStackTrace(System.out);
			throw new RuntimeException(
					"Error setting up database connection", e);
		}
	}

	@Override
	public void destroy() {
		this.filterConfig = null;
	}

	public static Connection getConnection() {
		return null;
	}
	
}