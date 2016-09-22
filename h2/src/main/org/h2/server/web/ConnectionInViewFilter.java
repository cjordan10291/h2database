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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.h2.util.StringUtils;




public class ConnectionInViewFilter implements Filter {

	// DevCon db
//	private static final String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
//	private static final String DB_URL = "jdbc:mysql://10.3.188.85:3306/cf_03fc7b21_27cf_4bb5_b7fe_d8ce98e25bb8?user=uPKhp11qR6KI2cya&password=B7YZaqrpFGMl057L";
//	private static final String DB_USER = "uPKhp11qR6KI2cya";
//	private static final String DB_PASS = "B7YZaqrpFGMl057L";

	// Wireless1 test db
	private static final String DRIVER_CLASS_NAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	private static final String DB_URL = "jdbc:sqlserver://N060SQLT05.KROGER.COM:1675;databaseName=ISS_DGateway_Data";
	private static final String DB_USER = "svcDG";
	private static final String DB_PASS = "svsDgT46";

	
	private FilterConfig filterConfig = null;

	private static ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<Connection>();
	
	private static ThreadLocal<String> euidThreadLocal = new ThreadLocal<String>();
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {

		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		
		try {
			Class.forName(DRIVER_CLASS_NAME);
			
			Connection connection = DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
			connection.setAutoCommit(true);

			connectionThreadLocal.set(connection);
			
			String euidFromRequest = httpServletRequest.getParameter("euid");
			
            if (!StringUtils.isNullOrEmpty(euidFromRequest))
            {
            	System.out.println("Setting from querystring" + euidFromRequest);
            	
            	Cookie[] cookieArray = httpServletRequest.getCookies();

				System.out.println(cookieArray);
				boolean cookieSet = false;
				if (cookieArray != null) {
	    			for (Cookie cookie: cookieArray)
	    			{
	    			    if ( "euid".equals(cookie.getName()))
	    			    {
	    			    	cookie.setValue(euidFromRequest);
	    			    	cookieSet = true;
	    			    	break;
	    			    }
	    			        
	    			}
				}
            	
            	if (!cookieSet) {
                	Cookie aCookie = new Cookie("euid", euidFromRequest);
            		httpServletResponse.addCookie(aCookie);
            	}
            	euidThreadLocal.set(euidFromRequest);
            } else {

				Cookie[] cookieArray = httpServletRequest.getCookies();

				System.out.println(cookieArray);
				
				if (cookieArray != null) {
	    			for (Cookie cookie: cookieArray)
	    			{
	    			    if ( "euid".equals(cookie.getName()))
	    			    {
	    			    	System.out.println(cookie.getValue());
	    			    	if (!StringUtils.isNullOrEmpty(cookie.getValue())) {
		    			        euidThreadLocal.set(cookie.getValue());
		    			        break;
	    			    	}
	    			    }
	    			        
	    			}
				}
            }
			
            System.out.println("Did it find a euid?" + euidThreadLocal.get());
            
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
		return connectionThreadLocal.get();
	}
	
	public static String getEuid()
	{
	    return euidThreadLocal.get();
	}
	
}