

import org.apache.jena.query.*;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.util.PrintUtil;
import org.apache.jena.vocabulary.RDFS;


import virtuoso.jena.driver.*;

import java.io.FileReader;
import java.io.LineNumberReader;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Vector;



public class Main {

    public static final String VIRTUOSO_INSTANCE = "localhost";
    public static final int VIRTUOSO_PORT = 1111;
    public static final String VIRTUOSO_USERNAME = "dba";
    public static final String VIRTUOSO_PASSWORD = "dba";



    public static void main(String[] args) {

        String[] sa = new String[4];
        sa[0] = VIRTUOSO_INSTANCE;
        sa[1] = VIRTUOSO_PORT + "";
        sa[2] = VIRTUOSO_USERNAME;
        sa[3] = VIRTUOSO_PASSWORD;
        for (int i = 0; i < sa.length && i < args.length; i++) {
            sa[i] = args[i];
        }

        try{
            String gName = "urn:spin:nanotation:demo:royal:family";

            String url = "jdbc:virtuoso://" + sa[0] + ":" + sa[1]+"/log_enable=0";

            init_db(url, sa[2], sa[3]);

            VirtModel vdata = VirtModel.openDatabaseModel(gName, url, sa[2], sa[3]);

            vdata.setMacroLib("urn:spin:nanotation:demo:royal:family:lib2");

            String q1 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT *\n" +
                    " WHERE { ?s a <#RoyalPerson> . }";

            exec_select("Query1", vdata, q1);


            String q2 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT *\n" +
                    " WHERE { ?s a ?t . }";

            exec_select("Query2", vdata, q2);


            String q3 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT *\n" +
                    " WHERE { ?s a <#RoyalPerson> ; rel:siblingOf ?sibling . }\n";

            exec_select("Query3", vdata, q3);

            
            String q4 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT *\n" +
                    " WHERE { ?s a <#RoyalPerson> ; rel:grandParent ?gp . }\n";

            exec_select("Query4", vdata, q4);

            
            String q5 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT ?person rel:ancestorOf as ?relation ?descendant\n" +
                    " WHERE { ?person a <#RoyalPerson> ;\n" +
                    " rel:ancestorOf ?descendant . }";

            exec_select("Query5", vdata, q5);

            
            String q6 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT DISTINCT ?person rel:siblingOf as ?relation ?siblingOf\n" +
                    " WHERE { ?person  a <#RoyalPerson> ;\n" +
                    " rel:siblingOf  ?siblingOf\n" +
                    " }";

            exec_select("Query6", vdata, q6);

            
            String q7 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT ?person <#hasAuntie> as ?relation  ?hasAuntie\n" +
                    " WHERE { ?person a <#RoyalPerson> ;\n" +
                    " <#hasAuntie> ?hasAuntie\n" +
                    " } ";

            exec_select("Query7", vdata, q7);

            
            String q8 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT ?person <#hasUncle> as ?relation ?hasUncle\n" +
                    " WHERE { ?person a <#RoyalPerson> ;\n" +
                    " <#hasUncle> ?hasUncle\n" +
                    " } ";

            exec_select("Query8", vdata, q8);

            
            String q9 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    "\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT ?person <#hasUncle2> as ?relation ?hasUncle2\n" +
                    " WHERE { ?person a <#RoyalPerson> ;\n" +
                    " <#hasUncle2> ?hasUncle2\n" +
                    " } ";

            exec_select("Query9", vdata, q9);

            
            String q10 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    "\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT ?person <#hasCousin> as ?relation ?hasCousin\n" +
                    " WHERE { ?person a <#RoyalPerson> ;\n" +
                    " <#hasCousin> ?hasCousin\n" +
                    " } ";

            exec_select("Query10", vdata, q10);

            
            String q11 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    "\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT ?person <#hasCousin2> as ?relation ?hasCousin2\n" +
                    " WHERE { ?person a <#RoyalPerson> ;\n" +
                    " <#hasCousin2> ?hasCousin2\n" +
                    " }";

            exec_select("Query11", vdata, q11);

            
            String q12 = " PREFIX  rel:  <http://purl.org/vocab/relationship/>\n" +
                    "\n" +
                    " WITH <urn:spin:nanotation:demo:royal:family>\n" +
                    " SELECT ?s as ?ancestor\n" +
                    " ?descendant\n" +
                    " WHERE { ?s a <#RoyalPerson> ;\n" +
                    " rel:ancestorOf ?descendant\n" +
                    " }";

            exec_select("Query12", vdata, q12);

            
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

            exec_select("Query13", vdata, q13);


        } catch (Exception e) {
            System.out.println("ERROR Test Failed.");
            e.printStackTrace();
        }

    }




    static int init_db(String url, String uid, String pwd) throws Exception
    {
        Class.forName("virtuoso.jdbc4.Driver");

        java.sql.Connection conn = DriverManager.getConnection(url,uid,pwd);
        java.sql.Statement st = conn.createStatement();

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

    static void exec_query(java.sql.Statement st, String query) throws Exception
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


    public static void print_model(String header, Model m) {
        String h = header==null?"Model":header;
        System.out.println("===========["+h+"]==========");
        StmtIterator it = m.listStatements((Resource)null, (Property)null, (RDFNode)null);
        while(it.hasNext()) {
            Statement st = it.nextStatement();
            System.out.println(st);
        }
        System.out.println("============================\n");
    }

    public static void print_model(String header, StmtIterator it) {
        String h = header==null?"Model iterator":header;
        System.out.println("===========["+h+"]==========");
        while(it.hasNext()) {
            Statement st = it.nextStatement();
            System.out.println(st);
        }
        System.out.println("============================\n");
    }

    public static void exec_select(String header, Model m, String query) {
        String h = header==null?"":header;
        System.out.println("===========["+h+"]==========");
        System.out.println(query);
        System.out.println("-----------------------------");
        QueryExecution qexec = VirtuosoQueryExecutionFactory.create(query, m);
        ResultSet results =  qexec.execSelect();
        ResultSetFormatter.out(System.out, results);
        qexec.close();
        System.out.println("============================\n");

    }
	

}

