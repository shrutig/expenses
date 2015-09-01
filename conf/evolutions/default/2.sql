# --- !Ups

ALTER TABLE expenses add column admin varchar(15);

# --- !Downs

ALTER TABLE expenses drop column admin;