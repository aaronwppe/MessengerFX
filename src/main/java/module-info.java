module ui.messengerfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens ui.messengerfx to javafx.fxml;
    exports ui.messengerfx;
}