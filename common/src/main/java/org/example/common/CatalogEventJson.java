package org.example.common;

public final class CatalogEventJson {

    private CatalogEventJson() {
    }

    public static String write(CatalogEvent event) {
        return "{"
                + "\"eventId\":" + quote(event.eventId())
                + ",\"type\":" + quote(event.type())
                + ",\"serverId\":" + event.serverId()
                + ",\"hostname\":" + quote(event.hostname())
                + ",\"status\":" + quote(event.status())
                + "}";
    }

    public static CatalogEvent read(String json) {
        return new CatalogEvent(
                text(json, "eventId"),
                text(json, "type"),
                Long.valueOf(raw(json, "serverId")),
                text(json, "hostname"),
                text(json, "status"));
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String text(String json, String name) {
        String value = raw(json, name);
        if (value.length() < 2 || value.charAt(0) != '"') {
            throw new IllegalArgumentException(name + " is not a string");
        }
        return value.substring(1, value.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static String raw(String json, String name) {
        String marker = "\"" + name + "\":";
        int start = json.indexOf(marker);
        if (start < 0) {
            throw new IllegalArgumentException("Missing " + name);
        }
        int valueStart = start + marker.length();
        if (json.charAt(valueStart) == '"') {
            int end = valueStart + 1;
            while (end < json.length()) {
                if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') {
                    return json.substring(valueStart, end + 1);
                }
                end++;
            }
            throw new IllegalArgumentException("Unclosed " + name);
        }
        int end = valueStart;
        while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') {
            end++;
        }
        return json.substring(valueStart, end);
    }
}
