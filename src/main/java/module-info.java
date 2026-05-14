module app.db_proj {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens app.db_proj to javafx.fxml;
    exports app.db_proj;
}