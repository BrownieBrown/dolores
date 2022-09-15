CREATE TABLE user_data (
        id uuid DEFAULT uuid_generate_v4(),
        email varchar(255),
        full_name varchar(255),
        hashed_password varchar(255),
        is_active boolean not null,
        is_super_user boolean not null,
        PRIMARY KEY (id)
    )