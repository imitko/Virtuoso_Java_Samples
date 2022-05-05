!!! This sample works ONLY with Virtuoso 8 and above !!!

1) Use this command to compile the sample program:

      gradlew clean build

2) Use this command to run the sample program:

      gradlew run


**NOTE 1**  
The example connects to a Virtuoso instance on 
localhost port 1111, with userid and password 
both "dba". To connect to a different Virtuoso 
server instance or use different credentials, 
you will need to edit the file "build.gradle", 
and change the args ['{hostname or IP address}', 
'{port number}', '{username}', '{password}'] to 
suit your target instance.

...
run {
args = ['localhost', '1111', 'dba', 'dba']
}
...


**NOTE 2** 
Most of the demo's requirements are contained in 
the zip file. gradle will automatically download 
any missing dependencies when the demo is run for 
the first time, so you’ll need an active internet 
connection at that time.