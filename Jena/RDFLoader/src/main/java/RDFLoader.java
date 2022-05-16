

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.StreamRDF;

import org.json.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EmptyStackException;
import java.util.Stack;


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
    public static int max_threads = 5;

    public static String instance = VIRTUOSO_INSTANCE;
    public static int port = VIRTUOSO_PORT;
    public static String uid = VIRTUOSO_USERNAME;
    public static String pwd = VIRTUOSO_PASSWORD;

    public static VirtIsolationLevel isolation = VirtIsolationLevel.REPEATABLE_READ;
    public static int concurrency = VirtGraph.CONCUR_DEFAULT;

    static Stack<TaskItem> files = new Stack<>();
    static String clear_graph = null;
    static String data_dir = ".";



    TaskItem work;

    public static String getJSONString(JSONObject o, String key, String def) {
        if (o.has(key))
            return o.getString(key);
        else
            return def;
    }
    public static int getJSONInt(JSONObject o, String key, int def) {
        if (o.has(key))
            return o.getInt(key);
        else
            return def;
    }
    public static boolean getJSONBool(JSONObject o, String key, boolean def) {
        if (o.has(key))
            return o.getBoolean(key);
        else
            return def;
    }

    public static Lang getLang(String ftype) {
        String s = ftype.toUpperCase();
        switch (s) {
            case "RDF/XML":  return RDFLanguages.RDFXML;
            case "TURTLE":  return RDFLanguages.TURTLE;
            case "TTL":  return RDFLanguages.TTL;
            case "N3":  return RDFLanguages.N3;
            case "NTRIPLES":  return RDFLanguages.NTRIPLES;
            case "JSON-LD":  return RDFLanguages.JSONLD;
            case "JSON-LD10":  return RDFLanguages.JSONLD10;
            case "JSON-LD11":  return RDFLanguages.JSONLD11;
            case "RDF/JSON":  return RDFLanguages.RDFJSON;
            case "TRIG":  return RDFLanguages.TRIG;
            case "NQUADS":  return RDFLanguages.NQUADS;
            case "RDF-PROTO":  return RDFLanguages.RDFPROTO;
            case "RDF-THRIFT":  return RDFLanguages.RDFTHRIFT;
            case "SHACLC":  return RDFLanguages.SHACLC;
            case "TRIX":  return RDFLanguages.TRIX;
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
               instance = getJSONString(cconn, "host", VIRTUOSO_INSTANCE);
               port = getJSONInt(cconn, "port", VIRTUOSO_PORT);
               uid = getJSONString(cconn, "uid", VIRTUOSO_USERNAME);
               pwd = getJSONString(cconn, "pwd", VIRTUOSO_PASSWORD);
               uid = getJSONString(cconn, "uid", VIRTUOSO_USERNAME);

               s_isolation = getJSONString(cconn, "isolationMode", s_isolation);
               s_concurrency = getJSONString(cconn, "concurrencyMode", s_concurrency);

               chunk_size = getJSONInt(cconn, "chunk_size", chunk_size);
               useAutoCommit = getJSONBool(cconn, "useAutoCommit", useAutoCommit);
               clear_graph = getJSONString(cconn, "clear_graph", clear_graph);
               data_dir = getJSONString(cconn, "data_dir", data_dir);
               max_threads = getJSONInt(cconn, "max_threads", max_threads);




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
                       files.push(new TaskItem(fname, lang, graph, clear_graph));
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

       if (max_threads == 0)
           max_threads = files.size();

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
        System.out.println(" max Threads = "+max_threads);
        System.out.println("    Data dir = "+data_dir);

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

        RDFLoader[] tasks = new RDFLoader[max_threads];
        for(var i=0; i< tasks.length; i++)
            tasks[i] = new RDFLoader();

        for(var i : tasks)
            i.start();

        try {
            for (var i : tasks)
                i.join();
        } catch(Exception e) {
            log(e.toString());
        }
    }


   public RDFLoader() {

   }



    public void run()
    {
       this.work = null;

        while (files.size() != 0) {

            try {
                this.work = files.pop();
            }catch (EmptyStackException e){
                break;
            } catch (Exception e) {
                log("==[" + Thread.currentThread().getName() + "] Ex:" + e.toString());
                break;
            }


            log("==[" + Thread.currentThread().getName() + "] get task");

            VirtDataset vds = null;

            try {
                vds = new VirtDataset("jdbc:virtuoso://" + instance + ":" + port, uid, pwd);
                vds.setIsolationLevel(isolation);


                VirtModel vm = (VirtModel) vds.getNamedModel(work.graph);

                if (work.clear_graph) {
                    log("==[" + Thread.currentThread().getName() + "] Start clear graph = " + work.graph);
                    vm.removeAll();
                    log("==[" + Thread.currentThread().getName() + "] End clear graph = " + work.graph);
                }

                vm.setConcurrencyMode(concurrency);
                vm.setBatchSize(chunk_size);

                StreamRDF writer = vm.getStreamRDF(useAutoCommit, chunk_size, new MyDeadLockHandler(0));

                String fpath = (new File(data_dir, work.fname)).getPath();

                try (InputStream in = new FileInputStream(fpath)) {
                    RDFParser parser = RDFParser.create()
                            .source(in)
                            .lang(work.ftype) //.lang(RDFLanguages.N3)
                            .errorHandler(ErrorHandlerFactory.errorHandlerWarn)  //.errorHandler(ErrorHandlerFactory.errorHandlerStrict)
                            .build();

                    log("==[" + Thread.currentThread().getName() + "] Start load data = " + fpath);
                    parser.parse(writer);
                    log("==[" + Thread.currentThread().getName() + "] End load data = " + fpath);
                }

            } catch (Exception e) {
                log("==[" + Thread.currentThread().getName() + "]***FAILED Upload data " + e);
            } finally {
                if (vds != null)
                    try {
                        vds.close();
                    } catch (Exception e) {
                    }
            }
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
