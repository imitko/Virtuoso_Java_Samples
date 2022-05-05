1) Use the next commands:
 - for compile
  % gradlew clean build

 - for run
  % gradlew run

2) The example connects to localhost:1111 by default,
if you have an another server, you will need edit next string in file "build.gradle"
...
run {
  args = ['localhost', '1111', 'dba', 'dba', 'repeatable_read', 'default']
}
...
App run args:
[ hostname, port_num, uid, pwd, isolationMode, concurrencyMode]

 where 
   isolationMode: read_uncommitted | read_committed | repeatable_read | serializable 
                  default isolationMode = repeatable_read 

   concurrencyMode: default | optimistic | pessimistic 
                  default concurrencyMode = default 

