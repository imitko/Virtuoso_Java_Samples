

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.iterator.*;
import org.apache.jena.graph.*;
import org.apache.jena.shared.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;
import java.time.*;
import java.sql.SQLException;
import org.apache.jena.sparql.core.DatasetGraph ;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtModel;
import virtuoso.jena.driver.VirtDataset;
import virtuoso.jena.driver.VirtIsolationLevel;

public class Test_Perf_Jena extends Thread {

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

    public static VirtIsolationLevel isolation = VirtIsolationLevel.REPEATABLE_READ;
    public static int concurrency = VirtGraph.CONCUR_DEFAULT;

    char pid;
    int id;


    public static VirtIsolationLevel getIsolationLevel() {
       String s = s_isolation.toLowerCase();
       if (s.equals("read_uncommitted"))
         return VirtIsolationLevel.READ_UNCOMMITTED;
       else if (s.equals("read_committed"))
         return VirtIsolationLevel.READ_COMMITTED;
       else if (s.equals("repeatable_read"))
         return VirtIsolationLevel.REPEATABLE_READ;
       else if (s.equals("serializable"))
         return VirtIsolationLevel.SERIALIZABLE;
       else {
         s_isolation = "repeatable_read";
         return VirtIsolationLevel.REPEATABLE_READ;
       }
    }

    public static int getConcurrencyMode() {
       String s = s_concurrency.toLowerCase();
       if (s.equals("optimistic"))
         return  VirtGraph.CONCUR_OPTIMISTIC;
       else if (s.equals("pessimistic"))
         return VirtGraph.CONCUR_PESSIMISTIC;
       else {
         s_concurrency = "default";
         return VirtGraph.CONCUR_DEFAULT;
       }
    }


    public static void log(String mess) {
        System.out.println("   " + mess);
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

        System.out.println("App options:");
        System.out.println(" % java Test_Perf_Jena hostname port_num uid pwd isolationMode concurrencyMode\n");
        System.out.println(" where ");
        System.out.println("   isolationMode: read_uncommitted | read_committed | repeatable_read | serializable ");
        System.out.println("                  default isolationMode = repeatable_read \n");
        System.out.println("   concurrencyMode: default | optimistic | pessimistic ");
        System.out.println("                  default concurrencyMode = default \n");

        System.out.println("Example of using:");
        System.out.println(" % java Test_Perf_Jena localhost 1111 dba dba repeatable_read default\n");

        System.out.println("===========================================================================\n");
        System.out.println("App will use next options");
        System.out.println("    hostname = "+instance);
        System.out.println("        port = "+port);
        System.out.println("         UID = "+uid);
        System.out.println("         PWD = "+pwd);
        System.out.println("   isolation = "+s_isolation);
        System.out.println(" concurrency = "+s_concurrency);
        System.out.println("===========================================================================\n");

        VirtDataset vds = null;
        try {
          vds = new VirtDataset("jdbc:virtuoso://" + instance + ":" + port, uid, pwd);
          vds.setIsolationLevel(isolation);
          Model vm = vds.getNamedModel(graph_name);
          vm.removeAll();
        } catch (Exception e) {
          log(e.toString());
          return;
        } finally {
          if (vds != null)
            try {
              vds.close();
            } catch(Exception e) {}
        }

        Test_Perf_Jena[] tests = new Test_Perf_Jena[max_threads];

        char pid = 'A';

        for(int i=0; i < max_threads; i++) {
          tests[i] = new Test_Perf_Jena((char)(pid+i));
        }

        for(Test_Perf_Jena task : tests)
          task.start();

        try {
          for(Test_Perf_Jena task : tests) {
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
    

    public Test_Perf_Jena(char _pid) {
      this.pid = _pid;
    }



    public Model genModel() {
        Model m = ModelFactory.createDefaultModel();

        try {
/**
http://www.beamery.com/person_A http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.beamery.com/person.
http://www.beamery.com/person_A http://www.beamery.com/lastLocationUpdate "2018-12-01"^^http://www.w3.org/2001/XMLSchema#date.
http://www.beamery.com/person_A http://www.beamery.com/hasCountry http://www.beamery.com/country_A.
http://www.beamery.com/person_A http://www.beamery.com/hasSkill http://www.beamery.com/skill_A.
http://www.beamery.com/person_A http://www.beamery.com/hasSkill http://www.beamery.com/skill_B.
http://www.beamery.com/person_A http://www.beamery.com/hasSkill http://www.beamery.com/skill_C.

**/
          int i = 0;

          Property np1 = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
          Property np2 = ResourceFactory.createProperty("http://www.beamery.com/lastLocationUpdate");
          Property np3 = ResourceFactory.createProperty("http://www.beamery.com/hasCountry");
          Property np4 = ResourceFactory.createProperty("http://www.beamery.com/hasSkill");

          Resource no1 = ResourceFactory.createResource("http://www.beamery.com/person");
          Resource no2 = ResourceFactory.createResource("\""+ LocalDate.now() +"\"^^http://www.w3.org/2001/XMLSchema#date");

          while(i < chunk_size) 
          {
            Resource ns = ResourceFactory.createResource("http://www.beamery.com/person_"+pid+"_"+id);
            id++;

            Resource no3 = ResourceFactory.createResource("http://www.beamery.com/country_"+pid);
            Resource no4 = ResourceFactory.createResource("http://www.beamery.com/skill_A"+rnd_skill());
            Resource no5 = ResourceFactory.createResource("http://www.beamery.com/skill_B"+rnd_skill());
            Resource no6 = ResourceFactory.createResource("http://www.beamery.com/skill_C"+rnd_skill());
            Resource no7 = ResourceFactory.createResource("http://www.beamery.com/skill_D"+rnd_skill());
            Resource no8 = ResourceFactory.createResource("http://www.beamery.com/skill_E"+rnd_skill());

            m.add(ResourceFactory.createStatement(ns, np1, no1)); i++;
            m.add(ResourceFactory.createStatement(ns, np2, no2)); i++;
            m.add(ResourceFactory.createStatement(ns, np3, no3)); i++;
            m.add(ResourceFactory.createStatement(ns, np4, no4)); i++;
            m.add(ResourceFactory.createStatement(ns, np4, no5)); i++;
            m.add(ResourceFactory.createStatement(ns, np4, no6)); i++;
            m.add(ResourceFactory.createStatement(ns, np4, no7)); i++;
            m.add(ResourceFactory.createStatement(ns, np4, no8)); i++;
          }

        } catch (Exception e) {
            log("==["+Thread.currentThread().getName()+"]***FAILED Test " + e);
            return null;
        }

        return m;
    }



    public void run() {

        VirtDataset vds = null;
        long i = 0;

        try {
          vds = new VirtDataset("jdbc:virtuoso://" + instance + ":" + port, uid, pwd);
          vds.setIsolationLevel(isolation);

          VirtModel vm = (VirtModel)vds.getNamedModel(graph_name);
          vm.setConcurrencyMode(concurrency);

          while(i < max_triples) {
            Model m = genModel();
            long sz = m.size();

            i += sz;

            log("==["+Thread.currentThread().getName()+"]== data prepared "+sz);

            while(true) {
              try {
                vm.begin().add(m).commit();
              } catch (Exception e) {
                Throwable ex = e.getCause();
                boolean deadlock = false;
                if ((ex instanceof SQLException) && ((SQLException)ex).getSQLState().equals("40001"))
                  deadlock = true;

                if (deadlock) {
                  log("==["+Thread.currentThread().getName()+"]== deadlock, rollback all and try insert again");
                  vm.abort();
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
          if (vds != null)
            try {
              vds.close();
            } catch(Exception e) { }
        }

        log("==["+Thread.currentThread().getName()+"] DONE = "+i);
    }
 }
