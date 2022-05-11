/*
 *  ScrollDemo2.java
 *
 *  $Id$
 *
 *  Sample JDBC program
 *
 *  This file is part of the OpenLink Software Virtuoso Open-Source (VOS)
 *  project.
 *
 *  Copyright (C) 1998-2022 OpenLink Software
 *
 *  This project is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation; only version 2 of the License, dated June 1991.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 *
 *
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.*;


class ScrollDemo2 extends Frame implements Runnable, WindowListener {
    java.awt.TextField textQuery;
    java.awt.Panel Info;
    java.awt.TextField textField1;
    java.awt.TextField textPos;
    java.awt.TextField textStatus;

    java.sql.ResultSetMetaData meta = null;
    java.sql.Statement stmt = null;
    java.sql.ResultSet result = null;
    java.sql.Connection conn = null;
    java.awt.Label[] labelColumnName;
    java.awt.TextField[] textColumnValue;
    int bookmark = 0;
    int columnCount;
    public String connectionURL = "jdbc:virtuoso://localhost:1111/UID=dba/PWD=dba";
    String[][] data;
    static int numThreads = 1;
    static int numThreadsInvoked = 1;
    int myThreadNum;
    String driverName;

    static String m_NewThread = "New Thread";
    static String m_SetConn = "Set Connection URL...";
    static String m_CloseConn = "Close Connection";
    static String m_Exit = "Exit";

    static String m_First = "First";
    static String m_Previous = "Previous";
    static String m_Next = "Next";
    static String m_Last = "Last";
    static String m_Absolute = "Absolute";

    void buttonUpdate_Clicked() {
        textStatus.setText("Updating row");
        for (int c = 1; c <= columnCount; c++)
            try {
                result.updateString(c, textColumnValue[c - 1].getText());
            } catch (Exception e) {
                textStatus.setText(e.toString());
            }
        try {
            result.updateRow();
            updateInfo();
        } catch (Exception e) {
            textStatus.setText(e.toString());
        }
    }

    void buttonInsert_Clicked() {
        textStatus.setText("Inserting row");
        try {
            result.moveToInsertRow();
        } catch (Exception e) {
            textStatus.setText(e.toString());
            return;
        }
        for (int c = 1; c <= columnCount; c++)
            try {
                result.updateString(c, textColumnValue[c - 1].getText());
            } catch (Exception e) {
                textStatus.setText(e.toString());
            }
        try {
            result.insertRow();
            result.moveToCurrentRow();
            updateInfo();
        } catch (Exception e) {
            textStatus.setText(e.toString());
        }
    }

    void buttonAbsolute_Clicked() {
        int position = Integer.parseInt(textPos.getText());
        MoveToPos(position);
    }

    public void MoveToPos(int pos) {
        textStatus.setText("Going to position " + pos);
        try {
            if (result.absolute(pos))
                updateInfo();
        } catch (Exception e) {
            textStatus.setText(e.toString());
        }
    }

    void buttonRelative_Clicked() {
        int position = Integer.parseInt(textPos.getText());
        MoveRelative(position);
    }

    public void MoveRelative(int pos) {
        textStatus.setText("Going to relative position " + pos);
        try {
            if (result.relative(pos))
                updateInfo();
        } catch (Exception e) {
            textStatus.setText(e.toString());
        }
    }


    void buttonRefresh_Clicked() {
        textStatus.setText("Refreshing the current row");
        try {
            result.refreshRow();
            updateInfo();
        } catch (Exception e) {
            textStatus.setText(e.toString());
        }
    }

    void buttonDelete_Clicked() {
        textStatus.setText("Deleting the current row");
        try {
            result.deleteRow();
            updateInfo();
        } catch (Exception e) {
            textStatus.setText(e.toString());
        }
    }

    void buttonLast_Clicked() {
        textStatus.setText("Going to the last row");
        try {
            if (result.last())
                updateInfo();
        } catch (Exception e) {
            textStatus.setText(e.toString());
        }
    }

    void buttonFirst_Clicked() {
        textStatus.setText("Going to the first row");
        try {
            if (result.first())
                updateInfo();
        } catch (Exception e) {
            textStatus.setText(e.toString());
        }
    }

    void buttonPrevious_Clicked() {
        textStatus.setText("Going to the previous row");
        try {
            if (result.previous())
                updateInfo();
        } catch (Exception e) {
            textStatus.setText(e.toString());
        }
    }

    void buttonNext_Clicked() {
        textStatus.setText("Going to the next row");
        try {
            if (result.next())
                updateInfo();
        } catch (Exception e) {
            textStatus.setText(e.toString());
        }
    }

    void buttonQuery_Clicked() {
        try {
            ExecuteQuery(textQuery.getText());
        } catch (Exception e) {
            textStatus.setText(e.toString());
        }
    }


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
            textStatus.setText("Done.");
        } catch (Exception e) {
            textStatus.setText(e.toString());
        }
        conn = null;
    }


    void OpenConnection_Action() {
        DialogConnection dlg = new DialogConnection(this, true, connectionURL, driverName, true);
        dlg.show();
        if (dlg.sURL != null) {
            connectionURL = dlg.sURL;
            textStatus.setText("Connection URL is  :" + connectionURL);
        }
        if (dlg.sDriver != null)
            loadDriver(dlg.sDriver);
    }


    void ExecuteQuery(String query) throws Exception
    {
        if (conn != null) {
            stmt.close();
            stmt = null;
            conn.close();
            conn = null;
        }
        textStatus.setText("Connecting to :" + connectionURL);

        conn = DriverManager.getConnection(connectionURL);

        // Execute Query
        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        textStatus.setText("Executing query");
        result = stmt.executeQuery(query);
        // Get Resultset information
        meta = result.getMetaData();
        columnCount = meta.getColumnCount();
        labelColumnName = new Label[columnCount];
        textColumnValue = new TextField[columnCount];
        data = new String[columnCount][1];
        Info.removeAll();
        GridBagLayout gbl = new GridBagLayout();
        Info.setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;

        for (int c = 1; c <= columnCount; c++) {
            //data[c-1][0] = new String();
            //result.bindColumn(c,data[c-1]);
            labelColumnName[c - 1] = new Label(meta.getColumnName(c), Label.RIGHT);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridwidth = 1;
            gbc.weightx = 1;
            gbl.setConstraints(labelColumnName[c - 1], gbc);
            Info.add(labelColumnName[c - 1]);
            textColumnValue[c - 1] = new TextField();
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 3;
            gbl.setConstraints(textColumnValue[c - 1], gbc);
            Info.add(textColumnValue[c - 1]);
        }
        result.next();
        updateInfo();
        super.show();
    }

    void updateInfo() {
        textStatus.setText("Fetching result");
        try {
            for (int c = 1; c <= columnCount; c++) {
                textColumnValue[c - 1].setText(result.getString(c));
            }
            textStatus.setText("Done.");
        } catch (Exception e) {
            textStatus.setText(e.toString());
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


    public ScrollDemo2() {
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
                    ScrollDemo2 p = new ScrollDemo2();
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

        java.awt.Menu menu1 = new java.awt.Menu("Go To");
        menu1.add(m_First);
        menu1.add(m_Previous);
        menu1.add(m_Next);
        menu1.add(m_Last);
        menu1.add(m_Absolute);
        menuBar1.add(menu1);
        menu1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                if (cmd.equals(m_First))
                    buttonFirst_Clicked();
                else if (cmd.equals(m_Previous))
                    buttonPrevious_Clicked();
                else if (cmd.equals(m_Next))
                    buttonNext_Clicked();
                else if (cmd.equals(m_Last))
                    buttonLast_Clicked();
                else if (cmd.equals(m_Absolute))
                    buttonAbsolute_Clicked();
            }
        });
        setMenuBar(menuBar1);

        setLayout(new BorderLayout(0, 5));
        addNotify();
        resize(insets().left + insets().right + 600, insets().top + insets().bottom + 325);
        setBackground(new Color(12632256));
        java.awt.Panel panel3 = new java.awt.Panel();
        panel3.setLayout(new BorderLayout(5, 10));
        panel3.reshape(insets().left + 0, insets().top + 0, 600, 21);
        panel3.setBackground(new Color(12632256));
        add("North", panel3);
        textQuery = new java.awt.TextField();
        textQuery.setText("SELECT * FROM \"Customers\"");
        textQuery.reshape(0, 0, 600, 21);
        textQuery.setBackground(new Color(16777215));
        panel3.add("Center", textQuery);
        java.awt.Button buttonQuery = new java.awt.Button("Query");
        buttonQuery.reshape(552, 0, 48, 21);
        panel3.add("East", buttonQuery);
        buttonQuery.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonQuery_Clicked();
            }
        });


        Info = new java.awt.Panel();
        GridBagLayout gridBagLayout;
        gridBagLayout = new GridBagLayout();
        Info.setLayout(gridBagLayout);
        Info.reshape(insets().left + 0, insets().top + 21, 600, 196);
        Info.setBackground(new Color(12632256));
        add("Center", Info);
        java.awt.Label label1 = new java.awt.Label("", Label.RIGHT);
        label1.reshape(141, 95, 14, 21);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 0);
        gridBagLayout.setConstraints(label1, gbc);
        Info.add(label1);
        textField1 = new java.awt.TextField();
        textField1.reshape(438, 95, 20, 21);
        gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 0);
        gridBagLayout.setConstraints(textField1, gbc);
        Info.add(textField1);
        java.awt.Panel panel4 = new java.awt.Panel();
        panel4.setLayout(new GridLayout(0, 1, 0, 1));
        panel4.reshape(insets().left + 0, insets().top + 233, 600, 92);
        add("South", panel4);
        java.awt.Panel Toolbar = new java.awt.Panel();
        Toolbar.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 1));
        Toolbar.reshape(0, 0, 600, 30);
        Toolbar.setBackground(new Color(12632256));
        panel4.add(Toolbar);
        java.awt.Panel panel1 = new java.awt.Panel();
        panel1.setLayout(new GridLayout(1, 0, 2, 0));
        panel1.reshape(56, 5, 178, 21);
        Toolbar.add(panel1);

        java.awt.Button buttonFirst = new java.awt.Button("  First  ");
        buttonFirst.reshape(0, 0, 43, 21);
        buttonFirst.setFont(new Font("Dialog", Font.PLAIN, 10));
        panel1.add(buttonFirst);
        buttonFirst.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonFirst_Clicked();
            }
        });

        java.awt.Button buttonPrevious = new java.awt.Button("Previous");
        buttonPrevious.reshape(45, 0, 43, 21);
        buttonPrevious.setFont(new Font("Dialog", Font.PLAIN, 10));
        panel1.add(buttonPrevious);
        buttonPrevious.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonPrevious_Clicked();
            }
        });

        java.awt.Button buttonNext = new java.awt.Button("Next");
        buttonNext.reshape(90, 0, 43, 21);
        buttonNext.setFont(new Font("Dialog", Font.PLAIN, 10));
        panel1.add(buttonNext);
        buttonNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonNext_Clicked();
            }
        });

        java.awt.Button buttonLast = new java.awt.Button("Last");
        buttonLast.reshape(135, 0, 43, 21);
        buttonLast.setFont(new Font("Dialog", Font.PLAIN, 10));
        panel1.add(buttonLast);
        buttonLast.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonLast_Clicked();
            }
        });

        java.awt.Panel panel2 = new java.awt.Panel();
        panel2.setLayout(new GridLayout(1, 0, 2, 0));
        panel2.reshape(239, 5, 304, 21);
        Toolbar.add(panel2);
        java.awt.Button buttonDelete = new java.awt.Button("Delete");
        buttonDelete.reshape(0, 0, 49, 21);
        buttonDelete.setFont(new Font("Dialog", Font.PLAIN, 10));
        panel2.add(buttonDelete);
        buttonDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonDelete_Clicked();
            }
        });

        java.awt.Button buttonRefresh = new java.awt.Button("Refresh");
        buttonRefresh.reshape(51, 0, 49, 21);
        buttonRefresh.setFont(new Font("Dialog", Font.PLAIN, 10));
        panel2.add(buttonRefresh);
        buttonRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonRefresh_Clicked();
            }
        });

        java.awt.Button buttonInsert = new java.awt.Button("Insert");
        buttonInsert.reshape(204, 0, 49, 21);
        buttonInsert.setFont(new Font("Dialog", Font.PLAIN, 10));
        panel2.add(buttonInsert);
        buttonInsert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonInsert_Clicked();
            }
        });

        java.awt.Button buttonUpdate = new java.awt.Button("Update");
        buttonUpdate.reshape(255, 0, 49, 21);
        buttonUpdate.setFont(new Font("Dialog", Font.PLAIN, 10));
        panel2.add(buttonUpdate);
        buttonUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonUpdate_Clicked();
            }
        });

        java.awt.Panel panel5 = new java.awt.Panel();
        panel5.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 0));
        panel5.reshape(0, 31, 600, 30);
        panel4.add(panel5);
        java.awt.Button buttonAbsolute = new java.awt.Button("Absolute");
        buttonAbsolute.reshape(262, 5, 44, 21);
        buttonAbsolute.setFont(new Font("Dialog", Font.PLAIN, 12));
        panel5.add(buttonAbsolute);
        buttonAbsolute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonAbsolute_Clicked();
            }
        });

        java.awt.Button buttonRelative = new java.awt.Button("Relative");
        buttonRelative.reshape(348, 5, 44, 21);
        buttonRelative.setFont(new Font("Dialog", Font.PLAIN, 12));
        panel5.add(buttonRelative);
        buttonRelative.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonRelative_Clicked();
            }
        });

        textPos = new java.awt.TextField();
        textPos.setText("1");
        textPos.reshape(397, 5, 28, 21);
        panel5.add(textPos);
        textStatus = new java.awt.TextField();
        textStatus.setEditable(false);
        textStatus.reshape(0, 62, 600, 30);
        textStatus.setFont(new Font("Dialog", Font.PLAIN, 10));
        panel4.add(textStatus);
        setTitle("OpenLink JDBC 2.0 Scrollable Cursor Demo");

        Info.remove(label1);
        Info.remove(textField1);

        addWindowListener(this);
        loadDriver("virtuoso.jdbc4.Driver");
    }

    public void show() {
        move(20 * myThreadNum, 20 * myThreadNum);
        super.show();
    }

    public String MakeConnectURL(String inurl, String inkey, String invalue) {
        int inkeypos, endpos;

        inkeypos = inurl.indexOf(inkey + "=", 0);
        if (inkeypos < 0) {
            return inurl + "/" + inkey + "=" + invalue;
        }
        endpos = inurl.indexOf("/", inkeypos);
        if (endpos < 0) {
            return inurl.substring(0, inkeypos) + inkey + "=" + invalue;
        }

        return inurl.substring(0, inkeypos) + inkey + "=" + invalue + inurl.substring(endpos);
    }



    static public void main(String args[])
            throws Exception {
        ScrollDemo2 sd = new ScrollDemo2();
        if (args.length > 0)
            sd.connectionURL = args[0];
        sd.show();
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
    public void windowOpened(WindowEvent e) {
    }

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
