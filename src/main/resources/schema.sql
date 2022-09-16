---

create table if not exists TB_LIVRO(
    id long auto_increment not null primary key,
    titulo varchar(100) not null,
    isbn varchar(50) not null
);
