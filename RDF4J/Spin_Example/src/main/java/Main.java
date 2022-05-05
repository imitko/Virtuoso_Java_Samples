import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import virtuoso.rdf4j.driver.VirtuosoRepository;

import java.io.FileReader;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;

public class Main {

//    public static final String VIRTUOSO_INSTANCE = "mc64";
    public static final String VIRTUOSO_INSTANCE = "localhost";
    public static final int VIRTUOSO_PORT = 1111;
    public static final String VIRTUOSO_USERNAME = "dba";
    public static final String VIRTUOSO_PASSWORD = "dba";

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
            String url = "jdbc:virtuoso://" + sa[0] + ":" + sa[1]+"/log_enable=0";

            init_db(url, sa[2], sa[3]);

            VirtuosoRepository repository = new VirtuosoRepository(url, sa[2], sa[3]);
            repository.setMacroLib("urn:spin:nanotation:demo:royal:family:lib2");
            con = repository.getConnection();


            String q1 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT *\n" +
                    " WHERE { ?s a <#RoyalPerson> . }";

            System.out.println("======== Query1 Exec:=========");
            System.out.println(q1);
            System.out.println("-----------------------------");
            doTupleQuery(con, q1);
            System.out.println("-----------------------------\n\n");



            String q2 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT *\n" +
                    " WHERE { ?s a ?t . }";

            System.out.println("Query2 Exec:");
            System.out.println(q2);
            System.out.println("-----------------------------");
            doTupleQuery(con, q2);
            System.out.println("-----------------------------\n\n");


            String q3 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT *\n" +
                    " WHERE { ?s a <#RoyalPerson> ; rel:siblingOf ?sibling . }\n";

            System.out.println("Query3 Exec:");
            System.out.println(q3);
            System.out.println("-----------------------------");
            doTupleQuery(con, q3);
            System.out.println("-----------------------------\n\n");


            String q4 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT *\n" +
                    " WHERE { ?s a <#RoyalPerson> ; rel:grandParent ?gp . }\n";

            System.out.println("Query4 Exec:");
            System.out.println(q4);
            System.out.println("-----------------------------");
            doTupleQuery(con, q4);
            System.out.println("-----------------------------\n\n");


            String q5 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT ?person rel:ancestorOf as ?relation ?descendant\n" +
                    " WHERE { ?person a <#RoyalPerson> ;\n" +
                    " rel:ancestorOf ?descendant . }";

            System.out.println("Query5 Exec:");
            System.out.println(q5);
            System.out.println("-----------------------------");
            doTupleQuery(con, q5);
            System.out.println("-----------------------------\n\n");


            String q6 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT DISTINCT ?person rel:siblingOf as ?relation ?siblingOf\n" +
                    " WHERE { ?person  a <#RoyalPerson> ;\n" +
                    " rel:siblingOf  ?siblingOf\n" +
                    " }";

            System.out.println("Query6 Exec:");
            System.out.println(q6);
            System.out.println("-----------------------------");
            doTupleQuery(con, q6);
            System.out.println("-----------------------------\n\n");


            String q7 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT ?person <#hasAuntie> as ?relation  ?hasAuntie\n" +
                    " WHERE { ?person a <#RoyalPerson> ;\n" +
                    " <#hasAuntie> ?hasAuntie\n" +
                    " } ";

            System.out.println("Query7 Exec:");
            System.out.println(q7);
            System.out.println("-----------------------------");
            doTupleQuery(con, q7);
            System.out.println("-----------------------------\n\n");


            String q8 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT ?person <#hasUncle> as ?relation ?hasUncle\n" +
                    " WHERE { ?person a <#RoyalPerson> ;\n" +
                    " <#hasUncle> ?hasUncle\n" +
                    " } ";

            System.out.println("Query8 Exec:");
            System.out.println(q8);
            System.out.println("-----------------------------");
            doTupleQuery(con, q8);
            System.out.println("-----------------------------\n\n");


            String q9 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    "\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT ?person <#hasUncle2> as ?relation ?hasUncle2\n" +
                    " WHERE { ?person a <#RoyalPerson> ;\n" +
                    " <#hasUncle2> ?hasUncle2\n" +
                    " } ";

            System.out.println("Query9 Exec:");
            System.out.println(q9);
            System.out.println("-----------------------------");
            doTupleQuery(con, q9);
            System.out.println("-----------------------------\n\n");

            String q10 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    "\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT ?person <#hasCousin> as ?relation ?hasCousin\n" +
                    " WHERE { ?person a <#RoyalPerson> ;\n" +
                    " <#hasCousin> ?hasCousin\n" +
                    " } ";

            System.out.println("Query10 Exec:");
            System.out.println(q10);
            System.out.println("-----------------------------");
            doTupleQuery(con, q10);
            System.out.println("-----------------------------\n\n");

            String q11 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    "\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT ?person <#hasCousin2> as ?relation ?hasCousin2\n" +
                    " WHERE { ?person a <#RoyalPerson> ;\n" +
                    " <#hasCousin2> ?hasCousin2\n" +
                    " }";

            System.out.println("Query11 Exec:");
            System.out.println(q11);
            System.out.println("-----------------------------");
            doTupleQuery(con, q11);
            System.out.println("-----------------------------\n\n");


            String q12 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    "\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT ?s as ?ancestor\n" +
                    " ?descendant\n" +
                    " WHERE { ?s a <#RoyalPerson> ;\n" +
                    " rel:ancestorOf ?descendant\n" +
                    " }";

            System.out.println("Query12 Exec:");
            System.out.println(q12);
            System.out.println("-----------------------------");
            doTupleQuery(con, q12);
            System.out.println("-----------------------------\n\n");

            String q13 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    "\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT DISTINCT *\n" +
                    " WHERE {\n" +
                    " { ?parent a <#RoyalPerson> ; rel:parentOf ?parentOf .}\n" +
                    " UNION\n" +
                    " { ?person a foaf:Person; <#hasAuntie> ?hasAuntie .}\n" +
                    " UNION\n" +
                    " { ?person a foaf:Person; <#hasUncle> ?hasUncle .}\n" +
                    " UNION\n" +
                    " { ?person a foaf:Person; <#hasCousin> ?hasCousin .}\n" +
                    " UNION\n" +
                    " { ?person a foaf:Person; <#hasUncle2> ?hasUncle2 .}\n" +
                    " UNION\n" +
                    " { ?person a foaf:Person; <#hasCousin2> ?hasCousin2 .}\n" +
                    " UNION\n" +
                    " { ?person a foaf:Person; rel:siblingOf ?hasSibling . }\n" +
                    " UNION\n" +
                    " { ?person a foaf:Person; rel:ancestorOf ?hasDescendant . }\n" +
                    " } ";

            System.out.println("Query13 Exec:");
            System.out.println(q13);
            System.out.println("-----------------------------");
            doTupleQuery(con, q13);
            System.out.println("-----------------------------\n\n");

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

    static void doTupleQuery(RepositoryConnection con, String query) throws Exception {
      try {
        TupleQuery resultsTable = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
        resultsTable.setIncludeInferred(true);
        TupleQueryResult bindings = resultsTable.evaluate();
        ArrayList<BindingSet> rs = new ArrayList<>();
        ArrayList<Integer> w = new ArrayList<>();

        // collect data and calc width
        for (String lb: bindings.getBindingNames()) {
            w.add(lb.length()+1);
        }

        for (int row = 0; bindings.hasNext(); row++) {
            BindingSet pairs = bindings.next();
            rs.add(pairs);
            int i=0;
            for (String lb: bindings.getBindingNames()) {
                Value v = pairs.getValue(lb);
                int len = (v!=null) ? len = v.toString().length() : 4;
                if (w.get(i)<=len)
                    w.set(i, len+1);
                i++;
            }
        }

        // print Results in table
        int i=0; int sz=0;
        for (String lb: bindings.getBindingNames()) {
            System.out.printf("%-"+w.get(i)+"s|",lb);
            sz+=w.get(i)+1;
            i++;
        }
        System.out.println();

        for(i=0; i < sz; i++) System.out.print('-');
        System.out.println();

        for (BindingSet bs: rs) {
            i=0;
            for (String lb: bindings.getBindingNames()) {
                Value v= bs.getValue(lb);
                System.out.printf("%-"+w.get(i)+"s|", (v!=null)?v.toString():"null");
                i++;
            }
            System.out.println();
        }
        System.out.println("\nRows ="+rs.size());
      } catch (Exception e) {
            System.out.println("ERROR Test Failed.");
            System.out.println(e);
      }
    }


    static int init_db(String url, String uid, String pwd) throws Exception
    {
        Class.forName("virtuoso.jdbc4.Driver");

        Connection conn = DriverManager.getConnection(url,uid,pwd);
        Statement st = conn.createStatement();

        LineNumberReader read = new LineNumberReader(new FileReader("spin_setup.sqlj"));
        StringBuilder  s = new StringBuilder();

        System.out.println("Start loadind data to DB");

        while (true) {
            String line = read.readLine();

            if (line == null) {
                break;
            }

            if (line.startsWith("--")) {
                continue; //skip
            }
            else if (line.startsWith("\\r")) {
                exec_query(st, s.toString());
                s.setLength(0);
            }
            else if(line.length()>0){
                s.append(line+"\n");
            }
        }
        System.out.println("End loadind data to DB\n\n");
        return 1;
    }

    static void exec_query(Statement st, String query) throws Exception
    {
        String s = trimStr(query);
        if (s.length()>0) {
            if (s.charAt(s.length() - 1) == ';') {
                s = s.substring(0, s.length() - 1);
            }
            st.execute(s);
        }

    }

    static String trimStr(String s)
    {
        int last = s.length()-1;
        for(int i=last; i >=0 && Character.isWhitespace(s.charAt(i)); i--) {}
        return s.substring(0,last).trim();
    }
}
