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

        VirtuosoDataSource ds = new VirtuosoDataSource();
        ds.setDataSourceName("virtuoso");
        ds.setServerName(sa[0]);
        ds.setPortNumber(Integer.valueOf(sa[1]));
        ds.setUser(sa[2]);
        ds.setPassword(sa[3]);
        ds.setCharset("UTF-8");
        ds.setLog_Enable(1);

        VirtDataset vd = null;
        VirtModel vm = null;
        long count;

        try {
            vd = new VirtDataset((DataSource)ds);

            vm = (VirtModel)vd.getNamedModel(test_model_name);

            vm.removeAll();

            log("test1 ============");
            try
            {
                log("Start transaction");
                vd.begin(ReadWrite.WRITE);

                log("Insert data from file "+nfile);
                InputStream in = FileManager.get().open(nfile);
                if (in == null) {
                    throw new IllegalArgumentException( "File: " + nfile + " not found");
                }
                vm.read(new InputStreamReader(in), null, "N3");

                count = vm.size();
                log("Inserted :"+count+" triples");

                log("Abort transaction");
                vm.abort();

                count = vm.size();
                log("After abort txn :"+count+" triples");
                if (count != 0) {
                  throw new Exception("Count must be == 0");
                }

            } catch (Exception e) {
                log("***FAILED Test "+e);
            }


            log("\n\ntest2 ============");
            try
            {
                log("Start transaction");
                vd.begin(ReadWrite.WRITE);

                log("Insert data from file "+nfile);
                InputStream in = FileManager.get().open(nfile);
                if (in == null) {
                    throw new IllegalArgumentException( "File: " + nfile + " not found");
                }
                vm.read(new InputStreamReader(in), null, "N3");

                count = vm.size();
                log("Inserted :"+count+" triples");

                log("Commit transaction");
                vm.commit();

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
                log("Start transaction");
                vd.begin(ReadWrite.WRITE);

                count = vm.size();
                log("There are :"+count+" triples in model "+test_model_name);

                log("Remove all triples from model");
                vm.removeAll();

                count = vm.size();
                log("There are :"+count+" triples after remove");

                log("Abort transaction");
                vm.abort();

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

}
