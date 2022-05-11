/*
 *  JDBCDemo2.java
 *
 *  $Id$
 *
 *  Sample JDBC program
 *
 *  Copyright (C) 1999-2022 OpenLink Software.
 *  All Rights Reserved.
 *
 *  The copyright above and this notice must be preserved in all
 *  copies of this source code.  The copyright above does not
 *  evidence any actual or intended publication of this source code.
 *
 *  This is unpublished proprietary trade secret of OpenLink Software.
 *  This source code may not be copied, disclosed, distributed, demonstrated
 *  or licensed except as authorized by OpenLink Software.
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class JDBCDemo2 extends Frame implements Runnable, WindowListener {

    java.awt.TextArea textArea;
    java.awt.Button buttonQuery;
    java.awt.TextField textStatus;
    java.awt.Menu menuSQL;
    GridView gridView;

    java.sql.Statement stmt = null;
    java.sql.ResultSet result = null;
    java.sql.Connection conn = null;
    //  public String connectionURL="jdbc:openlink://localhost/SVT=SQLServer 7/Database=pubs/UID=sa/PWD=/";
    public String connectionURL = "jdbc:virtuoso://localhost:1111/TIMEOUT=5/UID=dba/PWD=dba/";
    public String sqlQuery = defQuery;
    static String defQuery = "SPARQL\n" +
            "    SELECT DISTINCT *\n" +
            "    WHERE { ?webid  a foaf:Person;\n" +
            "                    foaf:name ?name .\n" +
            "                    FILTER (isIRI(?webid))\n" +
            "                    FILTER (?name != '')\n" +
            "                    FILTER (! contains(str(?webid),\"uriburner\"))\n" +
            "                    FILTER (! contains(str(?webid),\"mates\"))\n" +
            "                     FILTER (! contains(str(?webid),\"urn:\"))\n" +
            "                  }\n" +
            " LIMIT  100";
//    static String defQuery = "sparql SELECT *  where {?s ?p ?o} limit 100";


    static int numThreads = 1;
    static int numThreadsInvoked = 1;
    int myThreadNum;
    String driverName;

    static String m_NewThread = "New Thread";
    static String m_SetConn = "Set Connection URL...";
    static String m_CloseConn = "Close Connection";
    static String m_Exit = "Exit";
    static String m_Next = "Next";
    static String m_Execute = "Execute SQL...";
    
    void Exit_Action() {
        dispose();      // free the system resources
        synchronized (this) {
            numThreads--;
        }
        if (numThreads == 0)
            System.exit(0); // close the application
    }

    void CloseConnection_Action() {
        textStatus.setText("Closing connection");
        try {
            if (conn != null) {
                stmt.close();
                stmt = null;
                conn.close();
                conn = null;
            }
            textStatus.setText("Not connected");
        } catch (Exception e) {
            textStatus.setText(e.toString());
        } finally {
            menuSQL.setEnabled(false);
        }
    }


    void OpenConnection_Exec() {
        if (conn!=null)
            CloseConnection_Action();

        textStatus.setText("Connecting to DBMS");
        try {
            if (conn != null) {
                stmt.close();
                stmt = null;
                conn.close();
                conn = null;
            }
            textStatus.setText("Connecting to DBMS");
            conn = DriverManager.getConnection(connectionURL);
            textStatus.setText("Connected to DBMS");
            menuSQL.setEnabled(true);
        } catch (Exception e) {
            textStatus.setText(e.toString());
        }
    }


    void OpenConnection_Action() {
        DialogConnection dlg = new DialogConnection(this, true, connectionURL, driverName, true);
        dlg.show();
        if (dlg.sDriver != null)
            loadDriver(dlg.sDriver);

        if (dlg.sURL != null) {
            connectionURL = dlg.sURL;
            textStatus.setText("Connection URL is  :" + connectionURL);
            OpenConnection_Exec();
        }
    }

    void EnterQuery_Action() {
        DialogQuery dlg = new DialogQuery(this, true, sqlQuery);
        dlg.show();
        if (dlg.sqlQuery != null) {
          sqlQuery = dlg.sqlQuery;
          ExecuteQuery_Action();
        }

    }


    void ExecuteQuery_Action() {
        try {
            ExecuteQuery();
        } catch (Exception e) {
            gridView.setHead(new String[] {""});
            gridView.addRow(new String[]{"Execution error:"});
            gridView.addRow(new String[]{e.toString()});
            gridView.update();
            gridView.repaint();
        }
    }


    void ExecuteQuery() throws Exception {
        if (conn==null) {
            textStatus.setText("Not connected");
            return;
        }

        try {
            if (stmt != null) {
                stmt.close();
                stmt = null;
            }
        } catch (Exception e) {}

        // Execute Query
        String status = textStatus.getText();
        try {
            textStatus.setText(status+" : Executing query...");
            Thread.sleep(500);
            stmt = conn.createStatement();
            stmt.execute(sqlQuery);
        } finally {
            textStatus.setText(status);
        }

        int rc = stmt.getUpdateCount();
        result = stmt.getResultSet();
        if (result!=null && rc==-1) {
            updateResultSet(result);
        } else {
            gridView.setHead(new String[] {""});
            gridView.addRow(new String[] {"Update count"});
            gridView.addRow(new String[]{""+rc});
        }
        gridView.update();
        gridView.repaint();
        super.show();
    }

    void updateResultSet(ResultSet rs) {
        if (rs == null) {
            gridView.setHead(new String[] {""});
            gridView.addRow(new String[] {"Result"});
            gridView.addRow(new String[]{"(empty)"});
            return;
        }

        try {
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            String headers[] = new String[cols];

            for (int i = 1; i <= cols; i++) {
                headers[i - 1] = md.getColumnLabel(i);
            }

            gridView.setHead(headers);

            while (rs.next()) {
                for (int i = 1; i <= cols; i++) {
                    headers[i - 1] = rs.getString(i);

                    if (rs.wasNull()) {
                        headers[i - 1] = "[NULL]";
                    }
                }

                gridView.addRow(headers);
            }

            rs.close();
        } catch (SQLException e) {
        }
    }


    void loadDriver(String driverName) {
        try {
            Class.forName(driverName);
        } catch (Exception e) {
            textStatus.setText(e.toString());
        }
        this.driverName = driverName;
    }

    public JDBCDemo2() {

        java.awt.MenuBar menuBar1 = new java.awt.MenuBar();
        java.awt.Menu menuFile = new java.awt.Menu("File");
        menuFile.add(m_NewThread);
        menuFile.add(m_SetConn);
        menuFile.add(m_CloseConn);
        menuFile.add(m_Exit);
        menuBar1.add(menuFile);
        menuFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                if (cmd.equals(m_NewThread)) {
                    JDBCDemo2 p = new JDBCDemo2();
                    new Thread(p).start();
                } else if (cmd.equals(m_SetConn)) {
                    OpenConnection_Action();
                } else if (cmd.equals(m_CloseConn)) {
                    CloseConnection_Action();
                } else if (cmd.equals(m_Exit)) {
                    Exit_Action();
                }
            }
        });

        menuSQL = new java.awt.Menu("SQL");
        menuSQL.add(m_Execute);
        menuBar1.add(menuSQL);
        menuSQL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                if (cmd.equals(m_Execute))
                    EnterQuery_Action();
            }
        });

        setMenuBar(menuBar1);
        menuSQL.setEnabled(false);

        setLayout(new BorderLayout(0, 5));
        addNotify();
        super.show();
        resize(insets().left + insets().right + 700, insets().top + insets().bottom + 525);
        setBackground(Color.LIGHT_GRAY);

        textStatus = new java.awt.TextField();
        textStatus.setEditable(false);
        textStatus.reshape(0, 62, 600, 30);
        textStatus.setFont(new Font("Dialog", Font.PLAIN, 10));
        add("South",textStatus);

        gridView = new GridView();
        add("Center", gridView);

        Panel panel = new java.awt.Panel();
        panel.setLayout(new BorderLayout());
        add("North", panel);


        gridView.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                GridView v = (GridView) e.getSource();
                if (v.isCurCellUri()) {
                    String s = v.getCurCellData();
                    if (s != null && Desktop.isDesktopSupported()) {
                        Desktop dsk = Desktop.getDesktop();
                        dsk.isSupported(Desktop.Action.BROWSE);
                        try {
                            dsk.browse(new URI(s));
                        } catch (Exception e1) {
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });


        setTitle("OpenLink JDBC Demo");
        textStatus.setText("Not connected");

        addWindowListener(this);
        loadDriver("virtuoso.jdbc4.Driver");
    }

    public void show() {
        move(20 * myThreadNum, 20 * myThreadNum);
        super.show();
    }

    static public void main(String args[]) {
        JDBCDemo2 jd = new JDBCDemo2();
        jd.show();
    }

    public void run() {
        synchronized (this) {
            numThreads++;
            numThreadsInvoked++;
            myThreadNum = numThreadsInvoked;
        }
        show();
    }

    @Override
    public void windowOpened(WindowEvent e) { }

    @Override
    public void windowClosing(WindowEvent e) {
        Exit_Action();
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}



