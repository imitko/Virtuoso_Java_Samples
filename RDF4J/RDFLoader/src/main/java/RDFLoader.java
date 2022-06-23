

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import org.eclipse.rdf4j.rio.RDFFormat;
import virtuoso.rdf4j.driver.*;

import org.json.*;

import java.io.*;
import java.nio.file.*;
import java.sql.Connection;
import java.util.EmptyStackException;
import java.util.Stack;



public class RDFLoader extends Thread {

    public static final String VIRTUOSO_INSTANCE = "localhost";
    public static final int VIRTUOSO_PORT = 1111;
    public static final String VIRTUOSO_USERNAME = "dba";
    public static final String VIRTUOSO_PASSWORD = "dba";

    public static int batch_size = 5000;
    public static String s_isolation = "repeatable_read";
    public static String s_concurrency = "default";
    public static boolean useAutoCommit = false;
    public static int max_threads = 5;

    public static String instance = VIRTUOSO_INSTANCE;
    public static int port = VIRTUOSO_PORT;
    public static String uid = VIRTUOSO_USERNAME;
    public static String pwd = VIRTUOSO_PASSWORD;

    public static int isolation = Connection.TRANSACTION_REPEATABLE_READ;
    public static int concurrency = VirtuosoRepository.CONCUR_DEFAULT;


    static Stack<TaskItem> files = new Stack<>();
    static String clear_graph = null;
    static String data_dir = ".";

    ValueFactory vfac = null;
    IRI context = null;



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

    public static RDFFormat getFormat(String ftype) {
        String s = ftype.toUpperCase();
        switch (s) {
            case "RDF/XML":  return RDFFormat.RDFXML;
            case "TURTLE":  return RDFFormat.TURTLE;
            case "TTL":  return RDFFormat.TURTLE;
            case "N3":  return RDFFormat.N3;
            case "NTRIPLES":  return RDFFormat.NTRIPLES;
            case "JSON-LD":  return RDFFormat.JSONLD;
            case "RDF/JSON":  return RDFFormat.RDFJSON;
            case "TRIG":  return RDFFormat.TRIG;
            case "NQUADS":  return RDFFormat.NQUADS;
            case "HDT":  return RDFFormat.HDT;
            case "NDJSON-LD":  return RDFFormat.NDJSONLD;
            case "RDFA":  return RDFFormat.RDFA;
            case "TRIG-STAR":  return RDFFormat.TRIGSTAR;
            case "TURTLE-STAR":  return RDFFormat.TURTLESTAR;

            default:  return null;
        }
    }


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
        System.out.println("   " + mess);
    }

    public static void main(String[] args) {
       try {
           String config = new String(Files.readAllBytes(Paths.get("config.json")));

           JSONObject conf = new JSONObject(config);
           JSONObject cconn = conf.getJSONObject("conn");
           if (cconn != null) {
               instance = getJSONString(cconn, "host", VIRTUOSO_INSTANCE);
               port = getJSONInt(cconn, "port", VIRTUOSO_PORT);
               uid = getJSONString(cconn, "uid", VIRTUOSO_USERNAME);
               pwd = getJSONString(cconn, "pwd", VIRTUOSO_PASSWORD);
               uid = getJSONString(cconn, "uid", VIRTUOSO_USERNAME);

               s_isolation = getJSONString(cconn, "isolationMode", s_isolation);
               s_concurrency = getJSONString(cconn, "concurrencyMode", s_concurrency);

               batch_size = getJSONInt(cconn, "batch_size", batch_size);
               useAutoCommit = getJSONBool(cconn, "useAutoCommit", useAutoCommit);
               clear_graph = getJSONString(cconn, "clear_graph", clear_graph);
               data_dir = getJSONString(cconn, "data_dir", data_dir);
               max_threads = getJSONInt(cconn, "max_threads", max_threads);


               JSONArray data = conf.getJSONArray("data");
               if (data != null) {
                   for (int i=0; i < data.length(); i++) {
                       JSONObject v = data.getJSONObject(i);
                       String fname = v.getString("file");
                       String ftype = v.getString("type");
                       String graph = v.getString("graph");
                       boolean clear_graph = v.getBoolean("clear_graph");
                       RDFFormat format = getFormat(ftype);
                       if (format == null)
                           log("Error unsupported file type: "+ftype);
                       files.push(new TaskItem(fname, format, graph, clear_graph));
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
       System.out.println("  batch_size = "+batch_size);
       System.out.println("useAutoCommit= "+useAutoCommit);
       System.out.println(" max Threads = "+max_threads);
       System.out.println("    Data dir = "+data_dir);

       System.out.println("===========================================================================\n");

       if (clear_graph != null && clear_graph.length() > 0) {
            RepositoryConnection conn = null;
            try {
                VirtuosoRepository repository = new VirtuosoRepository("jdbc:virtuoso://" + instance + ":" + port, uid, pwd);

                IRI _context = repository.getValueFactory().createIRI(clear_graph);
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

       }

        RDFLoader[] tasks = new RDFLoader[max_threads];
        for(int i=0; i< tasks.length; i++)
            tasks[i] = new RDFLoader();

        for(RDFLoader i : tasks)
            i.start();

        try {
            for (RDFLoader i : tasks)
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

            VirtuosoRepositoryConnection conn = null;

            try {
                VirtuosoRepository repository = new VirtuosoRepository("jdbc:virtuoso://" + instance + ":" + port, uid, pwd);

                repository.setConcurrencyMode(concurrency);
                vfac = repository.getValueFactory();
                context = vfac.createIRI(work.graph);

                conn = (VirtuosoRepositoryConnection) repository.getConnection();
                conn.setJdbcTransactionIsolation(isolation);

                if (work.clear_graph) {
                    log("==[" + Thread.currentThread().getName() + "] Start clear graph = " + work.graph);
                    conn.clear(context);
                    log("==[" + Thread.currentThread().getName() + "] End clear graph = " + work.graph);
                    if (!useAutoCommit)
                        conn.commit();
                }

                String fpath = (new File(data_dir, work.fname)).getPath();

                log("==[" + Thread.currentThread().getName() + "] Start load data = " + fpath);

                try (InputStream in = new FileInputStream(fpath)) {
                    conn.add(in, "myfile", RDFFormat.TURTLE, batch_size, useAutoCommit, new MyDeadLockHandler(0), context);
                }
                log("==[" + Thread.currentThread().getName() + "] End load data = " + fpath);

            } catch (Exception e) {
                log("==[" + Thread.currentThread().getName() + "]***FAILED Upload data " + e);
                e.printStackTrace();
            } finally {
                if (conn != null)
                    try {
                        conn.close();
                    } catch(Exception e) { }
            }
        }

        log("==["+Thread.currentThread().getName()+"] DONE = ");
    }

    protected static class TaskItem {
        final String fname;
        final RDFFormat ftype;
        final String graph;
        final boolean clear_graph;

        TaskItem(String fname, RDFFormat ftype, String graph, boolean clear_graph)
        {
            this.fname = fname;
            this.ftype = ftype;
            this.graph = graph;
            this.clear_graph = clear_graph;
        }
    }


    public class MyDeadLockHandler extends VirtuosoRepositoryConnection.DeadLockHandler {

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
