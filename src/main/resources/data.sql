set @timenow = current_timestamp;
insert into account (name, last_updated) values ('test_1', @timenow);
insert into account (name, last_updated) values ('test_2', @timenow);
insert into account (name, last_updated) values ('test_3', @timenow);
insert into account (name, last_updated) values ('test_4', @timenow);
insert into account (name, last_updated) values ('test_5', @timenow);
