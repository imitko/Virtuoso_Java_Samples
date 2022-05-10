/*   This program is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation; either version 2, or (at your option)
     any later version.

     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with this program; if not, write to the Free Software Foundation,
     Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. */

package Bench;

import java.sql.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import org.w3c.dom.*;


import javax.swing.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


//import org.apache.crimson.tree.*;

public class BenchPanel extends JPanel {

   String PACKAGE_NAME = "OpenLink JDBC Benchmark Utility";
   String PACKAGE_VERSION = "0.99.4";
   String PACKAGE_BUGREPORT = "jdbc-bench@openlinksw.com";

   boolean m_bUseFiles = false;
   Frame m_parentFrame = null;
   JTextArea m_logArea = new JTextArea(20,60);
   JScrollPane m_pane = null;

   // options
   public ConnectionPool pool = new ConnectionPool();
   JTable pool_table = new JTable(pool);
   LoginData results = null;

   static final String m_logLevels[] = {"Basic", "Verbose", "SQL debug"};

   // Dynamic
   final static int m_nMaxBranch = 10, m_nMaxTeller = 100, m_nMaxAccount = 1000;
   int m_nWarehouses,m_nDistricts,m_nItems,m_nCustomers,m_nOrders;
   int nWar,nDis,mIt,nCust,nOrd,nRd;
   RunThread thr = null;

   public Vector m_tpcaBench = new Vector();
   ThreadGroup m_tests = null;
   TPCCBench m_tpccBench[];
   Logger m_logger = null;
   String m_strFileName = null, m_strNowFunction = null;
   volatile int nbTrans = 0;
   volatile int nbThreads = 0;
   boolean isBenchWorked = false;

   // SQL statements
   //
   public String makeDropTable(String strTableName) {
      return "Drop table " + strTableName;
   }

   public String makeInsertAccount(String strTableName) {
      return "insert into " + strTableName + " (account, branch, balance, filler) values (?, ?, ?, ?)";
   }

   public String makeCreateIndex(String strIndexType, String strTableName, String strFields) {
      return "create " + (strIndexType == null ? "" : strIndexType) + " index " + strTableName + "ix on " + strTableName + "(" + strFields + ")";
   }

   public static final String strFiller = "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";

   public BenchPanel(String strDriver, String strURL, String strUserName,
      String strPassword, int nDriverIndex) throws Exception
   {
      doOpenConnection(strDriver,strURL,strUserName,strPassword,false);
      setDriver(nDriverIndex);
   }

   public void setLogger(Logger logger) {
      m_logger = logger;
   }


   public void setDriver(int nDriverIndex) throws Exception
   {
       int selection[] = pool_table.getSelectedRows();
       if (selection == null)
           return;
       for (int nRow = 0; nRow < selection.length; nRow++) {
           pool.getConnection(selection[nRow]).setDriver(nDriverIndex);
           pool.fireTableRowsUpdated(selection[nRow], selection[nRow]);
       }
   }

   public void closeConnection(boolean bResults) {
       if (bResults) {
           try {
               if (results != null) {
                   results.doLogout();
                   log("Results connection is closed\n",0);
                   results = null;
               }
           } catch(SQLException ec) {
               log("Error closing results connection : " + ec.getMessage() + "\n",0);
           }
       } else {
           try {
               pool.removeLoginsFromThePool(pool_table.getSelectedRows());
           } catch(SQLException e) {
               log("Error closing connection(s) : " + e.getMessage() + "\n",0);
           }
       }
   }

   void openConnection(boolean bResults) {
       try {
           if (bResults) {
               if (results != null) {
                   results.doLogout();
                   log("Results connection is closed\n",0);
               }
               results = null;

               LoginData data = new LoginData("Results");
               data.setDetails(this, "Enter RESULTS connection data", pool.getLastConnection());
               results = data;
               log("Results Connection to " + results.strURL + " opened\n",0);
           } else {
               int selection[] = pool_table.getSelectedRows();
               if (selection == null || selection.length != 1) {
                   log("Please select a single connection for this operation\n", 0);
                   return;
               }
               LoginData data = pool.getConnection(selection[0]);
               data.setDetails(this, "Enter connection data", pool.getLastConnection());
               log("Connection to " + data.strURL + " opened\n",0);
               pool_table.repaint();
           }
       } catch(Exception e) {
           if (e.getMessage() != "Canceled")
               JOptionPane.showMessageDialog(this, e.getMessage(), "Error connecting", JOptionPane.ERROR_MESSAGE);
       }
   }

    public void doOpenConnection(String strDriver, String strURL, String strUserName,
                                 String strPassword, boolean bResults) throws Exception
    {
        if (strDriver != null)
            DriverManager.registerDriver((java.sql.Driver)Class.forName(strDriver).newInstance());

        if (bResults) {
            if (results != null) {
                results.doLogout();
                results = null;
            }
            results = new LoginData(strURL, strUserName, strPassword);
            log("Connection to " + strURL + " opened\n",0);
        } else {
            pool.addLoginToThePool(strURL, strUserName, strPassword);
            log("Connection to " + strURL + " opened\n", 0);
        }
   }

   public BenchPanel(boolean bUseFiles, Frame parentFrame) throws Exception {

      super(new BorderLayout(10,10));
      m_bUseFiles = bUseFiles; m_parentFrame = parentFrame;
      m_logArea.setTabSize(10);
      m_logArea.setFont(new java.awt.Font("Monospaced",java.awt.Font.PLAIN,12));
      m_logArea.setEditable(false);
      setLogger(new TheLogger(m_logArea));
      JMenuBar bar = new JMenuBar();

      JMenu fileMenu = new JMenu("File");

      if(m_bUseFiles) {
         fileMenu.add(new AbstractAction("Open...") {
            public void actionPerformed(ActionEvent e) {
              JFileChooser chooser = new JFileChooser();
              chooser.setDialogTitle("Select file to open");
              SimpleFileFilter filter = new SimpleFileFilter("xml");
              chooser.setFileFilter(filter);
              chooser.setCurrentDirectory(new File("."));
              int returnVal = chooser.showOpenDialog(BenchPanel.this);
              if(returnVal == JFileChooser.APPROVE_OPTION)
                m_strFileName = chooser.getSelectedFile().getPath();
              else
                return;

              doLoadItems(m_strFileName, true);
            }
         });
      }

      fileMenu.add(new AbstractAction("Clear log") {
         public void actionPerformed(ActionEvent e) {
            m_logArea.setText("");
         }
      });

      if(m_bUseFiles) {
         fileMenu.add(new AbstractAction("Save as...") {
            public void actionPerformed(ActionEvent e) {
              JFileChooser chooser = new JFileChooser();
              chooser.setDialogTitle("Select output file name");
              SimpleFileFilter filter = new SimpleFileFilter("xml");
              chooser.setFileFilter(filter);
              chooser.setCurrentDirectory(new File("."));
              int returnVal = chooser.showSaveDialog(BenchPanel.this);
              if(returnVal == JFileChooser.APPROVE_OPTION) {
                String name = chooser.getSelectedFile().getPath();
                if (name.length() <= 4 || (name.length() > 4 && (! name.regionMatches(true, name.length() - 4, ".xml", 0, 4))))
                  m_strFileName = name+".xml";
                else
                  m_strFileName = name;
              } else
                 return;

               try {
                Results res = Results.InitItemsSaving(BenchPanel.this);
                res.SaveAllItems(pool);
                Document doc = res.DoneItemsSaving();
                saveXML(doc, m_strFileName);
              } catch (Exception ex) {
                log(ex.toString()+"\n", 0);
              }

            }
         });

         fileMenu.addSeparator();
         fileMenu.add(new AbstractAction("Exit") {
            public void actionPerformed(ActionEvent e) {
               closeConnection(true);
               System.exit(0);
            }
         });
      }
      bar.add(fileMenu);

       JMenu editMenu = new JMenu("Edit");
       editMenu.add(new AbstractAction("New Benchmark Item...") {
           public void actionPerformed(ActionEvent e) {
               Object val = JOptionPane.showInputDialog (BenchPanel.this,
                       "Name for the new TPC-A like test",
                       "New test",
                       JOptionPane.PLAIN_MESSAGE, null, null, "New Item");
               if (val != null && val.toString() != null) {
                   LoginData newLogin = new LoginData(val.toString());
                   pool.addLoginToThePool(newLogin);
                   pool_table.selectAll();
               }
           }
       });
       editMenu.add(new AbstractAction("Delete selected items...") {
           public void actionPerformed(ActionEvent e) {
               try {
                   int[] selection = pool_table.getSelectedRows();
                   if (selection != null)
                       pool.removeLoginsFromThePool(selection);
               } catch(SQLException ex) {
                   log("Error closing connection(s) : " + ex.getMessage() + "\n",0);
               }
           }
       });
       editMenu.add(new AbstractAction("Save selected item as...") {
           public void actionPerformed(ActionEvent e) {
               String fileName = null;
               JFileChooser chooser = new JFileChooser();
               chooser.setDialogTitle("Select output file name");
               chooser.setCurrentDirectory(new File("."));
               SimpleFileFilter filter = new SimpleFileFilter("xml");
               chooser.setFileFilter(filter);
               int returnVal = chooser.showSaveDialog(BenchPanel.this);
               if(returnVal == JFileChooser.APPROVE_OPTION) {
                   String name = chooser.getSelectedFile().getPath();
                   if (name.length() <= 4 || (name.length() > 4 && (! name.regionMatches(true, name.length() - 4, ".xml", 0, 4))))
                       fileName = name+".xml";
                   else
                       fileName = name;
               } else
                   return;

               try {
                   Results res = Results.InitItemsSaving(BenchPanel.this);
                   res.SaveSelectedItems(pool, pool_table.getSelectedRows());
                   Document doc = res.DoneItemsSaving();
                   saveXML(doc, fileName);
               } catch (Exception ex) {
                   log(ex.toString()+"\n", 0);
               }
           }
       });
       editMenu.addSeparator();
       editMenu.add(new AbstractAction("Login details...") {
           public void actionPerformed(ActionEvent e) {
               openConnection(false);
           }
       });
       editMenu.add(new AbstractAction("Table details...") {
           public void actionPerformed(ActionEvent e) {
               createTables();
           }
       });
       editMenu.add(new AbstractAction("Run details...") {
           public void actionPerformed(ActionEvent e) {
               runDetails();
           }
       });
       editMenu.addSeparator();
       editMenu.add(new AbstractAction("Insert file...") {
           public void actionPerformed(ActionEvent e) {
               String fileName = null;
               JFileChooser chooser = new JFileChooser();
               chooser.setDialogTitle("Select file to insert");
               SimpleFileFilter filter = new SimpleFileFilter("xml");
               chooser.setFileFilter(filter);
               chooser.setCurrentDirectory(new File("."));
               int returnVal = chooser.showOpenDialog(BenchPanel.this);
               if(returnVal == JFileChooser.APPROVE_OPTION)
                   fileName = chooser.getSelectedFile().getPath();
               else
                   return;

               doLoadItems(fileName, false);
           }
       });
       bar.add(editMenu);

       JMenu actionMenu = new JMenu("Action");
       actionMenu.add(new AbstractAction("Creata tables & procedures") {
           public void actionPerformed(ActionEvent e) {
               doCreateTables();
           }
       });
       actionMenu.add(new AbstractAction("Drop tables & procedures") {
           public void actionPerformed(ActionEvent e) {
               cleanUp();
           }
       });
       actionMenu.addSeparator();
       actionMenu.add(new AbstractAction("Run Selected") {
           public void actionPerformed(ActionEvent e) {
               runSelected();
           }
       });
       bar.add(actionMenu);


       JMenu resultsMenu = new JMenu("Results");

       resultsMenu.add(new AbstractAction("Connect") {
           public void actionPerformed(ActionEvent e) {
               openConnection(true);
           }
       });

       resultsMenu.add(new AbstractAction("Disconnect") {
           public void actionPerformed(ActionEvent e) {
               closeConnection(true);
           }
       });

       resultsMenu.addSeparator();
       resultsMenu.add(new AbstractAction("Create the table") {
           public void actionPerformed(ActionEvent e) {
               createResult();
           }
       });
       resultsMenu.add(new AbstractAction("Drop the table") {
           public void actionPerformed(ActionEvent e) {
               dropResult();
           }
       });

       bar.add(resultsMenu);

       JMenu prefsMenu = new JMenu("Preferences");
       prefsMenu.add(new AbstractAction("Display refresh rate...") {
           public void actionPerformed(ActionEvent e) {
               Object val = JOptionPane.showInputDialog (BenchPanel.this,
                       "Refresh txn count for the progress bars",
                       "Enter option",
                       JOptionPane.PLAIN_MESSAGE,
                       null, null,
                       String.valueOf(TPCABench.prop_update_freq));
               if (val != null && val.toString() != null)
               {
                   int n = Integer.valueOf(val.toString()).intValue();
                   if (n >= 1)
                       TPCABench.prop_update_freq = n;
               }
           }
       });
       bar.add(prefsMenu);


//      JMenu windowMenu = new JMenu("Window");
//
//      windowMenu.add(new AbstractAction("Win 1")      {
//         public void actionPerformed(ActionEvent e) {
//            openConnection(true);
//         }
//      });
//      bar.add(windowMenu);

       JMenu helpMenu = new JMenu("Help");
       helpMenu.add(new AbstractAction("Log level") {
           public void actionPerformed(ActionEvent e) {
               JComboBox logLevel = new JComboBox(m_logLevels);
               logLevel.setSelectedIndex(m_logger.getLogLevel());
               if(JOptionPane.OK_OPTION != JOptionPane.showOptionDialog(BenchPanel.this,logLevel,"Select Log Level",JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,null,null,null)) return;
               int nNewLogLevel = logLevel.getSelectedIndex();
               if(nNewLogLevel < 0) return;
               m_logger.setLogLevel(nNewLogLevel);
           }
       });
       helpMenu.add(new AbstractAction("About") {
           public void actionPerformed(ActionEvent e) {
               JPanel pane = new JPanel(new GridLayout(12,1,25,0));
               pane.add(new JLabel(PACKAGE_NAME + " v. " + PACKAGE_VERSION, SwingConstants.LEFT));
               pane.add(new JLabel("(C) 2000-2022 OpenLink Software", SwingConstants.LEFT));
               pane.add(new JLabel("Please report all bugs to <" + PACKAGE_BUGREPORT + ">" , SwingConstants.LEFT));
               pane.add(new JLabel(""));
               pane.add(new JLabel("This utility is released under the GNU General Public License (GPL)", SwingConstants.LEFT));
               pane.add(new JLabel(""));
               pane.add(new JLabel("Disclaimer: The benchmark in this application is loosely based", SwingConstants.LEFT));
               pane.add(new JLabel("on the TPC-A standard benchmark, but this application", SwingConstants.LEFT));
               pane.add(new JLabel("does not claim to be a full or precise implementation, nor are", SwingConstants.LEFT));
               pane.add(new JLabel("the results obtained by this application necessarily comparable", SwingConstants.LEFT));
               pane.add(new JLabel("to the vendor's published results.", SwingConstants.LEFT));

               JOptionPane.showMessageDialog(BenchPanel.this,pane,"About jdbc-bench",JOptionPane.INFORMATION_MESSAGE);
           }
       });
       bar.add(helpMenu);

       add(BorderLayout.NORTH,bar);
       JPanel center_pane = new JPanel(new GridLayout(2, 1, 0, 10));

       JScrollPane pool_pane = new JScrollPane(pool_table);
       center_pane.setBorder(BorderFactory.createEtchedBorder());
       center_pane.add(pool_pane);

       m_pane = new JScrollPane(m_logArea);
       m_pane.setBorder(BorderFactory.createEtchedBorder());
       center_pane.add(m_pane);

       add(BorderLayout.CENTER,center_pane);
   }

/*******************************************************************************/
/*******************************************************************************/
   boolean executeNoCheck(Statement stmt, String strSQL) {
      boolean bSuccess = false;
      try {
         log(strSQL + "\n",2);
         stmt.execute(strSQL);
         bSuccess = true;
      } catch(SQLException e) {
         bSuccess = false;
         log(e.getMessage() + "\n",1);
      }
      return bSuccess;
   }

   public void setMaxTableLimits(LoginData data, int nMaxBranch,
      int nMaxTeller, int nMaxAccount)
  {
      if (nMaxBranch > 0)
         data.tpca.nMaxBranch = nMaxBranch;
      if (nMaxTeller > 0)
         data.tpca.nMaxTeller = nMaxTeller;
      if (nMaxAccount > 0)
         data.tpca.nMaxAccount = nMaxAccount;
   }


  public void doLoadItems(String fileName, boolean clear) {
      ItemLoader res = new ItemLoader(BenchPanel.this);
      Vector lst = res.LoadItemsFrom(fileName);
      try{
          if (lst != null && clear) {
              for (int i = 0; i < pool.getRowCount(); i++)
                  pool.removeLoginFromThePool(i);
          }
          for (Enumeration i = lst.elements(); i.hasMoreElements(); ) {
              LoginData data = (LoginData)i.nextElement();
              if (data != null)
                  pool.addLoginToThePool(data);
          }
      } catch (Exception ex) {
          log("Exception : "+ ex + "\n", 0);
      }
      pool_table.repaint();
  }

   public void doCreateTables()
   {
       String strIndexDef;

       int selection[] = pool_table.getSelectedRows();
       if (selection == null || selection.length == 0) {
           log("Please select connections for this operation\n", 0);
           return;
       }

       for (int item = selection[0]; item < selection.length; item++) {
           LoginData data = pool.getConnection(item);
           if (data.conn == null) {
               log(data.strItemName +": Not connected\n",0);
               continue;
           }
           if(data.m_Driver == null) {
               log(data.strItemName +": Driver type is not specified\n",0);
               continue;
           }

           Statement stmt = null;
           long startTime = System.currentTimeMillis();
           try {
               String strSQL;
               stmt = data.conn.createStatement();
               // create tables
               if(data.tpca.bCreateBranch) {
                   log("Building table definition for " + data.m_Driver.getBranchName() + " ...",0);
                   //                executeNoCheck(stmt, makeDropTable(m_Driver.getBranchName()));
                   strSQL = data.m_Driver.makeCreateBranch(data.tpca.bCreateIndexes);
                   executeNoCheck(stmt, "drop table " + data.m_Driver.getBranchName());
                   log(strSQL + "\n",2);
                   stmt.execute(strSQL);
                   log("Done\n",0);
               }
               if(data.tpca.bCreateTeller) {
                   log("Building table definition for " + data.m_Driver.getTellerName() + " ...",0);
                   //                executeNoCheck(stmt, makeDropTable(m_Driver.getTellerName()));
                   strSQL = data.m_Driver.makeCreateTeller(data.tpca.bCreateIndexes);
                   executeNoCheck(stmt, "drop table " + data.m_Driver.getTellerName());
                   log(strSQL + "\n",2);
                   stmt.execute(strSQL);
                   log("Done\n",0);
               }
               if(data.tpca.bCreateAccount) {
                   log("Building table definition for " + data.m_Driver.getAccountName() + " ...",0);
                   //                executeNoCheck(stmt, makeDropTable(m_Driver.getAccountName()));
                   strSQL = data.m_Driver.makeCreateAccount(data.tpca.bCreateIndexes);
                   executeNoCheck(stmt, "drop table " + data.m_Driver.getAccountName());
                   log(strSQL + "\n",2);
                   stmt.execute(strSQL);
                   log("Done\n",0);
               }
               if(data.tpca.bCreateHistory) {
                   log("Building table definition for " + data.m_Driver.getHistoryName() + " ...",0);
                   //                executeNoCheck(stmt, makeDropTable(m_Driver.getHistoryName()));
                   strSQL = data.m_Driver.makeCreateHistory();
                   executeNoCheck(stmt, "drop table " + data.m_Driver.getHistoryName());
                   log(strSQL + "\n",2);
                   stmt.execute(strSQL);
                   log("Done\n",0);
               }

               try {
                   if (data.tpca.bCreateProcedures) {
                       log("\nAttempting to load the stored procedure text ...",0);
                       String sql = data.m_Driver.m_strDropProcedure;
                       if (sql != null)
                           executeNoCheck(stmt, sql);
                       data.m_Driver.setProcedure(data.conn);
                       log("Done\n",0);
                   }
               } catch(SQLException e) {
                   log("\nLoad Procedures error : " + e.getMessage() + "\n",0);
               }

               if(data.m_Driver.mustCreateIndex() || data.tpca.bCreateIndexes) {
                   log("\n",0);
                   // create indices
                   strIndexDef = makeCreateIndex(data.m_Driver.getIndexType(),
                           data.m_Driver.getBranchName(), "branch");
                   if(strIndexDef != null) {
                       log(strIndexDef + "\n",0);
                       stmt.execute(strIndexDef);
                   }
                   strIndexDef = makeCreateIndex(data.m_Driver.getIndexType(),
                           data.m_Driver.getTellerName(), "teller");
                   if(strIndexDef != null) {
                       log(strIndexDef + "\n",0);
                       stmt.execute(strIndexDef);
                   }
                   strIndexDef = makeCreateIndex(data.m_Driver.getIndexType(),
                           data.m_Driver.getAccountName(), "account");
                   if(strIndexDef != null) {
                       log(strIndexDef + "\n",0);
                       stmt.execute(strIndexDef);
                   }
               }
               log("\n",0);
               // load the tables
               if (data.tpca.bLoadBranch)
                   loadBranch(data, stmt);
               if (data.tpca.bLoadTeller)
                   loadTeller(data, stmt);
               if (data.tpca.bLoadAccount)
                   loadAccount(data, stmt);
           } catch(SQLException e) {
               log("\nCreate table error : " + e.getMessage() + "\n",0);
           } finally {
               if(stmt != null)
                   try { stmt.close(); }
                   catch(SQLException e) { }
               stmt = null;
           }

           long endTime = System.currentTimeMillis();
           Results.addResultsRecord(this, data, "TPC-A","Load tables/" + data.tpca.nMaxBranch
                           + "/" + data.tpca.nMaxTeller
                           + "/" + data.tpca.nMaxAccount,
                   -1, (endTime - startTime) / 1000, -1, -1, -1, -1,"OK","");
       }
   }



   void createTables() {
       int selection[] = pool_table.getSelectedRows();
       if (selection == null || selection.length != 1)
       {
           log("Please select a single connection for this operation\n", 0);
           return;
       }
       LoginData data = pool.getConnection(selection[0]);

       if(data.conn == null) {
           log("Not connected\n",0);
           return;
       }
       JDialog dlg = new TPCATableProps(null, "Table details | "+ data.strItemName, true, data);
       dlg.show();
       repaint();
   }


   void runSelected() {
       int selection[] = pool_table.getSelectedRows();
       if (selection == null || selection.length < 1)
       {
           log("Please select one or more connections for this operation\n", 0);
           return;
       }

       for(int i = 0; i < selection.length; i++)
           if (pool.getConnection(selection[i]).conn == null) {
               log("Not at all selected items are connected\n", 0);
               return;
           }

       RunSelected pane = new RunSelected();
       String options[] = { "Start", "Run All", "Cancel"};
       int Response = JOptionPane.showOptionDialog(this,pane,"Run Duration",
               JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
               options,options[0]);
       if (Response == JOptionPane.CANCEL_OPTION || Response == -1)
           return;

       int nEndTime = 0;

       try {
           nEndTime = Integer.valueOf(pane.nEndTime.getText()).intValue();
       } catch(Exception e) {
           nEndTime = 0;
       }

       String xml_name = pane.sOutputFile.getText();
       RunAllThread allThread;
       if (Response == JOptionPane.YES_OPTION)
           // Start
           allThread = new RunAllThread(selection, nEndTime, xml_name, false);
       else
           // RunAll
           allThread = new RunAllThread(selection, nEndTime, xml_name, true);

       allThread.start();
   }

    void runDetails() {
        int selection[] = pool_table.getSelectedRows();
        if (selection == null || selection.length < 1)
        {
            log("Please select one or more connections for this operation\n", 0);
            return;
        }

        for(int i = 0; i < selection.length; i++)
            if (pool.getConnection(selection[i]).conn == null) {
                log("Not at all selected items are connected\n",0);
                return;
            }

        for(int i = 0; i < selection.length; i++) {
            LoginData data = pool.getConnection(selection[i]);

            if (data.dlg_runOpt == null)
                data.dlg_runOpt = new TPCARunOptions(null, "Run details | "+ data.strItemName, false, data);
            data.dlg_runOpt.show();
        }
   }

   public void oneTransMore() { nbTrans++; }
   public void addTrans(int count) { nbTrans += count; }
   public void oneThreadLess() { nbThreads--; }


   public void doRunTests(String titleDialog, int[] selection, int nEndTime, Results res) {

       //Block the start of a new tests, if a test is working already
       if (isBenchWorked) {
           log("Test is already run, wait when it is finished",0);
           return;
       }
       isBenchWorked = true;

       log("\nDeleting from the History table ...",0);
       for (int nRow = 0; nRow < selection.length; nRow++) {
           LoginData data = pool.getConnection(selection[nRow]);
           Statement stmt = null;
           try {
               log("delete from " + data.m_Driver.getHistoryName() + "\n",2);
               stmt = data.conn.createStatement();
               stmt.executeUpdate("delete from " + data.m_Driver.getHistoryName());
               data.conn.commit(); log("Done\n",0);
           } catch(SQLException e) {
               log("Run tests error : " + e.getMessage(),0);
               isBenchWorked = false;
               return;
           } finally {
               if (stmt != null)
                   try {
                       stmt.close();
                   } catch(SQLException e) { }
           }
       }

       // check if an jdbc driver support a selected options
       for (int nRow = 0; nRow < selection.length; nRow++) {
           LoginData data = pool.getConnection(selection[nRow]);
           data.tpca.supported = true;
           if (! data.isTxnModeSupported(data.tpca.txnOption))
               data.tpca.supported = false;
           if (! data.isResSetTypeSupported(data.tpca.scrsOption))
               data.tpca.supported = false;
           if ((! data.isBatchSupported()) && data.tpca.nBatchSize > 1)
               data.tpca.supported = false;
           if ((! data.m_Driver.supportsProcedures()) && data.tpca.sqlOption == TPCABench.RUN_SPROC)
               data.tpca.supported = false;
           if (data.tpca.nThreads > 1)
               data.tpca.bTrans = true;
       }

       m_tpcaBench.removeAllElements();
       java.util.Date startTime = new java.util.Date();
       m_tests = new ThreadGroup("JDBC Tests");

       int _nThreads = 0;
       try {
           for (int nRow = 0; nRow < selection.length; nRow++) {
               LoginData data = pool.getConnection(selection[nRow]);
               if (data.tpca.supported) {
                   _nThreads += data.tpca.nThreads;
                   m_tpcaBench.addElement(new TPCABench(m_tests,
                           "Test Thread 0",
                           data.conn,
                           m_logger,
                           this,
                           data.m_Driver,
                           nEndTime,
                           startTime.getTime(),
                           data.tpca.sqlOption,
                           data.tpca.txnOption,
                           data.tpca.scrsOption,
                           data.tpca.bTrans,
                           data.tpca.bQuery,
                           data.tpca.travCount,
                           data.tpca.nBatchSize,
                           data.strNowFunction));
                   for(int nThread = 1; nThread < data.tpca.nThreads; nThread++) {
                       m_tpcaBench.addElement(new TPCABench(m_tests,
                               "Test thread " + nThread,
                               data.strURL, data.strUID, data.strPWD,
                               m_logger,
                               this,
                               data.m_Driver,
                               nEndTime,
                               startTime.getTime(),
                               data.tpca.sqlOption,
                               data.tpca.txnOption,
                               data.tpca.scrsOption,
                               data.tpca.bTrans,
                               data.tpca.bQuery,
                               data.tpca.travCount,
                               data.tpca.nBatchSize,
                               data.strNowFunction));
                   }
               }
           }
       } catch (SQLException e) {
           log("Error in creating the threads : " + e.getMessage(), 0);
           isBenchWorked = false;
           return;
       }

       // start the timer thread

       ((TheLogger)m_logger).nb_threads = _nThreads * selection.length;
       nbThreads=_nThreads;
       nbTrans=0;

       JDialog progress = new JDialog(m_parentFrame, titleDialog, false);
       Container thisContent = progress.getContentPane();

       JLabel jlabel = new JLabel("Transaction num. 100000");
       jlabel.setHorizontalAlignment(JLabel.CENTER);

       JLabel jlabel1 = new JLabel("Num. threads left " + nbThreads);
       jlabel1.setHorizontalAlignment(JLabel.CENTER);

       JButton cancelBtn = new JButton();
       cancelBtn.setActionCommand("Cancel");
       cancelBtn.setText("Cancel");

       JPanel masterProgressPane = new JPanel(new GridLayout(selection.length, 1, 0, 10));
       masterProgressPane.setBorder(BorderFactory.createEtchedBorder());
       int i = 0;
       for (int nRow = 0; nRow < selection.length; nRow++)
       {
           LoginData data = pool.getConnection(selection[nRow]);
           if (data.tpca.supported) {
               JPanel progressPane = new JPanel(new GridLayout(data.tpca.nThreads, 1, 0, 5));
               progressPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), data.strItemName));
               for (int nThread = 0; nThread < data.tpca.nThreads; nThread++) {
                   progressPane.add(((TPCABench)m_tpcaBench.elementAt(i++)).getProgressBar());
               }
               masterProgressPane.add(progressPane);
           }
       }

       JPanel labelsPane = new JPanel(new BorderLayout());
       labelsPane.add(BorderLayout.NORTH, jlabel);
       labelsPane.add(BorderLayout.CENTER, jlabel1);

       BorderLayout thisLayout;
       JPanel jpanel = new JPanel(thisLayout = new BorderLayout());
       jpanel.add(BorderLayout.NORTH, labelsPane);
       jpanel.add(BorderLayout.CENTER, masterProgressPane);

       jpanel.add(BorderLayout.SOUTH, cancelBtn);

       thisContent.add(jpanel);

       progress.setSize(
               thisLayout.preferredLayoutSize(jpanel).width + 150,
               thisLayout.preferredLayoutSize(jpanel).height + 50
       );
       progress.setLocation(
               m_parentFrame.getLocation().x + (m_parentFrame.getSize().width - progress.getSize().width) / 2,
               m_parentFrame.getLocation().y + (m_parentFrame.getSize().height - progress.getSize().height) / 2
       );
       jlabel.setText("Transaction num. " + nbTrans);
       progress.setVisible(true);

       thr = new RunThread(nEndTime, jlabel, jlabel1, progress, cancelBtn);
       cancelBtn.addActionListener(thr);
       thr.start();

       // run the show
       long runStartTime = System.currentTimeMillis();

       for (Enumeration el = m_tpcaBench.elements(); el.hasMoreElements(); )
           ((TPCABench)el.nextElement()).start();

       try {
           for (Enumeration el = m_tpcaBench.elements(); el.hasMoreElements(); )
               ((TPCABench)el.nextElement()).join(nEndTime*60000+ 1000);
       } catch( InterruptedException ie ) {
       }

       for (Enumeration el = m_tpcaBench.elements(); el.hasMoreElements(); )
           ((TPCABench)el.nextElement()).interrupt();

       thr.interrupt();
       try {
           thr.join();
       } catch (InterruptedException e) { }

       isBenchWorked = false;
       res.printResults(runStartTime, selection);
   }




  void cleanUp() {
      int selection[] = pool_table.getSelectedRows();
      if (selection == null || selection.length == 0) {
          log("Please select connections for this operation\n", 0);
          return;
      }

      for (int item = selection[0]; item < selection.length; item++) {
          LoginData data = pool.getConnection(item);
          if (data.conn == null) {
              log(data.strItemName +": Not connected\n",0);
              continue;
          }
          if (data.m_Driver == null) {
              log(data.strItemName +": Driver type is not specified\n",0);
              continue;
          }

          if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this,
                  "Drop TPC-A tables & procedure results table on :\n" + data.strURL,
                  "Confirm tables & procedure drop",JOptionPane.YES_NO_OPTION))
          {
              Statement stmt = null;
              try {
                  stmt = data.conn.createStatement();
                  log("Cleaning up " + data.m_Driver.getBranchName() + " table ...",0);
                  log("drop table " + data.m_Driver.getBranchName() + "\n",2);
                  executeNoCheck(stmt, "drop table " + data.m_Driver.getBranchName());
                  log("Done\n",0);

                  log("Cleaning up " + data.m_Driver.getAccountName() + " table ...",0);
                  log("drop table " + data.m_Driver.getAccountName() + "\n",2);
                  executeNoCheck(stmt, "drop table " + data.m_Driver.getAccountName());
                  log("Done\n",0);

                  log("Cleaning up " + data.m_Driver.getTellerName() + " table ...",0);
                  log("drop table " + data.m_Driver.getTellerName() + "\n",2);
                  executeNoCheck(stmt, "drop table " + data.m_Driver.getTellerName());
                  log("Done\n",0);

                  log("Cleaning up " + data.m_Driver.getHistoryName() + " table ...",0);
                  log("drop table " + data.m_Driver.getHistoryName() + "\n",2);
                  executeNoCheck(stmt, "drop table " + data.m_Driver.getHistoryName());
                  log("Done\n",0);

                  String sql = data.m_Driver.m_strDropProcedure;
                  if (sql != null) {
                      log("Cleaning up procedures ...",0);
                      log(sql + "\n",2);
                      executeNoCheck(stmt, sql);
                      log("Done\n",0);
                  }

              } catch(SQLException e) {
                  log("Cleanup error : " + e.getMessage() + "\n",0);
              } finally {
                  if(stmt != null)
                      try {
                          stmt.close();
                      } catch(SQLException e) { }
              }
          }
      }
   }

   public void log(String message, int nLevel) {
      if(m_logger != null) m_logger.log(message,nLevel);
   }


   void createResult() {
       if (results == null) {
           log("Not Connected\n",0);
           return;
       }
       int nIndex = -1;
       if (results.m_Driver != null)
           for (int n = 0;n < Driver.DriverMap.length;n++)
               if (results.m_Driver == Driver.DriverMap[n])
                   nIndex = n;

       Statement stmt = null;
       try {
           results.setDriver(nIndex);

           log("Creating results in " + results.strURL + "(" + results.strDBMSName + ") ...  \n",0);
           stmt = results.conn.createStatement();
           //executeNoCheck(stmt, "drop table results");
           String strSQL = results.m_Driver.makeCreateResults();
           log(strSQL + "\n",2);
           stmt.execute(strSQL);
           log("Done\n",0);
       } catch(Exception e) {
           log("Error creating results Table : " + e.getMessage() + "\n",0);
       } finally {
           if (stmt != null)
               try {
                   stmt.close();
               } catch (SQLException ex) {}
       }
   }


   public void dropResult() {
       if (results == null) {
           log("Not Connected\n",0);
           return;
       }
       try {
           if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this,
                   "Drop 'resultsj' table on :\n" + results.strURL + "("
                           + results.strDBMSName + ")","Confirm RESULTS table drop", JOptionPane.YES_NO_OPTION))
               doDropResult();
       } catch(SQLException e) {
           log("Error dropping results Table : " + e.getMessage() + "\n",0);
       }
       repaint();
   }

   public void doDropResult() throws SQLException {
       if (results == null) {
           log("Not Connected\n",0);
           return;
       }
       Statement stmt = null;
       try {
           log("Dropping results from " + results.strURL + "(" + results.strDBMSName + ") ... ",0);
           stmt = results.conn.createStatement();
           log("drop table results\n",2);
           stmt.execute("drop table results");
           log("Done\n",0);
       } finally {
           if (stmt != null)
               stmt.close();
           stmt = null;
       }
   }

   public void saveXML(Document doc, String fname) throws Exception
   {
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "jbench.dtd");
//            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            // send DOM to file
            tr.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(fname)));
   }



  /////////////// RunThread //////////////////////
  class RunThread extends Thread implements ActionListener {
    private JLabel jlabel,jlabel1;
    private JDialog dialog;
    private JButton jbutton;
    public SQLException exception=null;
    private boolean cancel=false;
    long durationMins;
    long nCtr = 0;
    long startTime;

    public RunThread(long durationMins, JLabel jlabel,JLabel jlabel1,JDialog dialog,JButton jbutton) {
      this.jlabel=jlabel; this.jlabel1=jlabel1;
      this.dialog=dialog; this.jbutton=jbutton;
      this.durationMins = durationMins;
    }

    public boolean isCanceled() {
      return cancel;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == jbutton) {
            log("\n*** Cancel ***\n",0);
            cancel = true;

            if (m_tpcaBench != null)
                for (Enumeration el = m_tpcaBench.elements(); el.hasMoreElements(); )
                    ((TPCABench)el.nextElement()).cancel();
//        if (m_tpccBench != null)
//	  for (int i=0; i < m_tpccBench.length; i++)
//	      m_tpccBench[i].cancel();
        }
    }

    public void run() {

        if (jbutton != null && dialog != null && jlabel != null && jlabel1 != null)
            while(!dialog.isVisible() || !jbutton.isVisible() ||
                    !jlabel.isVisible() || !jlabel1.isVisible())
            {
                try {
                    new Thread().sleep(100);
                } catch(InterruptedException e) {}
            }

        nCtr = 0;
        startTime = System.currentTimeMillis();
        try {
            while (!cancel) {
                Thread.sleep(100);

                SwingUtilities.invokeAndWait(
                        new Runnable() {
                            public void run() {
                                if (durationMins > 0) {
                                    long remainingSecs = (durationMins * 60) -
                                            ((System.currentTimeMillis() - startTime)/1000);
                                    jlabel.setText(remainingSecs + " s remaining");
                                } else {
                                    jlabel.setText("Transaction num. "+nbTrans);
                                }

                                nCtr = nCtr >= 10000 ? 0 : nCtr + 1;
                                if (nCtr % 10 == 0)
                                    jlabel1.setText("Num. threads left "+nbThreads);
                            }
                        }
                );
            }
        } catch (Exception e) {};

        try {
            SwingUtilities.invokeAndWait(
                    new Runnable() {
                        public void run() { dialog.dispose(); }
                    }
            );
        } catch (Exception e) {}
    }
  }

  ///////////////////// RunThread ///////////////////
  class RunAllThread extends Thread {

    int[] selection;
    int nEndTime;
    String xml_name;
    boolean bRunAll;

    RunAllThread(int[] _selection, int _nEndTime,
     String _xml_name, boolean _bRunAll)
    {
      nEndTime = _nEndTime;
      bRunAll = _bRunAll;
      selection = _selection;
      xml_name = _xml_name;
    }

    public void run()
    {
        try{
            Results res = Results.InitResultsSaving(BenchPanel.this, nEndTime);

            if ( ! bRunAll ) {
                doRunTests("   JDBC  Benchmark  in  progress ...   ", selection, nEndTime, res);
            } else {
                int travCount = 0;

                for (int fTrans = 0; fTrans <= 1; fTrans++) {
                    for (int fQuery = 0; fQuery <= 1; fQuery++) {
                        for (int nIsolation = 0; nIsolation < 5; nIsolation++) {
                            if (fQuery == 0 && nIsolation > 0)
                                continue;

                            for (int nCursor = 0; nCursor < 3; nCursor++) {



                                if (fQuery == 0 && nCursor > 0)
                                    continue;

                                if (nCursor > 0)
                                    travCount = 3;

                                // Loop around the SQL options
                                for (int nOption = 1; nOption <= 3; nOption++){
                                    StringBuffer title = new StringBuffer();
                                    switch (nOption) {
                                        case TPCABench.RUN_TEXT: title.append("SQLExecute"); break;
                                        case TPCABench.RUN_PREPARED: title.append("Prep-Execute"); break;
                                        case TPCABench.RUN_SPROC: title.append("Stored proc"); break;
                                    }
                                    if (fTrans == 1)
                                        title.append("/Trans");
                                    if (fQuery == 1)
                                        title.append("/Query");
                                    switch(nIsolation) {
                                        case 1: title.append("/Uncommited"); break;
                                        case 2: title.append("/Commited"); break;
                                        case 3: title.append("/Repeatable"); break;
                                        case 4: title.append("/Serializable"); break;
                                    }
                                    switch(nCursor){
                                        case 1: title.append("/Insensitive"); break;
                                        case 2: title.append("/Sensitive"); break;
                                    }

                                    for (int nRow = 0; nRow < selection.length; nRow++) {
                                        LoginData.TPCA tpca = pool.getConnection(selection[nRow]).tpca;
                                        tpca.sqlOption = nOption;
                                        tpca.travCount = travCount;
                                        tpca.bTrans = (fTrans == 0 ? false : true);
                                        tpca.bQuery = (fQuery == 0 ? false : true);
                                        switch(nIsolation) {
                                            case 1:
                                                tpca.txnOption = Connection.TRANSACTION_READ_UNCOMMITTED;
                                                break;
                                            case 2:
                                                tpca.txnOption = Connection.TRANSACTION_READ_COMMITTED;
                                                break;
                                            case 3:
                                                tpca.txnOption = Connection.TRANSACTION_REPEATABLE_READ;
                                                break;
                                            case 4:
                                                tpca.txnOption = Connection.TRANSACTION_SERIALIZABLE;
                                                break;
                                            default:
                                                tpca.txnOption = TPCABench.TXN_DEFAULT;
                                        }

                                        switch(nCursor){
                                            case 1:
                                                tpca.scrsOption = ResultSet.TYPE_SCROLL_INSENSITIVE;
                                                break;
                                            case 2:
                                                tpca.scrsOption = ResultSet.TYPE_SCROLL_SENSITIVE;
                                                break;
                                            default:
                                                tpca.scrsOption = ResultSet.TYPE_FORWARD_ONLY;
                                        }



                                    }
                                    doRunTests(title.toString(), selection, nEndTime, res);
                                    if (thr.isCanceled())
                                        return;
                                }// SQL options

                            } // Cursor

                        } // Isolation
                    } // Query
                } // Transactions
            }

            Document doc = res.DoneResultsSaving();
            try {
                saveXML(doc, m_strFileName);
            } catch(Exception e) {
                log(e.toString(), 0);
            }

        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
  }


   class BranchThread extends Thread implements ActionListener {

       private int nMaxBranch, nBranch;
       private JLabel jlabel;
       private Statement stmt;
       private Driver m_Driver;
       private JDialog dialog;
       private JButton jbutton;
       public boolean cancel=false;
       public SQLException exception=null;

       public BranchThread(int _nMaxBranch, JLabel jlabel, Statement stmt,
            Driver m_Driver, JDialog dialog, JButton jbutton)
       {
           this.nMaxBranch=_nMaxBranch;
           this.jlabel=jlabel;
           this.stmt=stmt;
           this.m_Driver=m_Driver;
           this.dialog=dialog;
           this.jbutton=jbutton;
       }

       public void actionPerformed(ActionEvent e)
       {
           if (e.getSource() == jbutton) {
               log("\n*** Cancel ***\n",0);
               cancel=true;
               nBranch=nMaxBranch;
               dialog.dispose();
               jbutton.removeActionListener(this);
           }
       }

       public void run()
       {
           while(!(dialog.isVisible() && jbutton.isVisible() && jlabel.isVisible()))
               try {
                   new Thread().sleep(100);
               } catch(InterruptedException e) { }

           try {
               for(nBranch = 0; nBranch < nMaxBranch && !cancel; nBranch++) {
                   jlabel.setText("Create branch num "+nBranch+"/"+nMaxBranch);
                   jlabel.repaint();
                   String strSQL = "insert into " + m_Driver.getBranchName()
                           + " (branch, fillerint, balance, filler) values ("
                           + (nBranch + 1) + "," + // Branch ID
                           (nBranch + 1) + "," + 1000 + "," + "\'" + strFiller + "\'" + ")";
                   log(strSQL + "\n",2);
                   stmt.executeUpdate(strSQL);
               }
           } catch(SQLException e) {
               exception=e;
               dialog.dispose();
           }
           dialog.dispose();
      }
   }

   void loadBranch(LoginData data, Statement stmt) throws SQLException
   {
       int nMaxBranch = data.tpca.nMaxBranch;
       log("Attempting to load " + nMaxBranch + " records into "
               + data.m_Driver.getBranchName() + " table ... ",0);

       JDialog dialog = new JDialog(m_parentFrame,"Creation of branch table ...",true);
       Container thisContent = dialog.getContentPane();
       JLabel jlabel = new JLabel("Create branch num. "+nMaxBranch+"/"+nMaxBranch);
       jlabel.setHorizontalAlignment(JLabel.CENTER);
       JButton jbutton = new JButton();
       jbutton.setActionCommand("Cancel"); jbutton.setText("Cancel");
       JPanel jpanel = new JPanel();
       BorderLayout thisLayout = new BorderLayout();
       jpanel.setLayout(thisLayout);
       jpanel.add(BorderLayout.NORTH,jlabel);
       jpanel.add(BorderLayout.SOUTH,jbutton);
       thisContent.add(jpanel);
       dialog.setSize(thisLayout.preferredLayoutSize(jpanel).width+50,thisLayout.preferredLayoutSize(jpanel).height+50);
       dialog.setLocation(m_parentFrame.getLocation().x+(m_parentFrame.getSize().width-dialog.getSize().width)/2,
               m_parentFrame.getLocation().y+(m_parentFrame.getSize().height-dialog.getSize().height)/2);
       jlabel.setText("Create branch num. 1/"+nMaxBranch);
       BranchThread thr = new BranchThread(nMaxBranch, jlabel, stmt, data.m_Driver, dialog, jbutton);
       jbutton.addActionListener(thr);
       thr.start();
       dialog.setVisible(true);

       // stmt.close();
       if (thr.exception!=null)
           throw thr.exception;
       if (!thr.cancel)
           log("Done\n",0);
       repaint();
   }

   class TellerThread extends Thread implements ActionListener {

       private int nMaxTeller, nTeller, nMaxBranch;
       private JLabel jlabel;
       private Statement stmt;
       private Driver m_Driver;
       private JDialog dialog;
       private JButton jbutton;
       public boolean cancel=false;
       public SQLException exception=null;

       public TellerThread(int _nMaxBranch, int _nMaxTeller,JLabel jlabel,
                           Statement stmt, Driver m_Driver, JDialog dialog, JButton jbutton)
       {
           this.nMaxBranch = _nMaxBranch;
           this.nMaxTeller = _nMaxTeller;
           this.jlabel=jlabel;
           this.stmt=stmt;
           this.m_Driver=m_Driver;
           this.dialog=dialog;
           this.jbutton=jbutton;
       }

       public void actionPerformed(ActionEvent e) {
           if (e.getSource() == jbutton) {
               log("\n*** Cancel ***\n",0);
               cancel=true;
               nTeller=nMaxTeller;
               dialog.dispose();
               jbutton.removeActionListener(this);
           }
       }

       public void run() {
           while(!(dialog.isVisible() && jbutton.isVisible() && jlabel.isVisible()))
               try {
                   new Thread().sleep(100);
               } catch(InterruptedException e) { }

           try {
               for(nTeller = 0; nTeller < nMaxTeller && !cancel; nTeller++) {
                   jlabel.setText("Create teller num "+nTeller+"/"+nMaxTeller);
                   jlabel.repaint();
                   String strSQL = "insert into " + m_Driver.getTellerName()
                           + " (teller, branch, balance, filler) values ("
                           + (nTeller + 1) + "," + // Teller ID
                           ((long)(Math.random() * nMaxBranch)) + "," +100000
                           + "," + "'" + strFiller + "'" +
                           " )";
                   log(strSQL + "\n",2);
                   stmt.executeUpdate(strSQL);
               }
           } catch(SQLException e) {
               exception=e;
               dialog.dispose();
           }
           dialog.dispose();
       }
   }

   void loadTeller(LoginData data, Statement stmt) throws SQLException
   {
       int nMaxBranch = data.tpca.nMaxBranch;
       int nMaxTeller = data.tpca.nMaxTeller;
       log("Attempting to load " + nMaxTeller + " records into "
               + data.m_Driver.getTellerName() + " table ... ",0);

       JDialog dialog = new JDialog(m_parentFrame,"Creation of teller table ...",true);
       Container thisContent = dialog.getContentPane();
       JLabel jlabel = new JLabel("Create teller num. "+nMaxTeller+"/"+nMaxTeller);
       jlabel.setHorizontalAlignment(JLabel.CENTER);
       JButton jbutton = new JButton();
       jbutton.setActionCommand("Cancel"); jbutton.setText("Cancel");
       JPanel jpanel = new JPanel();
       BorderLayout thisLayout = new BorderLayout();
       jpanel.setLayout(thisLayout);
       jpanel.add(BorderLayout.NORTH,jlabel);
       jpanel.add(BorderLayout.SOUTH,jbutton);
       thisContent.add(jpanel);
       dialog.setSize(thisLayout.preferredLayoutSize(jpanel).width+50,
               thisLayout.preferredLayoutSize(jpanel).height+50);
       dialog.setLocation(m_parentFrame.getLocation().x+(m_parentFrame.getSize().width-dialog.getSize().width)/2,
               m_parentFrame.getLocation().y+(m_parentFrame.getSize().height-dialog.getSize().height)/2);
       jlabel.setText("Create teller num. 1/"+nMaxTeller);
       TellerThread thr = new TellerThread(nMaxBranch, nMaxTeller, jlabel, stmt,
               data.m_Driver, dialog, jbutton);
       jbutton.addActionListener(thr);
       thr.start();
       dialog.setVisible(true);

       // stmt.close();
       if (thr.exception!=null)
           throw thr.exception;
       if (!thr.cancel)
           log("Done\n",0);
       repaint();
   }

    class AccountThread extends Thread implements ActionListener {

        private int nMaxBranch, nMaxAccount, nAccount;
        private JLabel jlabel;
        private Statement stmt;
        private Driver m_Driver;
        private JDialog dialog;
        private JButton jbutton;
        public boolean cancel=false;
        public SQLException exception=null;

        public AccountThread(int _nMaxBranch, int _nMaxAccount, JLabel jlabel, Statement stmt,
                             Driver m_Driver, JDialog dialog, JButton jbutton)
        {
            this.nMaxBranch=_nMaxBranch;
            this.nMaxAccount=_nMaxAccount;
            this.jlabel=jlabel;
            this.stmt=stmt;
            this.m_Driver=m_Driver;
            this.dialog=dialog;
            this.jbutton=jbutton;
        }

        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource() == jbutton) {
                log("\n*** Cancel ***\n",0);
                cancel=true;
                nAccount=nMaxAccount;
                dialog.dispose();
                jbutton.removeActionListener(this);
            }
        }

        public void run()
        {
            while(!(dialog.isVisible() && jbutton.isVisible() && jlabel.isVisible()))
                try { new Thread().sleep(100); }
                catch(InterruptedException e) { }

            try {
                for(nAccount = 0; nAccount < nMaxAccount && !cancel; nAccount++) {
                    jlabel.setText("Create account num "+nAccount+"/"+nMaxAccount);
                    jlabel.repaint();
                    String strSQL = "insert into " + m_Driver.getAccountName()
                            + " (account, branch, balance, filler) values ("
                            + (nAccount + 1) + "," + // Account ID
                            ((long)(Math.random() * nMaxBranch)) + "," + 100000
                            + "," + "\'" + strFiller + "\'" +
                            " )";
                    log(strSQL + "\n",2);
                    stmt.executeUpdate(strSQL);
                }
            } catch(SQLException e) {
                exception=e;
                dialog.dispose();
            }
            dialog.dispose();
        }
    }

    void loadAccount(LoginData data, Statement stmt) throws SQLException
    {
        int nMaxBranch = data.tpca.nMaxBranch;
        int nMaxAccount = data.tpca.nMaxAccount;

        log("Attempting to load " + nMaxAccount + " records into "
                + data.m_Driver.getAccountName() + " table ... ",0);

        JDialog dialog = new JDialog(m_parentFrame,"Creation of account table ...",true);
        Container thisContent = dialog.getContentPane();
        JLabel jlabel = new JLabel("Create account num. "+nMaxAccount+"/"+nMaxAccount);
        jlabel.setHorizontalAlignment(JLabel.CENTER);
        JButton jbutton = new JButton();
        jbutton.setActionCommand("Cancel"); jbutton.setText("Cancel");
        JPanel jpanel = new JPanel();
        BorderLayout thisLayout = new BorderLayout();
        jpanel.setLayout(thisLayout);
        jpanel.add(BorderLayout.NORTH,jlabel);
        jpanel.add(BorderLayout.SOUTH,jbutton);
        thisContent.add(jpanel);
        dialog.setSize(thisLayout.preferredLayoutSize(jpanel).width+50,thisLayout.preferredLayoutSize(jpanel).height+50);
        dialog.setLocation(m_parentFrame.getLocation().x+(m_parentFrame.getSize().width-dialog.getSize().width)/2,
                m_parentFrame.getLocation().y+(m_parentFrame.getSize().height-dialog.getSize().height)/2);
        jlabel.setText("Create account num. 1/"+nMaxAccount);
        AccountThread thr = new AccountThread(nMaxBranch, nMaxAccount, jlabel, stmt,
                data.m_Driver, dialog, jbutton);
        jbutton.addActionListener(thr);
        thr.start();
        dialog.setVisible(true);

        // stmt.close();
        if (thr.exception!=null)
            throw thr.exception;
        if (!thr.cancel)
            log("Done\n",0);
        repaint();
    }




    public void oneMoreWarehouse() { nWar++; }
    public void oneMoreDistrict() { nDis++; }
    public void oneMoreItem() { mIt++; }
    public void oneMoreCustomer() { nCust++; }
    public void oneMoreOrder() { nOrd++; }
    public void oneMoreRound() { nRd++; }
    public void resetWarehouse() { nWar=0; }
    public void resetDistrict() { nDis=0; }
    public void resetItem() { mIt=0; }
    public void resetCustomer() { nCust=0; }
    public void resetOrder() { nOrd=0; }
    public void resetRound() { nRd=0; }


}
