# btprojects.java.dao.pojo.creator
Simple program that creates POJO objects from database tables.

##Intro 
Program was created to simplify development of plain jdbc or spring jdbc template based database integrations in java. 
This scaffolding based program will create plain old java objects (POJO) that mirror the database tables. It will create classes with fields that represent columns from database. Additionally it will provide helper methods that represent basic select, insert and update operations.  You can also use it to create spring row mappers. 
Program can help you create DAO layer quickly, with no extra frameworks and with full control over source code. 
Created java classes will be stored in build/createdByDaoCreator folder. 

##Usage
To setup the program path to jdbc drivers must be defined in build.gradle
```
runtime files('C:/Oracle/instantclient_12_1/ojdbc6.jar', 'ojdbc6.jar') 
```
###Arguments
* 1. dbconString - database connection string
* 2. user - db username 
* 3. pass - db password 
* 4. schema - db schema 
* 5. tables - list of tables separated by ";"
* 6. packagemodel - java package of dao objects
* 7. packagedao - java package of helper objects
* 8. type - type of creation
* 9. projectdir - alternative folder for classes storage 

###Types:
* jdbcEjb - create DAO objects and jdbc Ejb helpers.
* ClRmOnly - create DAO objects and spring Row mappers.
* spring - create DAO objects, spring row mappers and spring helpers.

###Run options:
* You can run program directly from gradle with "gradle run", but the parameters must be set in build.gradle.
* Or you can compile program and run it with argumnets with java -jar   
* Or you can create ant script to run it. Ant script example:
```
<project default="run">

	<!— this should be changed -->
	<property name="schema" value="SC" />
	<property name="table" value="TABLE1;TABLE2" />
	<property name="projectName" value="TestProject" />
	<property name="user" value="xxxxx" />
	<property name="pass" value="xxx" />
	
	<!-- we change this once -->	
	<property name="dbconString" value="jdbc:oracle:thin:@oracle.db:1521/sid" />
	<property name="packagemodel" value="${projectName}.ejb.view.models" />
	<property name="packagedao" value="${projectName}.dao" />
	<property name="type" value="spring" /> <!-- jdbcEjb, spring, ClRmOnly,  -->
	<property name="projectdir" value="${basedir}" /> <!-- option -->

	<target name="run">
	        <java jar="../folder/createDaoObjects.jar" fork="true" >
	        	<arg line="${dbconString} ${user} ${pass} ${schema} ${table} ${packagemodel} ${packagedao} ${type} ${projectdir}"/>
	         </java> 
	</target>
</project>
```


###Other databases 
You can use program with any database, as long as it provides jdbc drivers. To use other database two things must be changed:
* Dependency in build.gradle must point to the right jdbc driver path.
* In Creator.java class, runInstance method initial drivers class loading must be changed to database class. 
```
Class.forName("oracle.jdbc.driver.OracleDriver"); //oracle driver
Class.forName("com.ibm.db2.jcc.DB2Driver") //DB2 driver
```
