package me.dags.actionreplay.database;

import me.dags.commandbus.utils.StringUtils;
import me.dags.sqlutils.statement.Select;
import me.dags.sqlutils.statement.Table;

/**
 * @author dags <dags@dags.me>
 */
public class Queries {

    private static final String ID = "id";
    private static final String FRAME = "frame";

    public static String insetBlob(String name) {
        return StringUtils.format("INSERT INTO `{}` ({}) VALUES(?)", name, Queries.FRAME);
    }

    public static Table table(String name) {
        return new Table.Builder()
                .name(name)
                .column(Queries.ID, "int NOT NULL AUTO_INCREMENT")
                .column(Queries.FRAME, "MEDIUMBLOB")
                .primary(Queries.ID)
                .build();
    }

    public static Select selectReplay(String name) {
        return new Select.Builder()
                .select("*")
                .from(name)
                .build();
    }
}
