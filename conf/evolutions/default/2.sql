
# --- !Ups
ALTER TABLE expenses ADD COLUMN description varchar(30),status varchar(2);


# --- !Downs
ALTER TABLE expenses DROP COLUMN description;