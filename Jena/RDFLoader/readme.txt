1) Use the next commands:
 - to compile
  % gradlew clean build

 - to run
  % gradlew run

2) App settings are in file config.json
where 
- "conn"  block
   "isolationMode": read_uncommitted | read_committed | repeatable_read | serializable 
                  default isolationMode = repeatable_read 

   "concurrencyMode": default | optimistic | pessimistic 
                  default concurrencyMode = default 

   "batch_size": the size of one chunk data, that will be sent to server

   "useAutoCommit": false, true

   "clear_graph": the graph name, that will be clear before insert data, if it is required, may be empty

   "max_threads": max count of working threads, by default max count of threads = count of uploaded files

   "data_dir": directory name with data files


- "data" block
   The list of files comprising data to be loaded to a Virtuoso DBMS instance.
   By default, this app starts ONE DBMS connection (with a single thread) for each source file.
   "file" : file name that includes its path
   "type" : content-type, which may be one of: 
               RDF/XML | TURTLE | TTL | N3 | NTRIPLES | JSON-LD | JSON-LD10 | JSON-LD11 | RDF/JSON |
               TRIG | NQUADS | RDF-PROTO | RDF-THRIFT | SHACLC | TRIX
   "graph": named graph denoted by an IRI that names internal DBMS storage of data. Note, this may be left empty if the source data comprises quads
   "clear_graph": true - indicates clearance of existing data associated with destination named graph prior to commencement of new data load run.


