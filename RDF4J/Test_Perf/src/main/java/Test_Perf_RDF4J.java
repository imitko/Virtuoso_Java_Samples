import java.sql.SQLException;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import virtuoso.rdf4j.driver.*;


public class Test_Perf_RDF4J extends Thread {

    public static final String VIRTUOSO_INSTANCE = "localhost";
    public static final String VIRTUOSO_PORT = "1111";
    public static final String VIRTUOSO_USERNAME = "dba";
    public static final String VIRTUOSO_PASSWORD = "dba";

    public static long max_triples = 10000000;         //max insert triples per thread
    public static int max_threads = 5;

    public static int chunk_size = 20000;
    public static String graph_name = "test:insert";
    public static String s_isolation = "repeatable_read";
    public static String s_concurrency = "default";

    public static String instance = VIRTUOSO_INSTANCE;
    public static String port = VIRTUOSO_PORT;
    public static String uid = VIRTUOSO_USERNAME;
    public static String pwd = VIRTUOSO_PASSWORD;

    public static int isolation = Connection.TRANSACTION_REPEATABLE_READ;
    public static int concurrency = VirtuosoRepository.CONCUR_DEFAULT;

    char pid;
    int id;

    ValueFactory vfac = null;
    IRI context = null;


    public static int getIsolationLevel() {
       String s = s_isolation.toLowerCase();
       if (s.equals("read_uncommitted"))
         return Connection.TRANSACTION_READ_UNCOMMITTED;
       else if (s.equals("read_committed"))
         return Connection.TRANSACTION_READ_COMMITTED;
       else if (s.equals("repeatable_read"))
         return Connection.TRANSACTION_REPEATABLE_READ;
       else if (s.equals("serializable"))
         return Connection.TRANSACTION_SERIALIZABLE;
       else {
         s_isolation = "repeatable_read";
         return Connection.TRANSACTION_REPEATABLE_READ;
       }
    }

    public static int getConcurrencyMode() {
       String s = s_concurrency.toLowerCase();
       if (s.equals("optimistic"))
         return  VirtuosoRepository.CONCUR_OPTIMISTIC;
       else if (s.equals("pessimistic"))
         return VirtuosoRepository.CONCUR_PESSIMISTIC;
       else {
         s_concurrency = "default";
         return VirtuosoRepository.CONCUR_DEFAULT;
       }
    }


    public static void log(String mess) {
        System.err.println("   " + mess);
    }


    public static void main(String[] args) {

        if (args.length > 0)
          instance = args[0];

        if (args.length > 1)
          port = args[1];

        if (args.length > 2)
          uid = args[2];

        if (args.length > 3)
          pwd = args[3];

        if (args.length > 4)
          s_isolation = args[4];

        if (args.length > 5)
          s_concurrency = args[5];

       isolation = getIsolationLevel();
       concurrency = getConcurrencyMode();

        System.out.println("App options:");
        System.out.println(" % java Test_Perf_RDF4J hostname port_num uid pwd isolationMode concurrencyMode\n");
        System.out.println(" where ");
        System.out.println("   isolationMode: read_uncommitted | read_committed | repeatable_read | serializable ");
        System.out.println("                  default isolationMode = repeatable_read \n");
        System.out.println("   concurrencyMode: default | optimistic | pessimistic ");
        System.out.println("                  default concurrencyMode = default \n");

        System.out.println("Example of using:");
        System.out.println(" % java Test_Perf_RDF4J localhost 1111 dba dba repeatable_read default\n");

        System.out.println("===========================================================================\n");
        System.out.println("App will use next options");
        System.out.println("    hostname = "+instance);
        System.out.println("        port = "+port);
        System.out.println("         UID = "+uid);
        System.out.println("         PWD = "+pwd);
        System.out.println("   isolation = "+s_isolation);
        System.out.println(" concurrency = "+s_concurrency);
        System.out.println("===========================================================================\n");
        
        RepositoryConnection conn = null;
        try {
          VirtuosoRepository repository = new VirtuosoRepository("jdbc:virtuoso://" + instance + ":" + port, uid, pwd);

          IRI _context = repository.getValueFactory().createIRI(graph_name);
          conn = repository.getConnection();
          conn.clear(_context);

        } catch (Exception e) {
          log(e.toString());
          return;
        } finally {
          if (conn != null)
            try {
              conn.close();
            } catch(Exception e) {}
        }

        Test_Perf_RDF4J[] tests = new Test_Perf_RDF4J[max_threads];

        char pid = 'A';

        for(int i=0; i < max_threads; i++) {
          tests[i] = new Test_Perf_RDF4J((char)(pid+i));
        }

        for(Test_Perf_RDF4J task : tests)
          task.start();

        try {
          for(Test_Perf_RDF4J task : tests) {
            task.join();
          }
        } catch(Exception e) {
          log(e.toString());
        }
    }


    public static int rnd_skill(){
      int max_rnd = 100;
      return (int) (Math.random() * max_rnd); 
    }
    

    public Test_Perf_RDF4J(char _pid) {
      this.pid = _pid;
    }


    public List<Statement> genModel() {
        ArrayList<Statement> lst = new ArrayList<>(20000);

        try {
/**
http://www.beamery.com/person_A http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.beamery.com/person.
http://www.beamery.com/person_A http://www.beamery.com/lastLocationUpdate �2018-12-01�^^http://www.w3.org/2001/XMLSchema#date.
http://www.beamery.com/person_A http://www.beamery.com/hasCountry http://www.beamery.com/country_A.
http://www.beamery.com/person_A http://www.beamery.com/hasSkill http://www.beamery.com/skill_A.
http://www.beamery.com/person_A http://www.beamery.com/hasSkill http://www.beamery.com/skill_B.
http://www.beamery.com/person_A http://www.beamery.com/hasSkill http://www.beamery.com/skill_C.

**/
          int i = 0;

          IRI np1 = vfac.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

          IRI np2 = vfac.createIRI("http://www.beamery.com/lastLocationUpdate");
          IRI np3 = vfac.createIRI("http://www.beamery.com/hasCountry");
          IRI np4 = vfac.createIRI("http://www.beamery.com/hasSkill");

          IRI no1 = vfac.createIRI("http://www.beamery.com/person");
          IRI no2 = vfac.createIRI("\""+ LocalDate.now() +"\"^^http://www.w3.org/2001/XMLSchema#date");

          while(i < chunk_size) 
          {
            IRI ns = vfac.createIRI("http://www.beamery.com/person_"+pid+"_"+id);
            id++;

            IRI no3 = vfac.createIRI("http://www.beamery.com/country_"+pid);
            IRI no4 = vfac.createIRI("http://www.beamery.com/skill_A"+rnd_skill());
            IRI no5 = vfac.createIRI("http://www.beamery.com/skill_B"+rnd_skill());
            IRI no6 = vfac.createIRI("http://www.beamery.com/skill_C"+rnd_skill());
            IRI no7 = vfac.createIRI("http://www.beamery.com/skill_D"+rnd_skill());
            IRI no8 = vfac.createIRI("http://www.beamery.com/skill_E"+rnd_skill());

            lst.add(vfac.createStatement(ns,np1, no1, context));  i++;
            lst.add(vfac.createStatement(ns, np2, no2, context)); i++;
            lst.add(vfac.createStatement(ns, np3, no3, context)); i++;
            lst.add(vfac.createStatement(ns, np4, no4, context)); i++;
            lst.add(vfac.createStatement(ns, np4, no5, context)); i++;
            lst.add(vfac.createStatement(ns, np4, no6, context)); i++;
            lst.add(vfac.createStatement(ns, np4, no7, context)); i++;
            lst.add(vfac.createStatement(ns, np4, no8, context)); i++;

          }

        } catch (Exception e) {
            log("==["+Thread.currentThread().getName()+"]***FAILED Test " + e);
            return null;
        }

        return lst;
    }



    public void run() {

        RepositoryConnection conn = null;
        long i = 0;

        try {
          VirtuosoRepository repository = new VirtuosoRepository("jdbc:virtuoso://" + instance + ":" + port, uid, pwd);

          repository.setConcurrencyMode(concurrency);
          vfac = repository.getValueFactory();
          context = vfac.createIRI(graph_name);

          conn = repository.getConnection();
          ((VirtuosoRepositoryConnection)conn).setJdbcTransactionIsolation(isolation);

          while(i < max_triples) {
            List<Statement> g = genModel();
            long sz = g.size();

            i += sz;

            log("==["+Thread.currentThread().getName()+"]== data prepared "+sz);

            while(true) {
              try {
                conn.begin();
                conn.add(g);
                conn.commit();
              } catch (Exception e) {
                Throwable ex = e.getCause();
                boolean deadlock = false;
                if ((ex instanceof SQLException) && ((SQLException)ex).getSQLState().equals("40001"))
                  deadlock = true;

                if (deadlock) {
                  log("==["+Thread.currentThread().getName()+"]== deadlock, rollback all and try insert again");
                  conn.rollback();
                  continue;
                }

                throw e;
              }

              break;
            }
            log("==["+Thread.currentThread().getName()+"]== data inserted "+sz);
          
          }
        
        } catch (Exception e) {
            log("==["+Thread.currentThread().getName()+"]***FAILED Test " + e);
        } finally {
          if (conn != null)
            try {
              conn.close();
            } catch(Exception e) { }
        }

        log("==["+Thread.currentThread().getName()+"] DONE = "+i);
    }


}

