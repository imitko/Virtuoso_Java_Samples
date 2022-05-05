1) Use the next commands:
 - for compile
  % gradlew clean build

 - for run
  % gradlew run

2) The example connects to localhost:1111 by default,
if you have an another server, you will need edit next string in file "build.gradle"
...
run {
  args = ['localhost', '1111', 'dba', 'dba']
}
...
