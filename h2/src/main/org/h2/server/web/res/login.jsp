<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<!--
Copyright 2004-2014 H2 Group. Multiple-Licensed under the MPL 2.0,
and the EPL 1.0 (http://h2database.com/html/license.html).
Initial Developer: H2 Group
-->
<html><head>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=0.9" />
    <title>${text.a.title}</title>
    <link rel="stylesheet" type="text/css" href="stylesheet.css" />
    <script type="text/javascript">
        if (self.name == 'h2result' || self.name == 'h2query' || self.name == 'h2menu') {
            parent.location = "login.jsp";
        }
    </script>
</head>
<body style="margin: 20px" onload="krogerDbGuiOnLoad()">

	<script>
	
		function krogerDbGuiOnLoad() {
			var connectionId = document.getElementById("krogerDbGuiConnectionSelect").value;
			if (connectionId) {
				var password = krogerDbGuiReadCookie("connection" + connectionId + "pass");
				document.getElementById("krogerDbGuiConnectionPassword").value = password;
				
				document.getElementById("krogerDbGuiSaveUpdateButton").value = "${text.login.update}";
				
			} else {

				document.getElementById("krogerDbGuiSaveUpdateButton").value = "${text.login.save}";
			}
		}
	
		function krogerDbGuiReadCookie(name) {
		    var nameEQ = name + "=";
		    var ca = document.cookie.split(';');
		    for(var i=0;i < ca.length;i++) {
		        var c = ca[i];
		        while (c.charAt(0)==' ') c = c.substring(1,c.length);
		        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
		    }
		    return null;
		}
		
		function krogerDbGuiOnConnect() {
			var connectionId = document.getElementById("krogerDbGuiConnectionSelect").value;
			var password = document.getElementById("krogerDbGuiConnectionPassword").value;
			
			var now = new Date();
			var oneYearFromNow = new Date();
			oneYearFromNow.setFullYear(now.getFullYear() + 1);
			
			document.cookie = "connection" + connectionId + "pass=" + password + "; expires=" + oneYearFromNow.toUTCString();
		}
		
		
		function krogerDbGuiOnNew() {
			document.location='index.do?jsessionid=${sessionId}&amp;connectionInfoId=';
		}
	</script>

	<div style="text-align:center;">

    <form name="login" method="post" action="login.do?jsessionid=${sessionId}" id="login">


    

    <input type="hidden" name="euid" value="${euid }"/>
    
    <p>                    <select name="language" size="1"
                        onchange="javascript:document.location='index.do?jsessionid=${sessionId}&amp;language='+login.language.value;"
                    >
                    ${languageCombo}
                    </select>
    &nbsp;&nbsp;
    &nbsp;&nbsp; <a href="help.jsp?jsessionid=${sessionId}">${text.a.help}</a>
    </p>
        <table class="login" cellspacing="0" cellpadding="0">
            <tr class="login">
                <th class="login">${text.login.login}</th>
                <th class="login"></th>
            </tr>
            <tr><td  class="login" colspan="2"></td></tr>
            <tr class="login">
                <td class="login">${text.login.savedSetting}:</td>
                <td class="login" nowrap style="vertical-align:top;">
                    <select name="connectionInfoId" size="1" id="krogerDbGuiConnectionSelect"
                        style="width:300px"
                        onchange="javascript:document.location='index.do?jsessionid=${sessionId}&amp;connectionInfoId='+login.connectionInfoId.value;"
                    >
                    <option value="">--Choose--</option>
                    ${settingsList}
                    </select>
                    
                    <input type="button" class="button" value="New" id="krogerDbGuiNewConnectionButton" onclick="krogerDbGuiOnNew();"/>
                </td>
            </tr>
            <tr class="login">
                <td class="login">${text.login.settingName}:</td>
                <td class="login">
                    <input type="text" name="name" value="${name}" style="width:200px;" />
                    <input id="krogerDbGuiSaveUpdateButton" type="button" class="button" value="${text.login.save}" onclick="javascript:document.login.action='settingSave.do?jsessionid=${sessionId}';submit()" />
                    <input type="button" class="button" value="${text.login.remove}" onclick="javascript:if (confirm('Are you sure you want to delete this connection configuration?')){document.login.action='settingRemove.do?jsessionid=${sessionId}';submit();}" />
                </td>
            </tr>
            <tr class="login">
                <td class="login" colspan="2">
                    <hr />
                </td>
            </tr>
            <tr class="login">
                <td class="login">${text.login.driverClass}:</td>
                <td class="login">
                	<select name="driver" style="width:300px;">
                		<option value="">--Choose--</option>
                		${jdbcDriversList}
                	</select>
                
                </td>
            </tr>
            <tr class="login">
                <td class="login">${text.login.jdbcUrl}:</td>
                <td class="login"><input type="text" name="url" value="${url}" style="width:300px;" /></td>
            </tr>
            <tr class="login">
                <td class="login">${text.a.user}:</td>
                <td class="login"><input type="text" name="user" value="${user}" style="width:200px;" /></td>
            </tr>
            <tr class="login">
                <td class="login">${text.a.password}:</td>
                <td class="login"><input type="password" name="password" value="" style="width:200px;" id="krogerDbGuiConnectionPassword"/></td>
            </tr>
            <tr class="login">
                <td class="login"></td>
                <td class="login">
                    <input type="submit" class="button" value="${text.login.connect}" onclick="krogerDbGuiOnConnect()"/>
                    &nbsp;
                    <input type="button" class="button" value="${text.login.testConnection}" onclick="javascript:document.login.action='test.do?jsessionid=${sessionId}';submit()" />
                    <br />
                    <br />
                </td>
            </tr>
        </table>
        <br />
        <div id="url" style="display: none">
            <h2>H2 Database URLs</h2>
            <h3>Embedded</h3>
            <p>
            The URL <code>jdbc:h2:~/test</code> means the database is stored in
            the user home directory in files starting with 'test'.
            Absolute locations like <code>jdbc:h2:/data/db/test</code> are supported.
            In embedded mode, the database runs in the same process as the application.
            Only one process may access a database at any time.
            Databases are automatically created if they don't exist.
            <b>Warning</b>: if no path is used (for example jdbc:h2:test),
            then the database is stored in the current working directory
            (the directory where the application was started).
            URLs of the form jdbc:h2:data/test are relative to
            the current working directory. It is recommended to use locations relative to ~
            or absolute locations.
            </p>

            <h4>Remote (client/server)</h4>
            <p>
            The URL <code>jdbc:h2:tcp://localhost/~/test</code> means connect
            over TCP/IP to the H2 TCP server running on this computer, and open a database
            called test in the user home directory. The server must be started first.
            Any number of clients can connect to the same database.
            The same location rules as for embedded databases apply.
            </p>

            <h4>In-Memory</h4>
            <p>
            The URL <code>jdbc:h2:mem:test</code> means open an in-memory database
            named 'test'. Data is not persisted, and lost when the last connection to the database
            is closed. Multiple threads can access the same database, but data is only visible
            within the same process.
            </p>

            <p>
            For more information, see <a target="_blank" href="http://www.h2database.com/html/features.html#database_url">Database URL Overview</a>.
            </p>
        </div>
        <p class="error">${error}</p>
    </form>
    
    </div>
</body></html>