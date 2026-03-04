module org.example.messagerie_association {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.persistence;
    requires org.hibernate.orm.core;
    requires java.sql;
    requires org.postgresql.jdbc;
    requires net.bytebuddy;

    opens org.example.messagerie_association to javafx.fxml;
    opens org.example.messagerie_association.entity to org.hibernate.orm.core, javafx.base;
    opens org.example.messagerie_association.repository to org.hibernate.orm.core;

    exports org.example.messagerie_association;
    exports org.example.messagerie_association.entity;
}
