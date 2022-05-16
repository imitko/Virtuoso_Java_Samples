1) Use the next commands:
 - for compile
  % gradlew clean build

 - for run
  % gradlew run

2) The App settings are in file config.json
where 
- "conn"  block
   isolationMode: read_uncommitted | read_committed | repeatable_read | serializable 
                  default isolationMode = repeatable_read 

   concurrencyMode: default | optimistic | pessimistic 
                  default concurrencyMode = default 

   chunk_size: the size of one chunk data, that will be sent to server

   useAutoCommit: false, true

   clear_graph: the graph name, that will be clear before insert data, if it is required, may be empty

   max_threads: max count of working threads, by default max count of threads = count of uploaded files

   data_dir: directory name with data files


- "data" block
   The list of files with data, that will be inserted to DB.
   Now app start ONE thread connection for each file.
   "file" : file name with path
   "type" : type of data, may be one of: 
               RDF/XML | TURTLE | TTL | N3 | NTRIPLES | JSON-LD | JSON-LD10 | JSON-LD11 | RDF/JSON |
               TRIG | NQUADS | RDF-PROTO | RDF-THRIFT | SHACLC | TRIX
   "graph": graph name for data , may be empty for quad data
   "clear_graph": true - if graph must be cleared before insert data from file.


