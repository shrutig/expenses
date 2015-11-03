# Employee Vendor Payee System

##Prerequisites: 
This program requires SBT(Scala Build Tool) to compile and run it. It uses Play plugin 2.4.2 . A MySql server which is installed and running is required. A MYSQL version of 5.6.26 of MySQL Community Server was used. This program was written with SBT version 0.13.8 and Scala version 2.11.7. 

The following Java configuration is installed in the system: java version "1.8.0_51"
Java(TM) SE Runtime Environment (build 1.8.0_51-b16)
Java HotSpot(TM) 64-Bit Server VM (build 25.51-b03, mixed mode)

##Run the Initial.sql file from conf directory
Run this file before starting tests and running the application. In the command prompt, open the MySql prompt. Then, run this file:

```
shell> mysql -h host -u user -p
mysql> source /link/to/file/Initial.sql
```

Replace host and user with your respective values.

This file has the necessary scripts required to create a database called evp.

##Database configuration details
The database configuration details are present in application.conf in conf directory . Change the username, password 
and url for the database configuration if required.

##Default evolutions
The files 1.sql and 2.sql in conf directory have the database evolutions. In this tables called employee, vendor and expenses are created and dropped durig database evolution. Hence it is advised to make copies of any original tables by the same name in evp database.

##Project details
This Play application is an Employee-Vendor Payee System.
 
* It has the capability of allowing login for user, admin or super admin. 
* Any user can add a payment to the possible vendors. 
* A super admin has the capability of changing role of an employee or adding or removing a vendor, or approving or 
denying transactions. 
* The admin or the super admin can download the accepted transactions in a .csv file and marking the payment as processed by uploading an EBPay receipt.

Run this file by :

```
shell> sbt run
```

This will start the application at http://localhost:9000/

A default super admin user has already been added. You can add or remove any new employees using this id.

