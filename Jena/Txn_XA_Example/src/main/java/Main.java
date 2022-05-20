import org.apache.jena.query.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.graph.*;
import org.apache.jena.graph.impl.*;
import org.apache.jena.shared.*;
import org.apache.jena.util.iterator.*;
import org.apache.jena.datatypes.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.*;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import javax.sql.*;
import javax.transaction.xa.*;
import org.apache.jena.sparql.core.DatasetGraph ;
import virtuoso.jena.driver.*;
import virtuoso.jdbc4.*;


public class Main {
    public static final String VIRTUOSO_INSTANCE = "localhost";
    public static final int VIRTUOSO_PORT = 1111;
    public static final String VIRTUOSO_USERNAME = "dba";
    public static final String VIRTUOSO_PASSWORD = "dba";

    public static final String nfile = "sp2b.n3";
    public static final String test_model_name = "test:1";
    

    public static void log(String mess) {
        System.out.println("   " + mess);
    }

    public static void main(String[] args) {
        Test_Txn1(args);
    }


    public static void Test_Txn1(String[] args) {
        String[] sa = new String[4];
        sa[0] = VIRTUOSO_INSTANCE;
        sa[1] = VIRTUOSO_PORT + "";
        sa[2] = VIRTUOSO_USERNAME;
        sa[3] = VIRTUOSO_PASSWORD;
        for (int i = 0; i < sa.length && i < args.length; i++) {
            sa[i] = args[i];
        }

        VirtuosoDataSource ds = new VirtuosoXADataSource();
        ds.setDataSourceName("virtuoso");
        ds.setServerName(sa[0]);
        ds.setPortNumber(Integer.valueOf(sa[1]));
        ds.setUser(sa[2]);
        ds.setPassword(sa[3]);
        ds.setCharset("UTF-8");
        ds.setLog_Enable(1);

        VirtDataset vd = null;
        VirtModel vm = null;
        XAResource xares = null;
        Xid xid = null;
        long count;

        try {
            vd = new VirtDataset((XADataSource)ds);

            vm = (VirtModel)vd.getNamedModel(test_model_name);

            vm.removeAll();

            xares = vd.getXAResource();
            xid = createXid(1);

            log("test1 ============");
            try
            {
                log("Start XA transaction");
                xares.start(xid, XAResource.TMNOFLAGS);

                log("Insert data from file "+nfile);
                InputStream in = FileManager.get().open(nfile);
                if (in == null) {
                    throw new IllegalArgumentException( "File: " + nfile + " not found");
                }
                vm.read(new InputStreamReader(in), null, "N3");

                count = vm.size();
                log("Inserted :"+count+" triples");

                log("End XA transaction");
                xares.end(xid, XAResource.TMFAIL);
                log("Rollback XA transaction");
                xares.rollback(xid);

                count = vm.size();
                log("After abort txn :"+count+" triples");
                if (count != 0) {
                  throw new Exception("Count must be == 0");
                }

            } catch (Exception e) {
                log("***FAILED Test "+e);
                e.printStackTrace();
            }


            log("\n\ntest2 ============");
            try
            {
                log("Start XA transaction");
                xares.start(xid, XAResource.TMNOFLAGS);

                log("Insert data from file "+nfile);
                InputStream in = FileManager.get().open(nfile);
                if (in == null) {
                    throw new IllegalArgumentException( "File: " + nfile + " not found");
                }
                vm.read(new InputStreamReader(in), null, "N3");

                count = vm.size();
                log("Inserted :"+count+" triples");

                log("End XA transaction");
                xares.end(xid, XAResource.TMSUCCESS);

                log("Prepare XA transaction");
                int prp = xares.prepare(xid);
                boolean doCommit = true;

                if (!((prp == XAResource.XA_OK) || (prp == XAResource.XA_RDONLY)))
                  doCommit = false;

                if (prp == XAResource.XA_OK) 
                {
                  if (doCommit)
                  {
                    // Commit(xid,onePhase) commits the prepared changes to the database
                    // If onePhase is set when only one transaction branch participates in
                    // the distributed transaction
                    log("Commit XA transaction");
                    xares.commit (xid, false);
                  }
                  else
                  {
                    log("Rollback XA transaction");
                    xares.rollback (xid);
                  }
                }

                count = vm.size();
                log("After commit txn :"+count+" triples");
                if (count == 0) {
                  throw new Exception("Count must be != 0");
                }

            } catch (Exception e) {
                log("***FAILED Test "+e);
            }


            log("\n\ntest3 ============");
            try
            {
                log("Start XA transaction");
                xares.start(xid, XAResource.TMNOFLAGS);

                count = vm.size();
                log("There are :"+count+" triples in model "+test_model_name);

                log("Remove all triples from model");
                vm.removeAll();

                count = vm.size();
                log("There are :"+count+" triples after remove");

                log("End XA transaction");
                xares.end(xid, XAResource.TMFAIL);
                log("Rollback XA transaction");
                xares.rollback(xid);

                count = vm.size();
                log("There are :"+count+" triples after Abort");


            } catch (Exception e) {
                log("***FAILED Test "+e);
            }

        }catch (Exception e){
            System.out.println("ERROR Test Failed.");
            e.printStackTrace();
        }
        finally {
            try {
                if (vm != null)
                  vm.close();
            }
            catch (Exception e) {}
            try {
                if (vd!=null)
                  vd.close();
            }
            catch (Exception e) {}
        }
    }


  private static Xid createXid(int branchId) throws XAException {

    // Global Transaction Id will be identical for all the branches under the
    // same distributed transaction. Here it is set to letter 'O'
    byte[] globalId = new byte[1];
    globalId[0]= (byte)'O';

    // Transaction Branch Id is unique for each branch under the same
    // distributed transaction. Set the transaction id as the parameter passed.
    byte[] branchIdArr = new byte[1];
    branchIdArr[0]= (byte)branchId;

    byte[] globalTranId    = new byte[64];
    byte[] branchQualifier = new byte[64];

    // Copy Global Transaction Id and Branch Id into a 64 byte array
    System.arraycopy (globalId, 0, globalTranId, 0, 1);
    System.arraycopy (branchIdArr, 0, branchQualifier, 0, 1);

    // Create the Transaction Id
    // Transaction Id has 3 components
    Xid xid = new VirtuosoXid(0x1234,     // Format identifier
                      globalTranId,     // Global transaction identifier
                      branchQualifier);    // Branch qualifier

    return xid;
  }

}
