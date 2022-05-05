import java.net.URL;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;

import virtuoso.rdf4j.driver.*;


public class Main {

    public static final String VIRTUOSO_INSTANCE = "localhost";
    public static final int VIRTUOSO_PORT = 1111;
    public static final String VIRTUOSO_USERNAME = "dba";
    public static final String VIRTUOSO_PASSWORD = "dba";
    public static final String test_graph_name = "test:1";
    public static final String nfile = "sp2b.n3";

    public static void log(String mess) {
        System.out.println("   " + mess);
    }


    public static void main(String[] args) {

        String[] sa = new String[4];
        sa[0] = VIRTUOSO_INSTANCE;
        sa[1] = VIRTUOSO_PORT + "";
        sa[2] = VIRTUOSO_USERNAME;
        sa[3] = VIRTUOSO_PASSWORD;
        for (int i = 0; i < sa.length && i < args.length; i++) {
            sa[i] = args[i];
        }
        RepositoryConnection con = null;

        try{
            String url = "jdbc:virtuoso://" + sa[0] + ":" + sa[1];
            long count;
            URL file_url = new URL("file:"+nfile);

            VirtuosoRepository repository = new VirtuosoRepository(url, sa[2], sa[3]);
            con = repository.getConnection();

            IRI context = repository.getValueFactory().createIRI(test_graph_name);

            con.clear(context);

            log("\ntest1 ============");
            try
            {
                log("Start transaction");
                con.begin();

                log("Insert data from file "+nfile);
                con.add(file_url, "", RDFFormat.N3, context);

                count = con.size(context);
                log("Inserted :"+count+" triples");

                log("Rollback transaction");
                con.rollback();

                count = con.size(context);
                log("After abort txn :"+count+" triples");
                if (count != 0)
                    throw new Exception("Count must be == 0");

            } catch (Exception e) {
                con.rollback();
                log("***FAILED Test "+e);
            }


            log("\n\ntest2 ============");
            try
            {
                log("Start transaction");
                con.begin();

                log("Insert data from file "+nfile);
                con.add(file_url, "", RDFFormat.N3, context);

                count = con.size(context);
                log("Inserted :"+count+" triples");

                log("Commit transaction");
                con.commit();

                count = con.size(context);
                log("After commit txn :"+count+" triples");
                if (count == 0)
                    throw new Exception("Count must be != 0");

            } catch (Exception e) {
                con.rollback();
                log("***FAILED Test "+e);
            }

            log("\n\ntest3 ============");
            try
            {
                log("Start transaction");
                con.begin();

                count = con.size(context);
                log("There are :"+count+" triples in graph "+test_graph_name);

                log("Remove all triples from graph");
                con.clear(context);

                count = con.size(context);
                log("There are :"+count+" triples after remove");

                log("Abort transaction");
                con.rollback();

                count = con.size(context);
                log("There are :"+count+" triples after Abort");

            } catch (Exception e) {
                con.rollback();
                log("***FAILED Test "+e);
            }

        }
        catch (Exception e) {
            System.out.println("ERROR Test Failed.");
            e.printStackTrace();
        }
        finally {
            if (con != null) try {
                con.close();
            }
            catch (RepositoryException e) {
                e.printStackTrace();
            }
        }
    }




}

