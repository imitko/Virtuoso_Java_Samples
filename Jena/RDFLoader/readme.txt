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

- "data" block
   The list of files with data, that will be inserted to DB.
   Now app start ONE thread connection for each file.
   "file" : file name with path
   "type" : type of data, may be one of: ttl | n3 | jsonld | rdfxml | turtle 
   "graph": graph name for data
   "clear_graph": true - if graph must be cleared before insert data from file.
