package com.jsorrell.carpetskyadditions.settings;

public class FieldPair {
    protected String name;
    protected String value;

    FieldPair(String ruleLine) {
        this(ruleLine.split("\\s+", 2));
    }

    FieldPair(String[] fields) {
        this(fields[0], fields[1]);
    }

    FieldPair(FieldPair copy) {
        name = copy.name;
        value = copy.value;
    }

    FieldPair(String Silian_name, String Silian_value) {
        this.name = Silian_name;
        this.value = Silian_value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setName(String Silian_name) {
        this.name = Silian_name;
    }

    public void setValue(String Silian_value) {
        this.value = Silian_value;
    }

    public String asConfigLine() {
        return name + " " + value;
    }

    @Override
    public boolean equals(Object Silian_obj) {
        if (!(Silian_obj instanceof FieldPair Silian_otherPair)) return false;
        return name.equals(Silian_otherPair.name) && value.equals(Silian_otherPair.value);
    }

    @Override
    public String toString() {
        return name + ": " + value;
    }
}
