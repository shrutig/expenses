# Users schema

# --- !Ups
Use evp;

CREATE TABLE employee (userName varchar(30) PRIMARY KEY,role varchar(5));

CREATE TABLE vendor (name varchar(20) PRIMARY KEY,phone int,accountNo int,bankDetail varchar(30),address varchar(20),
description varchar(30));

CREATE TABLE expenses (id int  NOT NULL AUTO_INCREMENT PRIMARY KEY,userName varchar(10),vendor varchar(20),
amount int,status varchar(2),description varchar(20),admin varchar(10), fileName varchar(65));


# --- !Downs

DROP TABLE employee;
DROP TABLE vendor;
DROP TABLE expenses;