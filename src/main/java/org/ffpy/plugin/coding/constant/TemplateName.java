package org.ffpy.plugin.coding.constant;

/**
 * 模板名称
 */
public enum TemplateName {
    ENUM_CODE("EnumCode.vm"),
    ;

    public static final String PATH_TEMPLATE = "/template/";

    public static String getPath(String name) {
        return PATH_TEMPLATE + name;
    }

    private final String name;

    TemplateName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return PATH_TEMPLATE + name;
    }
}
