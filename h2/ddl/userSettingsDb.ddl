create table connection_info
(
	id bigint auto_increment primary key,
	euid varchar not null,
	connection_driver varchar not null,
	connection_url varchar not null,
	connection_user varchar not null,
	connection_name varchar not null
);