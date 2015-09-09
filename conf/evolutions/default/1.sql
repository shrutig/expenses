# Users schema

# --- !Ups

CREATE TABLE employee (username varchar(20) PRIMARY KEY,password varchar(10),name varchar(20),accountNo int,phone int
,email varchar(30),address varchar(20),role varchar(5));

CREATE TABLE vendor (name varchar(20) PRIMARY KEY,phone int,address varchar(20),description varchar(30));

CREATE TABLE expenses (id int  NOT NULL AUTO_INCREMENT PRIMARY KEY,username varchar(20),vendor varchar(20)
,amount int,status varchar(2),description varchar(20));

INSERT INTO employee VALUES('super1','super1','super',1234,1234,'super@tuplejump.com','asd','super')
# --- !Downs

DROP TABLE employee;
DROP TABLE vendor;
DROP TABLE expenses;