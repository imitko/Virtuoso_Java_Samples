1) The following commands may be used:

   - to compile
     % gradlew clean build

   - to run
     % gradlew run

2) The following app settings are in file config.json

   - "conn"  block

   - "isolationMode": read_uncommitted | read_committed | repeatable_read | serializable 
                      default = repeatable_read 

   - "concurrencyMode": default | optimistic | pessimistic 
                        default = default 

   - "batch_size": the size of each chunk of data to be sent to server

   - "useAutoCommit": false, true

   - "clear_graph": the name of the graph that will be cleared before inserting 
                    data, if it is required. May be empty.

   - "max_threads": max count of working threads
                    default = count of uploaded files

   - "data_dir": path to directory containing data files to be loaded

   - "data" block: The list of files comprising data to be loaded to a Virtuoso 
                   DBMS instance. By default, this app will start ONE DBMS 
                   connection (with a single thread) for each source file.

     - "file" : file name, including its path

     - "type" : content-type, which may be any of: 
              "RDF/XML" | "TURTLE" | "TTL" | "N3" | "NTRIPLES" | "JSON-LD"
              | "JSON-LD10" | "JSON-LD11" | "RDF/JSON" | "TRIG" | "NQUADS"
              | "RDF-PROTO" | "RDF-THRIFT" | "SHACLC" | "TRIX"

     - "graph": named graph denoted by an IRI that names internal DBMS storage of data. 
                Note: this may be left empty if the source data comprises quads.

     - "clear_graph": true - indicates clearance of existing data associated with destination 
                    named graph prior to commencement of new data load run.


