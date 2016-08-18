# --- !Ups


create table "markets" ("id" BIGINT auto_increment NOT NULL PRIMARY KEY,"name" VARCHAR NOT NULL,"desc" VARCHAR NOT NULL,"created" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);

create table "product_types" ("id" BIGINT auto_increment NOT NULL PRIMARY KEY,"name" VARCHAR NOT NULL,"created" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);

create table "users" ("id" BIGINT auto_increment NOT NULL PRIMARY KEY,"username" VARCHAR NOT NULL,"mail" VARCHAR NOT NULL,"password" VARCHAR NOT NULL,"user_type" VARCHAR NOT NULL,"market_id" BIGINT NOT NULL,"created" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);

create table "products" ("id" BIGINT auto_increment NOT NULL PRIMARY KEY,"user_id" BIGINT NOT NULL,"product_type_id" BIGINT NOT NULL,"product_quantity" BIGINT NOT NULL,"product_constant_" DOUBLE NOT NULL,"product_exponential" DOUBLE NOT NULL,"created" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);

create table "offers" ("id" BIGINT auto_increment NOT NULL PRIMARY KEY,"market_id" BIGINT NOT NULL,"off_product_id" BIGINT NOT NULL,"off_amount" BIGINT NOT NULL,"wanted_user_id" BIGINT NOT NULL,"wanted_product_id" BIGINT NOT NULL,"wanted_amount" BIGINT NOT NULL,"created" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);

create table "transactions" ("id" BIGINT auto_increment NOT NULL PRIMARY KEY,"desc" VARCHAR NOT NULL,"off_user_id" BIGINT NOT NULL,"off_product_id" BIGINT NOT NULL,"off_amount" BIGINT NOT NULL,"off_marginal" DOUBLE NOT NULL,"wanted_user_id" BIGINT NOT NULL,"wanted_product_id" BIGINT NOT NULL,"wanted_amount" BIGINT NOT NULL,"wanted_marginal" DOUBLE NOT NULL,"created" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);

INSERT INTO "users" ("username","mail","password","user_type","market_id") VALUES
    ('username1', 'usermail1','password1','usertype1',1),
    ('username2', 'usermail2','password2','usertype2',1),
    ('username3', 'usermail3','password3','usertype3',1),
    ('username4', 'usermail4','password4','usertype4',1),
    ('username5', 'usermail5','password5','usertype5',2),
    ('username6', 'usermail6','password6','usertype6',2);

INSERT INTO "markets" ("name","desc") VALUES
    ('marketname1', 'marketdesc1'),
    ('marketname2', 'marketdesc2');

INSERT INTO "product_types" ("name") VALUES
    ('name1'),
    ('name2');
INSERT INTO "products" ("user_id","product_type_id","product_quantity","product_constant_","product_exponential") VALUES
    (1, 1,100,1.2,0.5),
    (1, 2,2,2.2,0.1),
    (3, 2,3,3.2,0.1),
    (4, 2,4,4.2,0.1),
    (5, 2,5,5.2,0.2),
    (6, 2,6,6.2,0.2);

INSERT INTO "offers" ("market_id","off_product_id","off_amount","wanted_user_id","wanted_product_id","wanted_amount") VALUES
    (1, 1,100,2,2,1),
    (1, 1,2,2,1,1),
    (1, 1,3,2,1,1),
    (2, 2,4,4,1,1),
    (2, 2,5,5,2,1),
    (2, 2,6,6,2,1);





# --- !Downs
;
drop table "transactions";
drop table "offers";
drop table "products";
drop table "users";
drop table "product_types";
drop table "markets";