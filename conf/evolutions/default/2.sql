# --- !Ups

# ---ALTER TABLE expenses add column admin varchar(15);
# ---ALTER TABLE expenses add column fileName varchar(16);
# --- !Downs

# ---ALTER TABLE expenses drop column admin;
# ---ALTER TABLE expenses drop column fileName;