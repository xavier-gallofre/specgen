package com.specgen.database.exporter;

import com.specgen.core.model.PropertyDefinition;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestor que aplica una lista de reglas de exportación y mantiene un registro para revisión.
 */
public class ExportRuleManager {

    private final List<ExportRule> rules = new ArrayList<>();
    private final List<ReviewEntry> reviewLog = new ArrayList<>();

    public void addRule(ExportRule rule) {
        this.rules.add(rule);
    }

    public PropertyDefinition applyRules(String tableName, String columnName, String sqlType, Integer columnSize, PropertyDefinition currentProp) {
        PropertyDefinition result = currentProp;
        for (ExportRule rule : rules) {
            PropertyDefinition newProp = rule.apply(tableName, columnName, sqlType, columnSize, result);
            if (newProp != null && !newProp.equals(result)) {
                String note = rule.getReviewNote(tableName, columnName, sqlType, columnSize);
                if (note != null) {
                    reviewLog.add(new ReviewEntry(tableName, columnName, note));
                }
                result = newProp;
            }
        }
        return result;
    }

    public List<ReviewEntry> getReviewLog() {
        return reviewLog;
    }

    public static record ReviewEntry(String tableName, String columnName, String note) {}
}
