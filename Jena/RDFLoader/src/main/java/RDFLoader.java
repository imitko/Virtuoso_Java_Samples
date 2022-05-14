

import com.google.gson.JsonObject;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.StreamRDF;

import org.json.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;

import virtuoso.jena.driver.*;

public class RDFLoader extends Thread {

    public static final String VIRTUOSO_INSTANCE = "localhost";
    public static final int VIRTUOSO_PORT = 1111;
    public static final String VIRTUOSO_USERNAME = "dba";
    public static final String VIRTUOSO_PASSWORD = "dba";

    public static int chunk_size = 5000;
    public static String s_isolation = "repeatable_read";
    public static String s_concurrency = "default";
    public static boolean useAutoCommit = false;

    public static String instance = VIRTUOSO_INSTANCE;
    public static int port = VIRTUOSO_PORT;
    public static String uid = VIRTUOSO_USERNAME;
    public static String pwd = VIRTUOSO_PASSWORD;

    public static VirtIsolationLevel isolation = VirtIsolationLevel.REPEATABLE_READ;
    public static int concurrency = VirtGraph.CONCUR_DEFAULT;

    static ArrayList<TaskItem> files = new ArrayList<>();
    static String clear_graph = null;

    TaskItem work;


    public static Lang getLang(String ftype) {
        String s = ftype.toLowerCase();
        switch (s) {
            case "n3":  return RDFLanguages.N3;
            case "jsonld":  return RDFLanguages.JSONLD;
            case "rdfxml":  return RDFLanguages.RDFXML;
            case "turtle":  return RDFLanguages.TURTLE;
            case "ttl":  return RDFLanguages.TTL;
            default:  return null;
        }
    }

    public static VirtIsolationLevel getIsolationLevel() {
       String s = s_isolation.toLowerCase();
        switch (s) {
            case "read_uncommitted":
                return VirtIsolationLevel.READ_UNCOMMITTED;
            case "read_committed":
                return VirtIsolationLevel.READ_COMMITTED;
            case "repeatable_read":
                return VirtIsolationLevel.REPEATABLE_READ;
            case "serializable":
                return VirtIsolationLevel.SERIALIZABLE;
            default:
                s_isolation = "repeatable_read";
                return VirtIsolationLevel.REPEATABLE_READ;
        }
    }

    public static int getConcurrencyMode() {
       String s = s_concurrency.toLowerCase();
       switch (s) {
           case "optimistic":
               return  VirtGraph.CONCUR_OPTIMISTIC;
           case "pessimistic":
               return VirtGraph.CONCUR_PESSIMISTIC;
           default:
               s_concurrency = "default";
               return VirtGraph.CONCUR_DEFAULT;
       }
    }


    public static void log(String mess) {
        System.out.println("   " + mess);
    }

    public static void main(String[] args) {
       try {
           String config = Files.readString(Path.of("config.json"));

           var conf = new JSONObject(config);
           var cconn = conf.getJSONObject("conn");
           if (cconn != null) {
               var s = cconn.getString("host");
               if (s != null)
                   instance = s;

               var so = cconn.get("port");
               if (so != null)
                   port = cconn.getInt("port");

               s = cconn.getString("uid");
               if (s != null)
                   uid = s;

               s = cconn.getString("pwd");
               if (s != null)
                   pwd = s;

               s = cconn.getString("isolationMode");
               if (s != null)
                   s_isolation = s;

               s = cconn.getString("concurrencyMode");
               if (s != null)
                   s_concurrency = s;

               so = cconn.get("chunk_size");
               if (so != null)
                   chunk_size = cconn.getInt("chunk_size");

               so = cconn.get("useAutoCommit");
               if (so != null)
                   useAutoCommit = cconn.getBoolean("useAutoCommit");

               s = cconn.getString("clear_graph");
               if (s != null)
                   clear_graph = s;

               var data_dir = conf.getString ("data-dir");

               var data = conf.getJSONArray("data");
               if (data != null) {
                   for (var i=0; i < data.length(); i++) {
                       var v = data.getJSONObject(i);
                       var fname = v.getString("file");
                       var ftype = v.getString("type");
                       var graph = v.getString("graph");
                       var clear_graph = v.getBoolean("clear_graph");
                       Lang lang = getLang(ftype);
                       if (lang == null)
                           log("Error unsupported file type: "+ftype);
                       files.add(new TaskItem(data_dir+"/"+fname, lang, graph, clear_graph));
                   }
               }
           }
       } catch (Exception e) {
           log("Error:"+e.toString());
           System.exit(-1);
       }

       if (files.size() == 0) {
           log("data section in config.json is EMPTY");
           System.exit(-1);
       }

       isolation = getIsolationLevel();
       concurrency = getConcurrencyMode();

        System.out.println("===========================================================================\n");
        System.out.println("App will use next options");
        System.out.println("    hostname = "+instance);
        System.out.println("        port = "+port);
        System.out.println("         UID = "+uid);
        System.out.println("         PWD = "+pwd);
        System.out.println("   isolation = "+s_isolation);
        System.out.println(" concurrency = "+s_concurrency);
        System.out.println("  chunk_size = "+chunk_size);
        System.out.println("useAutoCommit= "+useAutoCommit);

        System.out.println("===========================================================================\n");

        if (clear_graph != null && clear_graph.length() > 0) {
            VirtModel vm = null;
            try {
                vm = VirtModel.openDatabaseModel(clear_graph,"jdbc:virtuoso://" + instance + ":" + port, uid, pwd);
                log("==[] Start clear graph = " + clear_graph);
                vm.removeAll();
                log("==[] End clear graph = " + clear_graph);
            } catch (Exception e) {
                log(e.toString());
                return;
            } finally {
                if (vm != null)
                    try {
                        vm.close();
                    } catch(Exception e) {}
            }
        }

        ArrayList<RDFLoader> tasks = new ArrayList<>();
        for(var i : files)
            tasks.add( new RDFLoader(i));


        for(var x : tasks)
            x.start();

        try {
        for(var x : tasks)
            x.join();
        } catch(Exception e) {
            log(e.toString());
        }
    }





    public RDFLoader(TaskItem v) {
        this.work = v;
    }




    public void run()
    {
        VirtDataset vds = null;

        try {
            vds = new VirtDataset("jdbc:virtuoso://" + instance + ":" + port, uid, pwd);
            vds.setIsolationLevel(isolation);


            VirtModel vm = (VirtModel)vds.getNamedModel(work.graph);

            if (work.clear_graph) {
                log("==[" + Thread.currentThread().getName() + "] Start clear graph = "+ work.graph);
                vm.removeAll();
                log("==[" + Thread.currentThread().getName() + "] End clear graph = "+ work.graph);
            }

            vm.setConcurrencyMode(concurrency);
            vm.setBatchSize(chunk_size);

            StreamRDF writer = vm.getStreamRDF(useAutoCommit, chunk_size, new MyDeadLockHandler(0));

            try (InputStream in = new FileInputStream(work.fname)) {
                RDFParser parser = RDFParser.create()
                        .source(in)
                        .lang(work.ftype) //.lang(RDFLanguages.N3)
                        .errorHandler(ErrorHandlerFactory.errorHandlerWarn)  //.errorHandler(ErrorHandlerFactory.errorHandlerStrict)
                        .build();

                log("==["+Thread.currentThread().getName()+"] Start load data = "+work.fname);
                parser.parse(writer);
                log("==["+Thread.currentThread().getName()+"] End load data = ");
            }

        } catch (Exception e) {
            log("==["+Thread.currentThread().getName()+"]***FAILED Test " + e);
        } finally {
          if (vds != null)
            try {
              vds.close();
            } catch(Exception e) { }
        }

        log("==["+Thread.currentThread().getName()+"] DONE = ");
    }

    protected static class TaskItem {
        final String fname;
        final Lang ftype;
        final String graph;
        final boolean clear_graph;

        TaskItem(String fname, Lang ftype, String graph, boolean clear_graph)
        {
            this.fname = fname;
            this.ftype = ftype;
            this.graph = graph;
            this.clear_graph = clear_graph;
        }
    }


    public class MyDeadLockHandler extends VirtStreamRDF.DeadLockHandler {

        public MyDeadLockHandler(int maxDeadLockCount) {
            super(maxDeadLockCount);
        }

        public boolean deadLockFired(int pass)
        {
            boolean rc = super.deadLockFired(pass);
            System.out.println("==["+Thread.currentThread().getName()+"]***DEADLOCK pass=" + pass +" maxDeadLocks="+this.maxDeadLockCount);
            if (rc)
                System.out.println("==["+Thread.currentThread().getName()+"]***Try insert chunk again=");
            return rc;

        }
    }

 }
