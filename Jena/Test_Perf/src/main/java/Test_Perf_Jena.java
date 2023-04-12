

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.iterator.*;
import org.apache.jena.graph.*;
import org.apache.jena.shared.*;
import org.apache.jena.datatypes.xsd.XSDDatatype;

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
import com.thedeanda.lorem.*;

public class Test_Perf_Jena extends Thread {

    public static final String VIRTUOSO_INSTANCE = "localhost";
    public static final String VIRTUOSO_PORT = "1111";
    public static final String VIRTUOSO_USERNAME = "dba";
    public static final String VIRTUOSO_PASSWORD = "dba";

    public static long max_triples = 10000000;         //max insert triples per thread
    public static int max_threads = 5;
    public static long delay = 0;

    public static int chunk_size = 20000;
    public static String graph_name = "urn:test-perf:insert";
    public static String s_isolation = "repeatable_read";
    public static String s_concurrency = "default";

    public static String instance = VIRTUOSO_INSTANCE;
    public static String port = VIRTUOSO_PORT;
    public static String uid = VIRTUOSO_USERNAME;
    public static String pwd = VIRTUOSO_PASSWORD;

    public static VirtIsolationLevel isolation = VirtIsolationLevel.REPEATABLE_READ;
    public static int concurrency = VirtGraph.CONCUR_DEFAULT;
    static boolean add_label = true;
    static boolean clean = true;
    public static boolean multigraph = false;

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

        for (int i = 0; i < args.length; i ++)
          {
             if (args[i].equals("-host")) {
                 instance = args[i+1];
                 i ++;
             }
             if (args[i].equals("-port")) {
                 port = args[i+1];
                 i ++;
             }
             if (args[i].equals("-mt")) {
                 max_threads = Integer.parseInt (args[i+1]);
                 i ++;
             }
             if (args[i].equals("-count")) {
                 max_triples = Integer.parseInt (args[i+1]);
                 i ++;
             }
             if (args[i].equals("-sleep")) {
                 delay = Integer.parseInt (args[i+1]);
                 i ++;
             }
             if (args[i].equals("-bs")) {
                 chunk_size = Integer.parseInt (args[i+1]);
                 i ++;
             }
             if (args[i].equals("-U")) {
                 uid = args[i+1];
                 i ++;
             }
             if (args[i].equals("-P")) {
                 pwd = args[i+1];
                 i ++;
             }
             if (args[i].equals("-isolation")) {
                 s_isolation = args[i+1];
                 i ++;
             }
             if (args[i].equals("-concurrency")) {
                 s_concurrency = args[i+1];
                 i ++;
             }
             if (args[i].equals("-no-literals")) {
                 add_label = false;
             }
             if (args[i].equals("-no-clean")) {
                 clean = false;
             }
             if (args[i].equals("-multigraph")) {
                 multigraph = true;
             }
          }
        /*
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
        */

       isolation = getIsolationLevel();
       concurrency = getConcurrencyMode();

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
        System.out.println(" max clients = "+max_threads);
        System.out.println(" max triples = "+max_triples);
        System.out.println(" batch size  = "+chunk_size);
        System.out.println("===========================================================================\n");

        VirtDataset vds = null;
        try {
          vds = new VirtDataset("jdbc:virtuoso://" + instance + ":" + port, uid, pwd);
          vds.setIsolationLevel(isolation);
          Model vm = vds.getNamedModel(graph_name);
          if (clean) {
          log ("Cleaning the existing test data.");
              vm.removeAll();
          }
        } catch (Exception e) {
          log(e.toString());
          return;
        } finally {
          if (vds != null)
            try {
              vds.close();
            } catch(Exception e) {}
        }

        Instant start = Instant.now();

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

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis()/1000;
        log ("");
        log ("Start time: " + start);
        log ("End time: " + finish);
        log ("Elapsed time: " + timeElapsed + " sec.");

    }


    public static int rnd_skill(){
      int max_rnd = 100;
      return (int) (Math.random() * max_rnd);
    }

    public static int rnd_age(){
      int max_rnd = 55;
      return (int) (Math.random() * max_rnd) + 15;
    }

    public static double rnd_salary(){
      int min_rnd = 20000;
      return (double) (Math.random() * min_rnd) + 10000;
    }


    public Test_Perf_Jena(char _pid) {
      this.pid = _pid;
    }



    public Model genModel() {
        Model m = ModelFactory.createDefaultModel();
        Lorem lorem = LoremIpsum.getInstance();

        try {
/**
http://localhost.localdomain/person_A http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://localhost.localdomain/person.
http://localhost.localdomain/person_A http://localhost.localdomain/lastLocationUpdate "2018-12-01"^^http://www.w3.org/2001/XMLSchema#date.
http://localhost.localdomain/person_A http://localhost.localdomain/hasCountry http://localhost.localdomain/country_A.
http://localhost.localdomain/person_A http://localhost.localdomain/hasSkill http://localhost.localdomain/skill_A.
http://localhost.localdomain/person_A http://localhost.localdomain/hasSkill http://localhost.localdomain/skill_B.
http://localhost.localdomain/person_A http://localhost.localdomain/hasSkill http://localhost.localdomain/skill_C.
**/
          int i = 0;

          Property rdfType = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
          Property locUpdate = ResourceFactory.createProperty("http://localhost.localdomain/lastLocationUpdate");
          Property hasCountry = ResourceFactory.createProperty("http://localhost.localdomain/hasCountry");
          Property hasSkill = ResourceFactory.createProperty("http://localhost.localdomain/hasSkill");
          Property foaf_name = ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/name");
          Property foaf_age = ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/age");
          Property dct = ResourceFactory.createProperty("http://purl.org/dc/terms/description");
          Property targetSalary = ResourceFactory.createProperty("http://localhost.localdomain/targetSalary");
          Property isActive = ResourceFactory.createProperty("http://localhost.localdomain/isActive");
          Property rdfsLabel = ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label");

          Resource personType = ResourceFactory.createResource("http://localhost.localdomain/person");
          Literal currDate = ResourceFactory.createTypedLiteral(LocalDateTime.now().toString(), XSDDatatype.XSDdateTime);

          while(i < chunk_size)
          {
            int skill_id;
            Resource person = ResourceFactory.createResource("http://localhost.localdomain/person_"+pid+"_"+id);
            Literal personName = ResourceFactory.createStringLiteral(lorem.getName()); // + " " + lorem.getLastName());
            //Literal personName = ResourceFactory.createStringLiteral("Person_"+pid+"_"+id);
            Literal age = ResourceFactory.createTypedLiteral (rnd_age());
            Literal salary = ResourceFactory.createTypedLiteral (rnd_salary());
            Literal active = ResourceFactory.createTypedLiteral ((id % 5 != 0) ? true : false);
            Literal resume = ResourceFactory.createTypedLiteral (lorem.getParagraphs(2,4));
            id++;

            Resource country = ResourceFactory.createResource("http://localhost.localdomain/country/"+lorem.getCountry().replaceAll("\\s", "%20"));

            skill_id = rnd_skill();
            Resource skill_A = ResourceFactory.createResource("http://localhost.localdomain/skill_A"+skill_id);
            Literal skill_ALabel = ResourceFactory.createStringLiteral("Skill_"+skill_id);

            skill_id = rnd_skill();
            Resource skill_B = ResourceFactory.createResource("http://localhost.localdomain/skill_B"+skill_id);
            Literal skill_BLabel = ResourceFactory.createStringLiteral("Skill_"+skill_id);

            skill_id = rnd_skill();
            Resource skill_C = ResourceFactory.createResource("http://localhost.localdomain/skill_C"+skill_id);
            Literal skill_CLabel = ResourceFactory.createStringLiteral("Skill_"+skill_id);

            skill_id = rnd_skill();
            Resource skill_D = ResourceFactory.createResource("http://localhost.localdomain/skill_D"+skill_id);
            Literal skill_DLabel = ResourceFactory.createStringLiteral("Skill_"+skill_id);

            skill_id = rnd_skill();
            Resource skill_E = ResourceFactory.createResource("http://localhost.localdomain/skill_E"+skill_id);
            Literal skill_ELabel = ResourceFactory.createStringLiteral("Skill_"+skill_id);

            m.add(ResourceFactory.createStatement(person, rdfType, personType)); i++;
            m.add(ResourceFactory.createStatement(person, locUpdate, currDate)); i++;
            if (add_label) {
                m.add(ResourceFactory.createStatement(person, foaf_age, age)); i++;
                //m.add(ResourceFactory.createStatement(person, dct, resume)); i++;
                m.add(ResourceFactory.createStatement(person, foaf_name, personName)); i++;
                m.add(ResourceFactory.createStatement(person, targetSalary, salary)); i++;
                m.add(ResourceFactory.createStatement(person, isActive, active)); i++;
            }
            m.add(ResourceFactory.createStatement(person, hasCountry, country)); i++;
            m.add(ResourceFactory.createStatement(person, hasSkill, skill_A)); i++;
            m.add(ResourceFactory.createStatement(person, hasSkill, skill_B)); i++;
            m.add(ResourceFactory.createStatement(person, hasSkill, skill_C)); i++;
            m.add(ResourceFactory.createStatement(person, hasSkill, skill_D)); i++;
            m.add(ResourceFactory.createStatement(person, hasSkill, skill_E)); i++;
            if (add_label) m.add(ResourceFactory.createStatement(skill_A, rdfsLabel, skill_ALabel)); i++;
            if (add_label) m.add(ResourceFactory.createStatement(skill_B, rdfsLabel, skill_BLabel)); i++;
            if (add_label) m.add(ResourceFactory.createStatement(skill_C, rdfsLabel, skill_CLabel)); i++;
            if (add_label) m.add(ResourceFactory.createStatement(skill_D, rdfsLabel, skill_DLabel)); i++;
            if (add_label) m.add(ResourceFactory.createStatement(skill_E, rdfsLabel, skill_ELabel)); i++;
            if (multigraph)
               break;
          }

        } catch (Exception e) {
            log("==["+Thread.currentThread().getName()+"]***FAILED Test" + e);
            return null;
        }

        return m;
    }



    public void run() {

        VirtDataset vds = null;
        String graph_uri = graph_name;
        long i = 0, ndone = 0;

        try {
          vds = new VirtDataset("jdbc:virtuoso://" + instance + ":" + port, uid, pwd);
          vds.setIsolationLevel(isolation);

          VirtModel vm = null;
          vm = (VirtModel)vds.getNamedModel(graph_uri);
          vm.setConcurrencyMode(concurrency);
          vm.setConcurrencyMode(concurrency);
          vm.begin();

          while(i < max_triples) {

            Model m;
            long sz;




             if (multigraph) {
                graph_uri = "http://localhost.localdomain/person_"+pid+"_"+id;
                vm = (VirtModel)vds.getNamedModel(graph_uri);
                vm.begin();
             }
             m = genModel(); // inc id
             sz = m.size();
             i += sz;
             ndone += sz;
            log("==["+Thread.currentThread().getName()+"]== data prepared "+ graph_uri);

            while(true) {
                 
             try {

                if (multigraph) {  
                  vm.removeAll();
                }
                vm.add(m);
                if (ndone >= chunk_size) {
                  log("==["+Thread.currentThread().getName()+"]== commit start at "+ graph_uri);
                    vm.commit();
                    vm.begin();
                    ndone = 0;
                  log("==["+Thread.currentThread().getName()+"]== COMMITED "+ graph_uri);
                }

              } catch (Exception e) {
                Throwable ex = e.getCause();
                boolean deadlock = false;
                if ((ex instanceof SQLException) && ((SQLException)ex).getSQLState().equals("40001"))
                  deadlock = true;

                if (deadlock) {
                  log("==["+Thread.currentThread().getName()+"]== deadlock, rollback " + graph_uri);
                  vm.abort();
                  continue;
                }

                throw e;
              }
              //if (nth % 15 == 0)
              //    vm.commit();
              break;
            }
            log("==["+Thread.currentThread().getName()+"]== data inserted "+sz);
            /*
            if (multigraph)
              vm = null;
              */
            if (delay > 0)
              Thread.sleep (delay, 0);  
          }
          vm.commit();
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
