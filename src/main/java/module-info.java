module ui.messengerfx {
    requires javafx.controls;
    requires javafx.fxml;


    opens ui.messengerfx to javafx.fxml;
    exports ui.messengerfx;
}